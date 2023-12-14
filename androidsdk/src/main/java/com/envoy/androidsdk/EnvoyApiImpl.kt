package com.envoy.androidsdk

import com.envoy.androidsdk.api.EnvoyApi
import com.envoy.androidsdk.domain.model.CreateLinkBody
import com.envoy.androidsdk.domain.model.CreateLinkResponse
import com.envoy.androidsdk.domain.model.CreateSandboxLinkResponse
import com.envoy.androidsdk.domain.model.UserQuotaResponse
import com.envoy.androidsdk.domain.shared.Resource
import com.envoy.androidsdk.domain.usecase.CreateLinkUseCase
import com.envoy.androidsdk.domain.usecase.CreateSandboxLinkUseCase
import com.envoy.androidsdk.domain.usecase.GetUserQuotaUseCase
import kotlinx.coroutines.flow.Flow

internal class EnvoyApiImpl(
    private val createLinkUseCase: CreateLinkUseCase,
    private val createSandboxLinkUseCase: CreateSandboxLinkUseCase,
    private val getUserQuotaUseCase: GetUserQuotaUseCase
) : EnvoyApi {

    override fun createLink(body: CreateLinkBody): Flow<Resource<CreateLinkResponse>> {
        return createLinkUseCase.invoke(body = body)
    }

    override fun createSandboxLink(body: CreateLinkBody): Flow<Resource<CreateSandboxLinkResponse>> {
        return createSandboxLinkUseCase.invoke(body = body)
    }

    override fun getUserQuota(userId: String): Flow<Resource<UserQuotaResponse>> {
        return getUserQuotaUseCase.invoke(userId = userId)
    }
}