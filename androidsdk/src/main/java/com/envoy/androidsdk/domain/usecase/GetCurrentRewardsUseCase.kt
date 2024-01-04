package com.envoy.androidsdk.domain.usecase

import com.envoy.androidsdk.domain.EnvoyRepository
import com.envoy.androidsdk.domain.model.UserCurrentRewardsResponse
import com.envoy.androidsdk.domain.shared.Resource
import kotlinx.coroutines.flow.Flow

internal interface GetCurrentRewardsUseCase {
    operator fun invoke(userId: String): Flow<Resource<UserCurrentRewardsResponse>>
}

internal class GetCurrentRewardsUseCaseImpl(
    private val repository: EnvoyRepository
) : GetCurrentRewardsUseCase {
    override fun invoke(userId: String): Flow<Resource<UserCurrentRewardsResponse>> {
        return repository.getUserCurrentRewards(userId = userId)
    }
}
