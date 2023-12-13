package com.envoy.androidsdk.data

import com.envoy.androidsdk.domain.model.CreateLinkBody
import com.envoy.androidsdk.domain.model.CreateLinkResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

internal interface EnvoyServiceApi {

    @POST("create-link")
    suspend fun createLink(@Body body: CreateLinkBody): Response<CreateLinkResponse>
}