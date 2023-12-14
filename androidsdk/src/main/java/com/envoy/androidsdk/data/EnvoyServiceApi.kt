package com.envoy.androidsdk.data

import com.envoy.androidsdk.domain.model.CreateLinkBody
import com.envoy.androidsdk.domain.model.CreateLinkResponse
import com.envoy.androidsdk.domain.model.CreateSandboxLinkResponse
import com.envoy.androidsdk.domain.model.UserQuotaResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

internal interface EnvoyServiceApi {

    @POST("create-link")
    suspend fun createLink(@Body body: CreateLinkBody): Response<CreateLinkResponse>

    @POST("create-sandbox-link")
    suspend fun createSandboxLink(@Body body: CreateLinkBody): Response<CreateSandboxLinkResponse>

    @GET("user-quota/{user_id}")
    suspend fun getUserQuota(@Path("user_id") userId: String): Response<UserQuotaResponse>
}