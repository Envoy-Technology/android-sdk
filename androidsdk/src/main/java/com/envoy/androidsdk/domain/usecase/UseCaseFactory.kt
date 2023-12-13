package com.envoy.androidsdk.domain.usecase

import com.envoy.androidsdk.domain.EnvoyRepository

internal class UseCaseFactory(repository: EnvoyRepository) {

    private val createLinkUseCase: CreateLinkUseCase = CreateLinkUseCaseImpl(repository)
    private val createSandboxLinkUseCase: CreateSandboxLinkUseCase =
        CreateSandboxLinkUseCaseImpl(repository)

    fun getCreateLinkUseCase(): CreateLinkUseCase {
        return createLinkUseCase
    }

    fun getCreateSandboxLinkUseCase(): CreateSandboxLinkUseCase {
        return createSandboxLinkUseCase
    }

}