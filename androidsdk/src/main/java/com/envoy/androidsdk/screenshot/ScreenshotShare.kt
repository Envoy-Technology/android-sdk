package com.envoy.androidsdk.screenshot

import com.envoy.androidsdk.domain.model.CreateLinkBody
import com.envoy.androidsdk.domain.model.CreateLinkResponse

/**
 * What to share when the user takes a screenshot: the link to create and the text sent with it.
 *
 * @param body The link to create, e.g. [CreateLinkBody.forArticle]
 * @param message Shared together with the link, e.g. "Your friend wants to gift you this article"
 */
data class ScreenshotShare(
    val body: CreateLinkBody,
    val message: String? = null
)

/**
 * A screenshot waiting for your own screen, e.g. a "Send a gift" dialog, before anything is shared.
 * No link is created, so no quota is used, until [share] is called. Main thread only.
 *
 * @property content What [share] shares, as returned for this screenshot
 */
class ScreenshotSharePrompt internal constructor(
    val content: ScreenshotShare,
    onResolved: (ScreenshotSharePrompt, share: Boolean) -> Unit
) {
    private var onResolved: ((ScreenshotSharePrompt, Boolean) -> Unit)? = onResolved

    /**
     * False once [share] or [cancel] was called, or once the screen stopped screenshot sharing,
     * e.g. because it was destroyed; [share] and [cancel] then do nothing.
     */
    val isActive: Boolean
        get() = onResolved != null

    /**
     * Creates the link and opens the system share sheet with it. Only the first [share] or [cancel] counts.
     * Returns false when the prompt isn't active, so nothing is shared.
     */
    fun share(): Boolean = resolve(share = true)

    /**
     * Nothing is shared and no link is created. Call it when your screen is closed without sharing.
     * Returns false when the prompt isn't active.
     */
    fun cancel(): Boolean = resolve(share = false)

    /** The screen stopped screenshot sharing; [share] and [cancel] do nothing from now on. */
    internal fun invalidate() {
        onResolved = null
    }

    private fun resolve(share: Boolean): Boolean {
        val onResolved = onResolved ?: return false
        this.onResolved = null
        onResolved(this, share)
        return true
    }
}

/** Progress of the sharing started by a screenshot. Delivered on the main thread. */
sealed class ScreenshotSharingEvent {
    /** The link is being created; the share sheet opens when it's ready. */
    object CreatingLink : ScreenshotSharingEvent()

    /** The link was created, or reused from an earlier screenshot of the same content. */
    data class LinkCreated(val response: CreateLinkResponse) : ScreenshotSharingEvent()

    /** The system share sheet was opened with the link. */
    object ShareSheetOpened : ScreenshotSharingEvent()

    /** The link couldn't be created, so no share sheet was shown. */
    data class Failed(val throwable: Throwable) : ScreenshotSharingEvent()
}
