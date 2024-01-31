package com.envoy.androidsdk.domain.model

import com.google.gson.annotations.SerializedName

/**
 * @param eventName Name of the tracking event
 * @param leadUuid UUID of the lead received by email popup
 * @param sharerUserId ID of the internal user, this is in case share_link_hash is not provided
 * @param extra Any extra fields that you might want to track, sent as JSON
 * @param shareLinkHash Hash of the link
 */

data class CreatePixelEventBody(
    @SerializedName("event_name") val eventName: String,
    @SerializedName("lead_uuid") var leadUuid: String? = null,
    @SerializedName("sharer_user_id") val sharerUserId: String? = null,
    @SerializedName("extra") val extra: String? = null,
    @SerializedName("share_link_hash") var shareLinkHash: String? = null
)

enum class EventName {
    app_downloaded,
    account_created,
    trial_activated,
    payment_success
}