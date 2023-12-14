package com.envoy.androidsdk.domain

import com.envoy.androidsdk.domain.model.CreateLinkBody
import com.envoy.androidsdk.domain.model.CreateLinkResponse
import com.envoy.androidsdk.domain.model.CreateSandboxLinkResponse
import com.envoy.androidsdk.domain.model.UserQuotaResponse
import com.envoy.androidsdk.domain.shared.Resource
import kotlinx.coroutines.flow.Flow

internal interface EnvoyRepository {

    fun createLink(body: CreateLinkBody): Flow<Resource<CreateLinkResponse>>

    fun createSandboxLink(body: CreateLinkBody): Flow<Resource<CreateSandboxLinkResponse>>

    fun getUserQuota(userId: String): Flow<Resource<UserQuotaResponse>>
}