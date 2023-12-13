package com.envoy.androidsdk.domain.usecase

import com.envoy.androidsdk.domain.EnvoyRepository
import com.envoy.androidsdk.domain.model.CreateLinkBody
import com.envoy.androidsdk.domain.model.CreateSandboxLinkResponse
import com.envoy.androidsdk.domain.shared.Resource
import kotlinx.coroutines.flow.Flow

internal interface CreateSandboxLinkUseCase {
    operator fun invoke(body: CreateLinkBody): Flow<Resource<CreateSandboxLinkResponse>>
}

internal class CreateSandboxLinkUseCaseImpl(
    private val repository: EnvoyRepository
) : CreateSandboxLinkUseCase {
    override fun invoke(body: CreateLinkBody): Flow<Resource<CreateSandboxLinkResponse>> {
        return repository.createSandboxLink(body = body)
    }

}