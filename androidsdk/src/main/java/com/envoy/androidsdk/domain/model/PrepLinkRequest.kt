package com.envoy.androidsdk.domain.model

import com.google.gson.annotations.SerializedName

/**
* @param url Content URL
*/

data class PrepLinkRequest(
    @SerializedName("url") val url: String,
)
