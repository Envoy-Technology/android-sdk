package com.envoy.androidsdk.domain

import com.envoy.androidsdk.domain.model.CreateLinkBody
import com.envoy.androidsdk.domain.model.CreateLinkResponse
import com.envoy.androidsdk.domain.shared.Resource
import kotlinx.coroutines.flow.Flow

internal interface EnvoyRepository {

    fun createLink(body: CreateLinkBody): Flow<Resource<CreateLinkResponse>>
}