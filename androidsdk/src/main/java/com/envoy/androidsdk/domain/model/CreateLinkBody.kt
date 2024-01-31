package com.envoy.androidsdk.domain.model

import com.google.gson.annotations.SerializedName

/**
* @param autoPlay If true, the content will start when the user reaches the page
* @param contentSetting Content related fields used for landing display
* @param lifespanClick If this field is set, after a link was opened the a countdown starts until the link is no longer available.
* @param openQuota Sets the number of opens that can happen in a link.(Overrides the current link rules)
* @param extra Stores any extra fields that you need as JSON format
* @param sharerId ID of the user who created this link
* @param isSandbox Declare if this is a sandbox link
* @param labels Labels attached to this link
*/

data class CreateLinkBody(
    @SerializedName("autoplay") val autoPlay: Boolean = false,
    @SerializedName("content_setting") val contentSetting: ContentSetting,
    @SerializedName("lifespan_after_click") val lifespanClick: LifespanClick? = null,
    @SerializedName("open_quota") val openQuota: Int? = null,
    @SerializedName("extra") val extra: String? = null,
    @SerializedName("sharer_id") val sharerId: String,
    @SerializedName("is_sandbox") val isSandbox: Boolean = false,
    @SerializedName("labels") val labels: List<Label>? = null
)

/**
 * @param type Type of the content (see ContentType)
 * @param name Title of the content, will be displayed on gift landing.
 * @param description Description of the content, will be displayed on gift landing.
 * @param commonData The urls used for players and HTML pages
 * @param timeLimit Number of seconds that the VIDEO/AUDIO content are available
 * @param timeStart Timestamp of start for playable media type on gift landing page.
 * @param availableFrom Datetime determining when content will be available to user.
 * @param availableTo Datetime determining until when content will be available to user.
 * @param videoOrientation Determines orientation of the player in case of VIDEO contents.
 * @param previewTitle Title set as a meta tag on the web page, will be displayed when sending the link using messaging platforms
 * @param previewDescription Description set as a meta tag on the web page, will be displayed when sending the link using messaging platforms
 * @param previewImage URL for an image set as a meta tag on the web page, will be displayed when sending the link using messaging platforms
 * @param isEmailMandatory Blocks the content until the user enters their email that creates a Lead. Leads can be seen in the Leads tab in the dashboard
 * @param modalTitle Title of the modal where the user enters the email
 * @param buttonText Text on the button in the popup
 * @param contentProtection Content Protections used on the common
 */

data class ContentSetting(
    @SerializedName("content_type") val type: ContentType,
    @SerializedName("content_name") val name: String,
    @SerializedName("content_description") val description: String,
    @SerializedName("common") val commonData: CommonData,
    @SerializedName("time_limit") val timeLimit: Int? = null,
    @SerializedName("time_start") val timeStart: Int? = null,
    @SerializedName("available_from") val availableFrom: String? = null,
    @SerializedName("available_to") val availableTo: String? = null,
    @SerializedName("video_orientation") val videoOrientation: VideoOrientation? = null,
    @SerializedName("preview_title") val previewTitle: String? = null,
    @SerializedName("preview_description") val previewDescription: String? = null,
    @SerializedName("preview_image") val previewImage: String? = null,
    @SerializedName("mandatory_email") val isEmailMandatory: Boolean? = false,
    @SerializedName("modal_title") val modalTitle: String? = null,
    @SerializedName("button_text") val buttonText: String? = null,
    @SerializedName("content_protection") val contentProtection: Media? = null
)

/**
 * @param source URI to content, may be Manifest or Link to file or file stream in case of audio or video content type, plain html or source to public html
 * @param isRedirect If True, envoy will use `source` to get URL for the player from the redirect response
 * @param poster URI to the image used as video/audio poster
 */

data class CommonData(
    @SerializedName("source") val source: String,
    @SerializedName("source_is_redirect") val isRedirect: Boolean?,
    @SerializedName("poster") val poster: String?
)

data class LifespanClick(
    @SerializedName("value") val value: Int?,
    @SerializedName("unit") val unit: String?
)

data class Label(
    @SerializedName("id") val value: Int?,
    @SerializedName("text") val text: String,
    @SerializedName("color") val color: String
)

data class Media(
    @SerializedName("media") val mediaData: CommonData,
    @SerializedName("drm_player") val drmPlayer: DrmPlayer,
    // widevine | fairPlay | playerReady
    @SerializedName("player") val player: String
)

data class DrmPlayer(
    @SerializedName("server_name") val serverName: String,
    @SerializedName("server_certificate") val serverCertificate: String
)

enum class ContentType {
    VIDEO,
    AUDIO,
    HTML_PLAIN,
    HTML_SOURCE,
    SCREENSHOT
}

enum class VideoOrientation {
    VERTICAL,
    HORIZONTAL
}

