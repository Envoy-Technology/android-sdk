package com.envoy.androidsdk.api

import com.envoy.androidsdk.data.network.model.SdkConfig
import kotlin.coroutines.CoroutineContext

internal interface EnvoyApiProvider {

    fun init(sdkConfig: SdkConfig, coroutineContext: CoroutineContext)

    fun provide(): EnvoyApi
}