package com.envoy.androidsdk.domain.model

import com.google.gson.annotations.SerializedName

data class UserCurrentRewardsResponse(
    @SerializedName("earnedThisPeriod") val earnedThisPeriod: Int,
    @SerializedName("earnableLeft") val earnableLeft: Int,
    @SerializedName("eventCount") val eventCount: EventCount
)

data class EventCount(
    @SerializedName("completed") val completed: Int,
    @SerializedName("leftToReward") val leftToReward: Int,
    @SerializedName("percentageDone") val percentageDone: Int
)
