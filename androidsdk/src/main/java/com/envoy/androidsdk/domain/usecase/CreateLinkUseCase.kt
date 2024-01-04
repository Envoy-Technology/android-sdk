package com.envoy.androidsdk.domain.usecase

import com.envoy.androidsdk.domain.EnvoyRepository
import com.envoy.androidsdk.domain.model.CreateLinkBody
import com.envoy.androidsdk.domain.model.CreateLinkResponse
import com.envoy.androidsdk.domain.shared.Resource
import kotlinx.coroutines.flow.Flow

internal interface CreateLinkUseCase {
    operator fun invoke(body: CreateLinkBody): Flow<Resource<CreateLinkResponse>>
}

internal class CreateLinkUseCaseImpl(
    private val repository: EnvoyRepository
) : CreateLinkUseCase {

    override fun invoke(body: CreateLinkBody): Flow<Resource<CreateLinkResponse>> {
        return repository.createLink(body = body)
    }
}
