package com.envoy.androidsdk

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.envoy.androidsdk.data.network.model.SdkConfig
import com.envoy.androidsdk.domain.model.CommonData
import com.envoy.androidsdk.domain.model.ContentSetting
import com.envoy.androidsdk.domain.model.CreateLinkBody
import com.envoy.androidsdk.domain.model.Label
import com.envoy.androidsdk.domain.model.LifespanClick
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
                apiKey = "your_api_key"
            ),
            coroutineContext = Dispatchers.IO
        )
    }

    fun getLink() {
        viewModelScope.launch {
            EnvoyApiProviderImpl.provide().createLink(
                body = CreateLinkBody(
                    autoPlay = false,
                    contentSetting = ContentSetting(
                        type = "VIDEO",
                        name = "Content name",
                        description = "content description",
                        commonData = CommonData(
                            source = "example.com/media_url",
                            isRedirect = false,
                            poster = "example.com/image_url"
                        ),
                        timeLimit = 0,
                        timeStart = 0,
                        availableFrom = "2023-12-12T13:58:20.505Z",
                        availableTo = "2023-12-12T13:58:20.505Z",
                        videoOrientation = 1,
                        previewTitle = "Preview title",
                        previewOrientation = "Landscape",
                        previewImage = "example.com/image_url",
                        isSandbox = false,
                        isEmailMandatory = true,
                        modalTitle = "Enter email",
                        buttonText = "Enter your email to unlock the gift content",
                        contentProtection = CommonData(
                            source = "example.com/media_url",
                            isRedirect = false,
                            poster = "example.com/image_url"
                        )
                    ),
                    lifespanClick = LifespanClick(value = 4, unit = "minutes"),
                    openQuota = 3,
                    title = "Another tile",
                    sharerId = "123",
                    isSandbox = false,
                    labels = listOf(
                        Label(
                            value = 21,
                            text = "Label text",
                            color = "FFFFFF"
                        )
                    )
                )
            ).collect { resource ->
                when (resource) {
                    is Success -> {
                        Log.d(TAG, "Success -> ${resource.value}")
                    }

                    is Loading -> {
                        Log.d(TAG, "Loading")
                    }

                    is Failure -> {
                        Log.d(TAG, "Failure -> ${resource.throwable.message}")
                    }
                }
            }
        }
    }
}