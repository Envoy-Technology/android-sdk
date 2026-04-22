package com.envoy.androidsdk.domain.usecase

import com.envoy.androidsdk.domain.EnvoyRepository
import com.envoy.androidsdk.domain.shared.Resource
import kotlinx.coroutines.flow.Flow

internal interface ClearManagedLinksUseCase {
    operator fun invoke(): Flow<Resource<Unit>>
}

internal class ClearManagedLinksUseCaseImpl(
    private val repository: EnvoyRepository
) : ClearManagedLinksUseCase {

    override fun invoke(): Flow<Resource<Unit>> {
        return repository.clearManagedLinks()
    }
}
