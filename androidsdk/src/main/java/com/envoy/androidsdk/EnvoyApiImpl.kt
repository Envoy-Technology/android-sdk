package com.envoy.androidsdk

import com.envoy.androidsdk.api.EnvoyApi
import com.envoy.androidsdk.domain.model.ClaimUserRewardBody
import com.envoy.androidsdk.domain.model.ClaimUserRewardResponse
import com.envoy.androidsdk.domain.model.CreateLinkBody
import com.envoy.androidsdk.domain.model.CreateLinkResponse
import com.envoy.androidsdk.domain.model.CreatePixelEventBody
import com.envoy.androidsdk.domain.model.CreateSandboxLinkResponse
import com.envoy.androidsdk.domain.model.GetUserRewardResponse
import com.envoy.androidsdk.domain.model.UserCurrentRewardsResponse
import com.envoy.androidsdk.domain.model.UserQuotaResponse
import com.envoy.androidsdk.domain.shared.Resource
import com.envoy.androidsdk.domain.usecase.ClaimUserRewardUseCase
import com.envoy.androidsdk.domain.usecase.CreateLinkUseCase
import com.envoy.androidsdk.domain.usecase.CreatePixelEventUseCase
import com.envoy.androidsdk.domain.usecase.CreateSandboxLinkUseCase
import com.envoy.androidsdk.domain.usecase.GetUserQuotaUseCase
import com.envoy.androidsdk.domain.usecase.GetUserRewardsUseCase
import com.envoy.androidsdk.domain.usecase.GetCurrentRewardsUseCase
import kotlinx.coroutines.flow.Flow

internal class EnvoyApiImpl(
    private val createLinkUseCase: CreateLinkUseCase,
    private val createSandboxLinkUseCase: CreateSandboxLinkUseCase,
    private val getUserQuotaUseCase: GetUserQuotaUseCase,
    private val createPixelEventUseCase: CreatePixelEventUseCase,
    private val getUserRewardsUseCase: GetUserRewardsUseCase,
    private val claimUserRewardUseCase: ClaimUserRewardUseCase,
    private val getCurrentRewardsUseCase: GetCurrentRewardsUseCase
) : EnvoyApi {

    override fun createLink(body: CreateLinkBody): Flow<Resource<CreateLinkResponse>> {
        return createLinkUseCase.invoke(body = body)
    }

    override fun createSandboxLink(body: CreateLinkBody): Flow<Resource<CreateSandboxLinkResponse>> {
        return createSandboxLinkUseCase.invoke(body = body)
    }

    override fun getUserQuota(userId: String): Flow<Resource<UserQuotaResponse>> {
        return getUserQuotaUseCase.invoke(userId = userId)
    }

    override fun createPixelEvent(
        body: CreatePixelEventBody
    ): Flow<Resource<Unit>> {
        return createPixelEventUseCase.invoke(body = body)
    }

    override fun getUserReward(userId: String): Flow<Resource<GetUserRewardResponse>> {
        return getUserRewardsUseCase.invoke(userId = userId)
    }

    override fun claimUserReward(body: ClaimUserRewardBody): Flow<Resource<ClaimUserRewardResponse>> {
        return claimUserRewardUseCase.invoke(body = body)
    }

    override fun getUserCurrentRewards(userId: String): Flow<Resource<UserCurrentRewardsResponse>> {
        return getCurrentRewardsUseCase.invoke(userId = userId)
    }
}
