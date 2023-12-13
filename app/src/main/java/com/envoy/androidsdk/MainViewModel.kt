package com.envoy.androidsdk

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.envoy.androidsdk.data.network.model.SdkConfig
import com.envoy.androidsdk.domain.model.CommonData
import com.envoy.androidsdk.domain.model.ContentSetting
import com.envoy.androidsdk.domain.model.ContentType
import com.envoy.androidsdk.domain.model.CreateLinkBody
import com.envoy.androidsdk.domain.shared.Failure
import com.envoy.androidsdk.domain.shared.Loading
import com.envoy.androidsdk.domain.shared.Success
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private val TAG = MainViewModel::class.java.name

class MainViewModel : ViewModel() {

    init {
        EnvoyApiProviderImpl.init(
            sdkConfig = SdkConfig(
                baseUrl = "https://dev-api.envoy.is/partner/",
                apiKey = "3UuCg2FdPX2ifbnLMrOtG5qoTgYGFAha4ylWpZMu"
            ),
            coroutineContext = Dispatchers.IO
        )
    }

    fun getButtonsState(): List<ButtonState> {
        val list = mutableListOf<ButtonState>()
        list.add(
            ButtonState(
                text = "Get Link",
                onClick = { getLink() }
            )
        )

        list.add(
            ButtonState(
                text = "Get Sandbox Link",
                onClick = { getSandboxLink() }
            )
        )
        return list
    }

    private fun getLink() {
        viewModelScope.launch {
            EnvoyApiProviderImpl.provide().createLink(
                body = CreateLinkBody(
                    contentSetting = ContentSetting(
                        type = ContentType.VIDEO,
                        name = "Content name",
                        description = "content description",
                        commonData = CommonData(
                            source = "example.com/media_url",
                            isRedirect = false,
                            poster = "example.com/image_url"
                        ),
                    ),
                    sharerId = "17234"
                )
            ).collect { resource ->
                when (resource) {
                    is Success -> {
                        Log.d(TAG, "Link: Success -> ${resource.value}")
                    }

                    is Loading -> {
                        Log.d(TAG, "Link: Loading")
                    }

                    is Failure -> {
                        Log.d(TAG, "Link: Failure -> ${resource.throwable.message}")
                    }
                }
            }
        }
    }

    private fun getSandboxLink() {
        viewModelScope.launch {
            EnvoyApiProviderImpl.provide().createSandboxLink(
                body = CreateLinkBody(
                    contentSetting = ContentSetting(
                        type = ContentType.VIDEO,
                        name = "Content name",
                        description = "content description",
                        commonData = CommonData(
                            source = "example.com/media_url",
                            isRedirect = false,
                            poster = "example.com/image_url"
                        ),
                    ),
                    sharerId = "17234"
                )
            ).collect { resource ->
                when (resource) {
                    is Success -> {
                        Log.d(TAG, "Sandbox link: Success -> ${resource.value}")
                    }

                    is Loading -> {
                        Log.d(TAG, "Sandbox link: Loading")
                    }

                    is Failure -> {
                        Log.d(TAG, "Sandbox link: Failure -> ${resource.throwable.message}")
                    }
                }
            }
        }
    }
}