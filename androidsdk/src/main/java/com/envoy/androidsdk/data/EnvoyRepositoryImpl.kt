package com.envoy.androidsdk.data

import com.envoy.androidsdk.domain.EnvoyRepository
import com.envoy.androidsdk.domain.model.ClaimUserRewardBody
import com.envoy.androidsdk.domain.model.ClaimUserRewardResponse
import com.envoy.androidsdk.domain.model.CreateLinkBody
import com.envoy.androidsdk.domain.model.CreateLinkResponse
import com.envoy.androidsdk.domain.model.CreatePixelEventBody
import com.envoy.androidsdk.domain.model.GetUserRewardResponse
import com.envoy.androidsdk.domain.model.UserCurrentRewardsResponse
import com.envoy.androidsdk.domain.model.UserQuotaResponse
import com.envoy.androidsdk.domain.shared.Resource
import com.envoy.androidsdk.domain.shared.performRequest
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.CoroutineContext

internal class EnvoyRepositoryImpl(
    private val api: EnvoyServiceApi,
    private val coroutineContext: CoroutineContext
) : EnvoyRepository {
    override fun createLink(body: CreateLinkBody): Flow<Resource<CreateLinkResponse>> {
        return performRequest(
            {
                api.createLink(body = body)
            },
            coroutineContext
        )
    }

    override fun getUserQuota(userId: String): Flow<Resource<UserQuotaResponse>> {
        return performRequest(
            {
                api.getUserQuota(userId = userId)
            },
            coroutineContext
        )
    }

    override fun createPixelEvent(body: CreatePixelEventBody): Flow<Resource<Unit>> {
        return performRequest(
            {
                api.createPixelEvent(body = body)
            },
            coroutineContext
        )
    }

    override fun getUserRewards(userId: String): Flow<Resource<GetUserRewardResponse>> {
        return performRequest(
            {
                api.getUserRewards(userId = userId)
            },
            coroutineContext
        )
    }

    override fun claimUserReward(body: ClaimUserRewardBody): Flow<Resource<ClaimUserRewardResponse>> {
        return performRequest(
            {
                api.claimUserRewards(body = body)
            },
            coroutineContext
        )
    }

    override fun getUserCurrentRewards(userId: String): Flow<Resource<UserCurrentRewardsResponse>> {
        return performRequest(
            {
                api.getUserCurrentRewards(userId = userId)
            },
            coroutineContext
        )
    }
}
