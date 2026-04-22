package com.envoy.androidsdk.domain.model

import com.google.gson.annotations.SerializedName

data class ManageLinksRequest(
    @SerializedName("linkHashes") val linkHashes: List<String>
)
