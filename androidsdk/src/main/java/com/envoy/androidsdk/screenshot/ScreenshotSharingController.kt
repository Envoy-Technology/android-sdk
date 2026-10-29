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

/** What the share sheet shows: [text] is shared, [title] and [imageUri] only preview it. */
internal data class ShareContent(val text: String, val title: String?, val imageUri: String?)

/** The system share sheet, opened from one activity. */
internal interface ShareSheet {
    /** Returns false when it can't be opened. */
    fun open(content: ShareContent): Boolean
}

internal interface LinkCreator {
    /** Calls [onResult] on the main thread, unless the returned function is called first. */
    fun createLink(body: CreateLinkBody, onResult: (Result<CreateLinkResponse>) -> Unit): () -> Unit
}

internal interface ImageLoader {
    /**
     * Calls [onResult] on the main thread with a URI the share sheet can read the image from,
     * or null when it can't be loaded in time; unless the returned function is called first.
     */
    fun load(url: String, onResult: (String?) -> Unit): () -> Unit
}

/**
 * While a screen has it turned on, answers each screenshot by creating the screen's link
 * and opening the system share sheet with it. Main-thread only.
 */
internal class ScreenshotSharingController(
    private val linkCreator: LinkCreator,
    private val imageLoader: ImageLoader? = null,
    private val log: (String) -> Unit = {}
) {

    /** One screen with sharing turned on, driven by its lifecycle. */
    inner class Session(
        val screenCapture: ScreenCapture,
        val shareSheet: ShareSheet,
        val onEvent: ((ScreenshotSharingEvent) -> Unit)?,
        val beforeSharing: ((ScreenshotSharePrompt) -> Unit)?,
        val share: () -> ScreenshotShare?
    ) {
        /** Set by whoever drives the lifecycle; called when the session ends. */
        var detach: (() -> Unit)? = null

        internal var unregisterCapture: (() -> Unit)? = null
        internal var isResumed = false
        /** Your screen is shown; kept while the screen is stopped, e.g. when the user leaves the app meanwhile. */
        internal var prompt: ScreenshotSharePrompt? = null
        internal var preparation: Preparation? = null
        /** The link is ready and the share sheet opens once the screen is resumed. */
        internal var pendingShare: ShareContent? = null
        /** The share sheet is open; cleared when the screen is resumed again. */
        internal var isSharing = false

        /** From the screenshot until the user is back from the share sheet; screenshots meanwhile are ignored. */
        internal val isBusy: Boolean
            get() = prompt != null || preparation != null || pendingShare != null || isSharing

        fun onStarted() {
            if (this !== current || unregisterCapture != null) return
            unregisterCapture = screenCapture.register { onScreenshot(this) }
            if (unregisterCapture == null) log("Screenshot sharing needs Android 14 or later")
        }

        fun onResumed() {
            if (this !== current) return
            isResumed = true
            isSharing = false
            pendingShare?.let {
                pendingShare = null
                open(this, it)
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
            preparation?.cancel()
            preparation = null
            pendingShare = null
            isSharing = false
        }

        fun end() {
            onStopped()
            prompt = null
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
        beforeSharing: ((ScreenshotSharePrompt) -> Unit)? = null,
        share: () -> ScreenshotShare?
    ): Session {
        stop()
        return Session(screenCapture, shareSheet, onEvent, beforeSharing, share).also { current = it }
    }

    fun stop() {
        current?.end()
    }

    /** From the screenshot until both the link and the preview image are ready. */
    internal class Preparation(val share: ScreenshotShare) {
        var link: CreateLinkResponse? = null
        var isImageLoaded = false
        var imageUri: String? = null
        var cancelLink: (() -> Unit)? = null
        var cancelImage: (() -> Unit)? = null

        fun cancel() {
            cancelLink?.invoke()
            cancelLink = null
            cancelImage?.invoke()
            cancelImage = null
        }
    }

    private fun onScreenshot(session: Session) {
        if (session !== current || session.isBusy) return
        val share = session.share() ?: return
        val beforeSharing = session.beforeSharing
        if (beforeSharing == null) {
            prepare(session, share)
            return
        }
        val prompt = ScreenshotSharePrompt(share) { prompt, accepted ->
            if (session !== current || session.prompt !== prompt) return@ScreenshotSharePrompt
            session.prompt = null
            if (accepted) prepare(session, prompt.content)
        }
        session.prompt = prompt
        beforeSharing(prompt)
    }

    private fun prepare(session: Session, share: ScreenshotShare) {
        val preparation = Preparation(share)
        session.preparation = preparation

        // Loaded together with the link; the share sheet opens without it if it isn't ready in time.
        val setting = share.body.contentSetting
        val imageUrl = setting.previewImage ?: setting.commonData?.poster
        if (imageUrl.isNullOrEmpty() || imageLoader == null) {
            preparation.isImageLoaded = true
        } else {
            val cancel = imageLoader.load(imageUrl) { uri ->
                preparation.isImageLoaded = true
                preparation.imageUri = uri
                preparation.cancelImage = null
                presentIfReady(session, preparation)
            }
            if (!preparation.isImageLoaded) preparation.cancelImage = cancel
        }

        val cachedLink = createdLinks[share.body]
        if (cachedLink != null) {
            preparation.link = cachedLink
            session.onEvent?.invoke(ScreenshotSharingEvent.LinkCreated(cachedLink))
            presentIfReady(session, preparation)
            return
        }

        session.onEvent?.invoke(ScreenshotSharingEvent.CreatingLink)
        var finished = false
        val cancel = linkCreator.createLink(share.body) { result ->
            finished = true
            preparation.cancelLink = null
            if (session !== current || session.preparation !== preparation) return@createLink
            val link = result.getOrNull()
            when {
                link == null -> fail(session, preparation, result.exceptionOrNull() ?: IllegalStateException("No link"))
                link.url.isNullOrEmpty() -> fail(session, preparation, IllegalStateException("The created link has no url"))
                else -> {
                    createdLinks[share.body] = link
                    preparation.link = link
                    session.onEvent?.invoke(ScreenshotSharingEvent.LinkCreated(link))
                    presentIfReady(session, preparation)
                }
            }
        }
        if (!finished) preparation.cancelLink = cancel
    }

    private fun fail(session: Session, preparation: Preparation, throwable: Throwable) {
        preparation.cancel()
        session.preparation = null
        session.onEvent?.invoke(ScreenshotSharingEvent.Failed(throwable))
    }

    private fun presentIfReady(session: Session, preparation: Preparation) {
        if (session !== current || session.preparation !== preparation) return
        val link = preparation.link ?: return
        if (!preparation.isImageLoaded) return
        session.preparation = null

        val share = preparation.share
        val content = ShareContent(
            text = listOfNotNull(share.message, link.url).joinToString("\n"),
            title = share.body.contentSetting.name,
            imageUri = preparation.imageUri
        )
        if (session.isResumed) {
            open(session, content)
        } else {
            // The system screenshot UI can pause the activity; the share sheet opens once it's back.
            session.pendingShare = content
        }
    }

    private fun open(session: Session, content: ShareContent) {
        if (session.shareSheet.open(content)) {
            session.isSharing = true
            session.onEvent?.invoke(ScreenshotSharingEvent.ShareSheetOpened)
        } else {
            log("Could not open the share sheet")
        }
    }
}
