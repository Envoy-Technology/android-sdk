package com.envoy.androidsdk.domain.usecase

import com.envoy.androidsdk.domain.EnvoyRepository
import com.envoy.androidsdk.domain.model.UserQuotaResponse
import com.envoy.androidsdk.domain.shared.Resource
import kotlinx.coroutines.flow.Flow

internal interface GetUserQuotaUseCase {
    operator fun invoke(userId: String): Flow<Resource<UserQuotaResponse>>
}

internal class GetUserQuotaUseCaseImpl(
    private val repository: EnvoyRepository
) : GetUserQuotaUseCase {
    override fun invoke(userId: String): Flow<Resource<UserQuotaResponse>> {
        return repository.getUserQuota(userId = userId)
    }
}