package com.envoy.androidsdk.domain.model

import com.google.gson.annotations.SerializedName

data class CreateLinkBody(
    @SerializedName("autoplay") val autoPlay: Boolean = false,
    @SerializedName("content_setting") val contentSetting: ContentSetting,
    @SerializedName("lifespan_after_click") val lifespanClick: LifespanClick? = null,
    @SerializedName("open_quota") val openQuota: Int? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("sharer_id") val sharerId: String,
    @SerializedName("is_sandbox") val isSandbox: Boolean = false,
    @SerializedName("labels") val labels: List<Label>? = null,
)

data class ContentSetting(
    @SerializedName("content_type") val type: String,
    @SerializedName("content_name") val name: String,
    @SerializedName("content_description") val description: String,
    @SerializedName("common") val commonData: CommonData,
    @SerializedName("time_limit") val timeLimit: Int? = null,
    @SerializedName("time_start") val timeStart: Int? = null,
    @SerializedName("available_from") val availableFrom: String? = null,
    @SerializedName("available_to") val availableTo: String? = null,
    @SerializedName("video_orientation") val videoOrientation: Int? = null,
    @SerializedName("preview_title") val previewTitle: String? = null,
    @SerializedName("preview_description") val previewOrientation: String? = null,
    @SerializedName("preview_image") val previewImage: String? = null,
    @SerializedName("is_sandbox") val isSandbox: Boolean = false,
    @SerializedName("mandatory_email") val isEmailMandatory: Boolean = false,
    @SerializedName("modal_title") val modalTitle: String? = null,
    @SerializedName("button_text") val buttonText: String? = null,
    @SerializedName("content_protection") val contentProtection: Media? = null,
)

data class CommonData(
    @SerializedName("source") val source: String?,
    @SerializedName("source_is_redirect") val isRedirect: Boolean,
    @SerializedName("poster") val poster: String?,
)

data class LifespanClick(
    @SerializedName("value") val value: Int?,
    @SerializedName("unit") val unit: String?,
)

data class Label(
    @SerializedName("id") val value: Int?,
    @SerializedName("text") val text: String?,
    @SerializedName("color") val color: String?,
)

data class Media(
    @SerializedName("media") val mediaData: CommonData,
)
