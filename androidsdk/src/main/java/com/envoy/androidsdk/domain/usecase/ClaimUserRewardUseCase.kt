package com.envoy.androidsdk.domain.usecase

import com.envoy.androidsdk.domain.EnvoyRepository
import com.envoy.androidsdk.domain.model.ClaimUserRewardBody
import com.envoy.androidsdk.domain.model.ClaimUserRewardResponse
import com.envoy.androidsdk.domain.shared.Resource
import kotlinx.coroutines.flow.Flow

internal interface ClaimUserRewardUseCase {
    operator fun invoke(body: ClaimUserRewardBody): Flow<Resource<ClaimUserRewardResponse>>
}

internal class ClaimUserRewardUseCaseImpl(
    private val repository: EnvoyRepository
) : ClaimUserRewardUseCase {
    override fun invoke(body: ClaimUserRewardBody): Flow<Resource<ClaimUserRewardResponse>> {
        return repository.claimUserReward(body = body)
    }
}
