package com.envoy.androidsdk

import android.util.Log
import com.envoy.androidsdk.api.EnvoyApi
import com.envoy.androidsdk.api.EnvoyApiProvider
import com.envoy.androidsdk.data.EnvoyRepositoryImpl
import com.envoy.androidsdk.data.EnvoyServiceApi
import com.envoy.androidsdk.data.network.RetrofitFactory
import com.envoy.androidsdk.data.network.RetrofitFactoryImpl
import com.envoy.androidsdk.data.network.model.SdkConfig
import com.envoy.androidsdk.domain.EnvoyRepository
import com.envoy.androidsdk.domain.shared.InitException
import com.envoy.androidsdk.domain.usecase.CreateLinkUseCase
import com.envoy.androidsdk.domain.usecase.CreateLinkUseCaseImpl
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow

private val TAG = EnvoyApiProviderImpl::class.java.name

object EnvoyApiProviderImpl : EnvoyApiProvider {

    private var envoyApi: EnvoyApi? = null

    private lateinit var retrofitFactory: RetrofitFactory
    private lateinit var coroutineContext: CoroutineContext
    private lateinit var repository: EnvoyRepository
    private lateinit var createLinkUseCase: CreateLinkUseCase

    private val isInitialized = MutableStateFlow(false)
    override fun init(sdkConfig: SdkConfig, coroutineContext: CoroutineContext) {
        if (!isInitialized.value) {
            if (sdkConfig.apiKey.isEmpty() || sdkConfig.baseUrl.isEmpty()) {
                throw InitException("Envoy SDK initialisation exception: Please provide valid apiKey")
            }
            retrofitFactory = RetrofitFactoryImpl()
            retrofitFactory.setConfiguration(sdkConfig)
            this.coroutineContext = coroutineContext
            repository = EnvoyRepositoryImpl(
                api = retrofitFactory.getInstance(EnvoyServiceApi::class.java),
                coroutineContext = EnvoyApiProviderImpl.coroutineContext
            )
            createLinkUseCase = CreateLinkUseCaseImpl(repository)
            isInitialized.value = true
        } else {
            Log.w(TAG, "Envoy SDK is already initialized")
        }
    }

    override fun provide(): EnvoyApi {
        if (!isInitialized.value) {
            throw InitException("Envoy SDK is not initialized")
        }

        return if (envoyApi == null) {
            envoyApi = EnvoyApiImpl(createLinkUseCase)
            envoyApi as EnvoyApi
        } else {
            envoyApi as EnvoyApi
        }
    }
}