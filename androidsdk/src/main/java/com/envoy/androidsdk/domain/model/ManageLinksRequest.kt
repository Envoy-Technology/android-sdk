package com.envoy.androidsdk.domain.model

import com.google.gson.annotations.SerializedName

data class ManageLinksRequest(
    @SerializedName("links") val links: List<String>
)
