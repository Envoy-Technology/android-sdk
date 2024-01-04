package com.envoy.androidsdk.data

import com.envoy.androidsdk.domain.model.ClaimUserRewardBody
import com.envoy.androidsdk.domain.model.ClaimUserRewardResponse
import com.envoy.androidsdk.domain.model.CreateLinkBody
import com.envoy.androidsdk.domain.model.CreateLinkResponse
import com.envoy.androidsdk.domain.model.CreatePixelEventBody
import com.envoy.androidsdk.domain.model.CreateSandboxLinkResponse
import com.envoy.androidsdk.domain.model.GetUserRewardResponse
import com.envoy.androidsdk.domain.model.UserCurrentRewardsResponse
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

    @POST("pixel-event")
    suspend fun createPixelEvent(@Body body: CreatePixelEventBody): Response<Unit>

    @GET("user-rewards/{user_id}")
    suspend fun getUserRewards(@Path("user_id") userId: String): Response<GetUserRewardResponse>

    @POST("user-rewards")
    suspend fun claimUserRewards(@Body body: ClaimUserRewardBody): Response<ClaimUserRewardResponse>

    @GET("user-current-rewards/{user_id}")
    suspend fun getUserCurrentRewards(@Path("user_id") userId: String): Response<UserCurrentRewardsResponse>
}
