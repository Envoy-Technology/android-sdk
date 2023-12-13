package com.envoy.androidsdk

import com.envoy.androidsdk.api.EnvoyApi
import com.envoy.androidsdk.domain.model.CreateLinkBody
import com.envoy.androidsdk.domain.model.CreateLinkResponse
import com.envoy.androidsdk.domain.shared.Resource
import com.envoy.androidsdk.domain.usecase.CreateLinkUseCase
import kotlinx.coroutines.flow.Flow

internal class EnvoyApiImpl(
    private val createLinkUseCase: CreateLinkUseCase
) : EnvoyApi {

    override fun createLink(body: CreateLinkBody): Flow<Resource<CreateLinkResponse>> {
        return createLinkUseCase.invoke(body = body)
    }
}