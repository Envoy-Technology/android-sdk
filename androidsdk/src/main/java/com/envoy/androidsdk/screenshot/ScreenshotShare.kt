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
