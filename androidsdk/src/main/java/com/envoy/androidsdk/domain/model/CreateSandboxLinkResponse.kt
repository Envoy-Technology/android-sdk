package com.envoy.androidsdk.domain.model

import com.google.gson.annotations.SerializedName

data class CreateSandboxLinkResponse(
    @SerializedName("url") val url: String?,
    @SerializedName("user_remaining_quota") val remainingQuota: Int?,
)
