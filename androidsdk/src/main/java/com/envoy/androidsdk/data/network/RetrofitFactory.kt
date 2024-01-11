package com.envoy.androidsdk.data.network

import com.envoy.androidsdk.data.network.model.SdkConfig

internal interface RetrofitFactory {

    fun <T : Any> getInstance(className: Class<T>): T

    fun setConfiguration(config: SdkConfig)
}
