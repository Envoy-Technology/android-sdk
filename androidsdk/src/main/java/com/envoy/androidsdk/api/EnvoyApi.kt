package com.envoy.androidsdk.api

import com.envoy.androidsdk.data.network.model.SdkConfig
import com.envoy.androidsdk.domain.model.CreateLinkBody
import com.envoy.androidsdk.domain.model.CreateLinkResponse
import com.envoy.androidsdk.domain.model.CreateSandboxLinkResponse
import com.envoy.androidsdk.domain.shared.Resource
import kotlinx.coroutines.flow.Flow

interface EnvoyApi {

    fun createLink(body: CreateLinkBody): Flow<Resource<CreateLinkResponse>>

    fun createSandboxLink(body: CreateLinkBody): Flow<Resource<CreateSandboxLinkResponse>>
}