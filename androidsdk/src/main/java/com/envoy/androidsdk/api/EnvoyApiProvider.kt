package com.envoy.androidsdk.api

import com.envoy.androidsdk.data.network.model.SdkConfig
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers

internal interface EnvoyApiProvider {

    fun init(sdkConfig: SdkConfig, coroutineContext: CoroutineContext = Dispatchers.IO)

    fun provide(): EnvoyApi
}