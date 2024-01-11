package com.envoy.androidsdk.domain.model

import com.google.gson.annotations.SerializedName

data class UserQuotaResponse(
    @SerializedName("userId") val userId: String?,
    @SerializedName("userRemainingQuota") val remainingQuota: Int?
)
