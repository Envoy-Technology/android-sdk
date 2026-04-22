package com.envoy.androidsdk.api

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

internal interface EnvoyApiProvider {

    fun init(apiKey: String, coroutineContext: CoroutineContext = Dispatchers.IO, context: Context)

    fun provide(): EnvoyApi
}
