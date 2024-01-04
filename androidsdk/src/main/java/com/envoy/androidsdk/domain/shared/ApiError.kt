package com.envoy.androidsdk.domain.shared

import com.google.gson.annotations.SerializedName

data class ApiError(
    @SerializedName("detail") val details: List<ApiErrorData>
)

data class ApiErrorData(
    @SerializedName("msg") val message: String = "",
    @SerializedName("type") val type: String = "",
    @SerializedName("loc") val errors: List<String>?
)
