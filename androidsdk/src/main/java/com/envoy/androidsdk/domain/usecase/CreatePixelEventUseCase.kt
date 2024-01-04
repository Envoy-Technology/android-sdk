package com.envoy.androidsdk.domain.usecase

import com.envoy.androidsdk.domain.EnvoyRepository
import com.envoy.androidsdk.domain.model.CreatePixelEventBody
import com.envoy.androidsdk.domain.shared.HawkRepository
import com.envoy.androidsdk.domain.shared.Resource
import kotlinx.coroutines.flow.Flow

internal interface CreatePixelEventUseCase {
    operator fun invoke(body: CreatePixelEventBody): Flow<Resource<Unit>>
}

internal class CreatePixelEventUseCaseImpl(
    private val repository: EnvoyRepository,
    private val hawkRepository: HawkRepository
) : CreatePixelEventUseCase {

    override fun invoke(body: CreatePixelEventBody): Flow<Resource<Unit>> {
        val newBody = body.copy(
            shareLinkHash = hawkRepository.getHash()
        )
        return repository.createPixelEvent(body = newBody)
    }
}
