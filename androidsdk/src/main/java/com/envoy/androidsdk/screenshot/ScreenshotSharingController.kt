package com.envoy.androidsdk.screenshot

import com.envoy.androidsdk.domain.model.CreateLinkBody
import com.envoy.androidsdk.domain.model.CreateLinkResponse

/** Screenshots of one activity. */
internal interface ScreenCapture {
    /**
     * Calls [onCaptured] on the main thread for each screenshot, until the returned function is called.
     * Returns null when the device can't report screenshots.
     */
    fun register(onCaptured: () -> Unit): (() -> Unit)?
}

/** The system share sheet, opened from one activity. */
internal interface ShareSheet {
    /** Returns false when it can't be opened. */
    fun open(text: String, title: String?): Boolean
}

internal interface LinkCreator {
    /** Calls [onResult] on the main thread, unless the returned function is called first. */
    fun createLink(body: CreateLinkBody, onResult: (Result<CreateLinkResponse>) -> Unit): () -> Unit
}

/**
 * While a screen has it turned on, answers each screenshot by creating the screen's link
 * and opening the system share sheet with it. Main-thread only.
 */
internal class ScreenshotSharingController(
    private val linkCreator: LinkCreator,
    private val log: (String) -> Unit = {}
) {

    /** One screen with sharing turned on, driven by its lifecycle. */
    inner class Session(
        val screenCapture: ScreenCapture,
        val shareSheet: ShareSheet,
        val onEvent: ((ScreenshotSharingEvent) -> Unit)?,
        val share: () -> ScreenshotShare?
    ) {
        /** Set by whoever drives the lifecycle; called when the session ends. */
        var detach: (() -> Unit)? = null

        internal var unregisterCapture: (() -> Unit)? = null
        internal var isResumed = false
        internal var cancelLink: (() -> Unit)? = null
        /** The link is ready and the share sheet opens once the screen is resumed. */
        internal var pendingShare: Pair<String, String?>? = null
        /** The share sheet is open; cleared when the screen is resumed again. */
        internal var isSharing = false

        /** From the screenshot until the user is back from the share sheet; screenshots meanwhile are ignored. */
        internal val isBusy: Boolean
            get() = cancelLink != null || pendingShare != null || isSharing

        fun onStarted() {
            if (this !== current || unregisterCapture != null) return
            unregisterCapture = screenCapture.register { onScreenshot(this) }
            if (unregisterCapture == null) log("Screenshot sharing needs Android 14 or later")
        }

        fun onResumed() {
            if (this !== current) return
            isResumed = true
            isSharing = false
            pendingShare?.let { (text, title) ->
                pendingShare = null
                open(this, text, title)
            }
        }

        fun onPaused() {
            isResumed = false
        }

        /** A link still being created is dropped, so no share sheet opens once the screen is gone. */
        fun onStopped() {
            isResumed = false
            unregisterCapture?.invoke()
            unregisterCapture = null
            cancelLink?.invoke()
            cancelLink = null
            pendingShare = null
            isSharing = false
        }

        fun end() {
            onStopped()
            if (current === this) current = null
            detach?.invoke()
            detach = null
        }
    }

    private var current: Session? = null
    /** Links created in this app session, by request, so more screenshots of the same content reuse them. */
    private val createdLinks = mutableMapOf<CreateLinkBody, CreateLinkResponse>()

    /** Replaces the screen started earlier, if any. */
    fun start(
        screenCapture: ScreenCapture,
        shareSheet: ShareSheet,
        onEvent: ((ScreenshotSharingEvent) -> Unit)?,
        share: () -> ScreenshotShare?
    ): Session {
        stop()
        return Session(screenCapture, shareSheet, onEvent, share).also { current = it }
    }

    fun stop() {
        current?.end()
    }

    private fun onScreenshot(session: Session) {
        if (session !== current || session.isBusy) return
        val share = session.share() ?: return
        val title = share.body.contentSetting.name

        createdLinks[share.body]?.let { link ->
            session.onEvent?.invoke(ScreenshotSharingEvent.LinkCreated(link))
            present(session, link, share.message, title)
            return
        }

        session.onEvent?.invoke(ScreenshotSharingEvent.CreatingLink)
        var finished = false
        val cancel = linkCreator.createLink(share.body) { result ->
            finished = true
            session.cancelLink = null
            if (session !== current) return@createLink
            val link = result.getOrNull()
            when {
                link == null -> session.onEvent?.invoke(
                    ScreenshotSharingEvent.Failed(result.exceptionOrNull() ?: IllegalStateException("No link"))
                )
                link.url.isNullOrEmpty() -> session.onEvent?.invoke(
                    ScreenshotSharingEvent.Failed(IllegalStateException("The created link has no url"))
                )
                else -> {
                    createdLinks[share.body] = link
                    session.onEvent?.invoke(ScreenshotSharingEvent.LinkCreated(link))
                    present(session, link, share.message, title)
                }
            }
        }
        if (!finished) session.cancelLink = cancel
    }

    private fun present(session: Session, link: CreateLinkResponse, message: String?, title: String?) {
        val text = listOfNotNull(message, link.url).joinToString("\n")
        if (session.isResumed) {
            open(session, text, title)
        } else {
            // The system screenshot UI can pause the activity; the share sheet opens once it's back.
            session.pendingShare = text to title
        }
    }

    private fun open(session: Session, text: String, title: String?) {
        if (session.shareSheet.open(text, title)) {
            session.isSharing = true
            session.onEvent?.invoke(ScreenshotSharingEvent.ShareSheetOpened)
        } else {
            log("Could not open the share sheet")
        }
    }
}
