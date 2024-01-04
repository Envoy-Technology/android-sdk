package com.envoy.androidsdk.domain.model

import com.google.gson.annotations.SerializedName

data class GetUserRewardResponse(
    @SerializedName("userId") val userId: String,
    @SerializedName("rewardAvailable") val rewardAvailable: Boolean
)
