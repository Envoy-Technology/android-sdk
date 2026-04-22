package com.envoy.androidsdk.screenshot

import android.graphics.Bitmap
import com.envoy.androidsdk.domain.model.ContentSetting
import com.envoy.androidsdk.domain.model.ContentType
import com.envoy.androidsdk.domain.model.CreateLinkBody

/**
 * Helper class for creating screenshot-based share links.
 *
 * Usage:
 * ```
 * val linkBody = ScreenshotLinkHelper.createScreenshotLinkBody(
 *     bitmap = screenshotBitmap,
 *     sharerId = "user123",
 *     contentName = "My Screenshot",
 *     contentDescription = "Screenshot shared via app"
 * )
 *
 * EnvoyApiProviderImpl.provide().createLink(linkBody).collect { resource ->
 *     // Handle response
 * }
 * ```
 */
object ScreenshotLinkHelper {

    /**
     * Creates a [CreateLinkBody] configured for sharing a screenshot.
     *
     * @param bitmap The screenshot bitmap to share
     * @param sharerId ID of the user creating the link
     * @param contentName Title for the screenshot content
     * @param contentDescription Description for the screenshot content
     * @param isSandbox Whether this is a sandbox link (default: false)
     * @param isCarouselLink Whether this is a carousel link (default: false)
     * @param compressionFormat Bitmap compression format (default: PNG)
     * @param compressionQuality Compression quality 0-100 (default: 100)
     * @return [CreateLinkBody] ready to be used with EnvoyApi.createLink()
     */
    fun createScreenshotLinkBody(
        bitmap: Bitmap,
        sharerId: String,
        contentName: String = "Screenshot",
        contentDescription: String = "Shared screenshot",
        isSandbox: Boolean = false,
        isCarouselLink: Boolean = false,
        compressionFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
        compressionQuality: Int = 100
    ): CreateLinkBody {
        val base64Image = bitmap.toBase64(compressionFormat, compressionQuality)

        val contentSetting = ContentSetting(
            type = ContentType.SCREENSHOT,
            name = contentName,
            description = contentDescription,
            base64File = base64Image
        )

        return CreateLinkBody(
            contentSetting = contentSetting,
            sharerId = sharerId,
            isSandbox = isSandbox,
            isCarouselLink = isCarouselLink
        )
    }
}
