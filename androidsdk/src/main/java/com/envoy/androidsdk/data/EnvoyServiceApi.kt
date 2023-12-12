package com.envoy.androidsdk.data

import com.envoy.androidsdk.domain.model.CreateLinkBody
import com.envoy.androidsdk.domain.model.CreateLinkResponse
import retrofit2.Response
import retrofit2.http.GET

internal interface EnvoyServiceApi {

    @GET("/create-link")
    suspend fun createLink(body: CreateLinkBody): Response<CreateLinkResponse>
}