package com.envoy.androidsdk.domain.model

import com.google.gson.annotations.SerializedName

data class ClaimUserRewardBody(
    @SerializedName("userId") val userId: String
)
