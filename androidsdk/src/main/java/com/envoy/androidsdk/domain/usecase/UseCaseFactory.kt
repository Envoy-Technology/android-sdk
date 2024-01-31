package com.envoy.androidsdk.domain.usecase

import com.envoy.androidsdk.domain.EnvoyRepository
import com.envoy.androidsdk.domain.shared.HawkRepository

internal class UseCaseFactory(private val repository: EnvoyRepository) {

    private val createLinkUseCase: CreateLinkUseCase = CreateLinkUseCaseImpl(repository)
    private val getUserQuotaUseCase = GetUserQuotaUseCaseImpl(repository)
    private val getUserRewardsUseCase = GetUserRewardsUseCaseImpl(repository)
    private val claimUserRewardUseCase = ClaimUserRewardUseCaseImpl(repository)
    private val getCurrentRewardsUseCase = GetCurrentRewardsUseCaseImpl(repository)

    fun getCreateLinkUseCase(): CreateLinkUseCase {
        return createLinkUseCase
    }

    fun getUserQuotaUseCase(): GetUserQuotaUseCase {
        return getUserQuotaUseCase
    }

    fun getCreatePixelEventUseCase(hawkRepository: HawkRepository): CreatePixelEventUseCase {
        return CreatePixelEventUseCaseImpl(repository, hawkRepository)
    }

    fun getUserRewardsUseCase(): GetUserRewardsUseCase {
        return getUserRewardsUseCase
    }

    fun getClaimUserRewardUseCase(): ClaimUserRewardUseCase {
        return claimUserRewardUseCase
    }

    fun getUserCurrentRewardsUseCase(): GetCurrentRewardsUseCase {
        return getCurrentRewardsUseCase
    }
}
