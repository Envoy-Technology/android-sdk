package com.envoy.androidsdk.domain.usecase

import com.envoy.androidsdk.domain.EnvoyRepository
import com.envoy.androidsdk.domain.model.ManageLinksRequest
import com.envoy.androidsdk.domain.shared.Resource
import kotlinx.coroutines.flow.Flow

internal interface ManageLinksUseCase {
    operator fun invoke(body: ManageLinksRequest): Flow<Resource<Unit>>
}

internal class ManageLinksUseCaseImpl(
    private val repository: EnvoyRepository
) : ManageLinksUseCase {

    override fun invoke(body: ManageLinksRequest): Flow<Resource<Unit>> {
        return repository.manageLinks(body = body)
    }
}
