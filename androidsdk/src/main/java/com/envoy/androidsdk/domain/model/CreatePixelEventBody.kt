package com.envoy.androidsdk.domain.model

import com.google.gson.annotations.SerializedName

data class CreatePixelEventBody(
    @SerializedName("event_name") val eventName: String,
    @SerializedName("lead_uuid") var leadUuid: String? = null,
    @SerializedName("sharer_user_id") val sharerUserId: String? = null,
    @SerializedName("extra") val extra: ExtraPixel? = null,
    @SerializedName("share_link_hash") var shareLinkHash: String? = null
)

data class ExtraPixel(
    @SerializedName("campaign") val campaign: String,
    @SerializedName("userType") val userType: String
)

enum class EventName {
    app_downloaded,
    account_created,
    trial_activated,
    payment_success
}