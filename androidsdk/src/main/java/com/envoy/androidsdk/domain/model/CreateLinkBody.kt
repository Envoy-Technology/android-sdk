package com.envoy.androidsdk.domain.model

import com.google.gson.annotations.SerializedName

data class CreateLinkBody(
    @SerializedName("autoplay") val autoPlay: Boolean,
    @SerializedName("content_setting") val contentSetting: ContentSetting,
    @SerializedName("lifespan_after_click") val lifespanClick: LifespanClick,
    @SerializedName("open_quota") val openQuota: Int,
    @SerializedName("title") val title: String,
    @SerializedName("sharer_id") val sharerId: String,
    @SerializedName("is_sandbox") val isSandbox: Boolean,
    @SerializedName("labels") val labels: List<Label>,
)

data class ContentSetting(
    @SerializedName("content_type") val type: String?,
    @SerializedName("content_name") val name: String?,
    @SerializedName("content_description") val description: String?,
    @SerializedName("common") val commonData: CommonData,
    @SerializedName("time_limit") val timeLimit: Int?,
    @SerializedName("time_start") val timeStart: Int?,
    @SerializedName("available_from") val availableFrom: String?,
    @SerializedName("available_to") val availableTo: String?,
    @SerializedName("video_orientation") val videoOrientation: Int?,
    @SerializedName("preview_title") val previewTitle: String?,
    @SerializedName("preview_description") val previewOrientation: String?,
    @SerializedName("preview_image") val previewImage: String?,
    @SerializedName("is_sandbox") val isSandbox: Boolean,
    @SerializedName("mandatory_email") val isEmailMandatory: Boolean,
    @SerializedName("modal_title") val modalTitle: String?,
    @SerializedName("button_text") val buttonText: String?,
    @SerializedName("content_protection") val contentProtection: CommonData,
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
