package com.envoy.androidsdk.domain.usecase

import com.envoy.androidsdk.deferredlink.DeferredLinkCoordinator
import com.envoy.androidsdk.domain.EnvoyRepository
import com.envoy.androidsdk.domain.model.CreatePixelEventBody
import com.envoy.androidsdk.domain.shared.Loading
import com.envoy.androidsdk.domain.shared.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow

internal interface CreatePixelEventUseCase {
    operator fun invoke(body: CreatePixelEventBody): Flow<Resource<Unit>>
}

internal class CreatePixelEventUseCaseImpl(
    private val repository: EnvoyRepository,
    private val deferredLinks: DeferredLinkCoordinator
) : CreatePixelEventUseCase {

    override fun invoke(body: CreatePixelEventBody): Flow<Resource<Unit>> = flow {
        emit(Loading())
        // Held while a deferred-link check runs; the attribution is read when the event is sent, so it carries its result.
        deferredLinks.awaitPixelEventRelease()
        val attribution = deferredLinks.storedAttribution()
        val newBody = body.copy(
            shareLinkHash = attribution.shareLinkHash,
            leadUuid = attribution.leadUuid
        )
        emitAll(repository.createPixelEvent(body = newBody).filter { it !is Loading })
    }
}
