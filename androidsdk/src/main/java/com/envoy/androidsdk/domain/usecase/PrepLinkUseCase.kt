package com.envoy.androidsdk.domain.usecase

import com.envoy.androidsdk.domain.EnvoyRepository
import com.envoy.androidsdk.domain.model.PrepLinkRequest
import com.envoy.androidsdk.domain.shared.Resource
import kotlinx.coroutines.flow.Flow

internal interface PrepLinkUseCase {
    operator fun invoke(body: PrepLinkRequest): Flow<Resource<Unit>>
}

internal class PrepLinkUseCaseImpl(
    private val repository: EnvoyRepository
) : PrepLinkUseCase {

    override fun invoke(body: PrepLinkRequest): Flow<Resource<Unit>> {
        return repository.prepLink(body = body)
    }
}
