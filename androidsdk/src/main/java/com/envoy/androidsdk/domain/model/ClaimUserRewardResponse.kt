package com.envoy.androidsdk.domain.model

import com.google.gson.annotations.SerializedName

data class ClaimUserRewardResponse(
    @SerializedName("rewardClaimed") val rewardClaimed: Int
)
