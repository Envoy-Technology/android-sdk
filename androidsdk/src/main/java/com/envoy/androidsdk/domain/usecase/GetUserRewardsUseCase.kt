package com.envoy.androidsdk.domain.usecase

import com.envoy.androidsdk.domain.EnvoyRepository
import com.envoy.androidsdk.domain.model.GetUserRewardResponse
import com.envoy.androidsdk.domain.shared.Resource
import kotlinx.coroutines.flow.Flow

internal interface GetUserRewardsUseCase {
    operator fun invoke(userId: String): Flow<Resource<GetUserRewardResponse>>
}

internal class GetUserRewardsUseCaseImpl(
    private val repository: EnvoyRepository
) : GetUserRewardsUseCase {
    override fun invoke(userId: String): Flow<Resource<GetUserRewardResponse>> {
        return repository.getUserRewards(userId = userId)
    }
}
