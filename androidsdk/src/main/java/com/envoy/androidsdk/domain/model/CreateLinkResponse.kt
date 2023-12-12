package com.envoy.androidsdk.domain.model

import com.google.gson.annotations.SerializedName

data class CreateLinkResponse(
    @SerializedName("url") val url: String?,
    @SerializedName("user_remaining_quota") val remainingQuota: Int?,
    @SerializedName("mandatory_email") val isEmailMandatory: Boolean,
    @SerializedName("modal_title") val modalTitle: String?,
    @SerializedName("button_text") val buttonText: String?,
)