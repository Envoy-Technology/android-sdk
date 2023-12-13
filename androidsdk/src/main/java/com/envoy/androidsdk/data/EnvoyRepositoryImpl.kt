package com.envoy.androidsdk.data

import com.envoy.androidsdk.domain.EnvoyRepository
import com.envoy.androidsdk.domain.model.CreateLinkBody
import com.envoy.androidsdk.domain.model.CreateLinkResponse
import com.envoy.androidsdk.domain.model.CreateSandboxLinkResponse
import com.envoy.androidsdk.domain.shared.Resource
import com.envoy.androidsdk.domain.shared.performRequest
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.flow.Flow

internal class EnvoyRepositoryImpl(
    private val api: EnvoyServiceApi,
    private val coroutineContext: CoroutineContext
) : EnvoyRepository {
    override fun createLink(body: CreateLinkBody): Flow<Resource<CreateLinkResponse>> {
        return performRequest(
            {
                api.createLink(body = body)
            },
            coroutineContext,
        )
    }

    override fun createSandboxLink(body: CreateLinkBody): Flow<Resource<CreateSandboxLinkResponse>> {
        return performRequest(
            {
                api.createSandboxLink(body = body)
            },
            coroutineContext,
        )
    }
}