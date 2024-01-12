package com.envoy.androidsdk

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.envoy.androidsdk.domain.model.ClaimUserRewardBody
import com.envoy.androidsdk.domain.model.CommonData
import com.envoy.androidsdk.domain.model.ContentSetting
import com.envoy.androidsdk.domain.model.ContentType
import com.envoy.androidsdk.domain.model.CreateLinkBody
import com.envoy.androidsdk.domain.model.CreatePixelEventBody
import com.envoy.androidsdk.domain.model.EventName
import com.envoy.androidsdk.domain.model.VideoOrientation
import com.envoy.androidsdk.domain.shared.Failure
import com.envoy.androidsdk.domain.shared.Loading
import com.envoy.androidsdk.domain.shared.Success
import kotlinx.coroutines.launch

private val TAG = MainViewModel::class.java.name
private const val USER_ID = "123456"

class MainViewModel : ViewModel() {

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

        list.add(
            ButtonState(
                text = "Get User Quota",
                onClick = { getUserQuota() }
            )
        )

        list.add(
            ButtonState(
                text = "Create Pixel Event - App download",
                onClick = { getPixelEvent(EventName.app_downloaded.name) }
            )
        )

        list.add(
            ButtonState(
                text = "Create Pixel Event - Account created",
                onClick = { getPixelEvent(EventName.account_created.name) }
            )
        )

        list.add(
            ButtonState(
                text = "Create Pixel Event - Payment success",
                onClick = { getPixelEvent(EventName.payment_success.name) }
            )
        )

        list.add(
            ButtonState(
                text = "Create Pixel Event - Trial activated",
                onClick = { getPixelEvent(EventName.trial_activated.name) }
            )
        )

        list.add(
            ButtonState(
                text = "Create Pixel Event - custom event",
                onClick = { getPixelEvent() }
            )
        )

        list.add(
            ButtonState(
                text = "Get User Rewards",
                onClick = { getUserRewards() }
            )
        )

        list.add(
            ButtonState(
                text = "Claim User Reward",
                onClick = { claimUserReward() }
            )
        )

        list.add(
            ButtonState(
                text = "Get User Current Rewards",
                onClick = { getUserCurrentRewards() }
            )
        )

        return list
    }

    private fun getLink() {
        viewModelScope.launch {
            EnvoyApiProviderImpl.provide().createLink(
                body = CreateLinkBody(
                    contentSetting = ContentSetting(
                        type = ContentType.HTML_PLAIN,
                        name = "Content name",
                        description = "content description",
                        commonData = CommonData(
                            source = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                            isRedirect = false,
                            poster = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
                        ),
                        videoOrientation = VideoOrientation.vertical
                    ),
                    sharerId = USER_ID
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
                            source = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                            isRedirect = false,
                            poster = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                        ),
                        videoOrientation = VideoOrientation.vertical
                    ),
                    sharerId = USER_ID
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

    private fun getUserQuota() {
        viewModelScope.launch {
            EnvoyApiProviderImpl.provide().getUserQuota(
                userId = USER_ID
            ).collect { resource ->
                when (resource) {
                    is Success -> {
                        Log.d(TAG, "User quota: Success -> ${resource.value}")
                    }

                    is Loading -> {
                        Log.d(TAG, "User quota: Loading")
                    }

                    is Failure -> {
                        Log.d(TAG, "User quota: Failure -> ${resource.throwable.message}")
                    }
                }
            }
        }
    }

    private fun getPixelEvent(pixelEvent: String? = null) {
        viewModelScope.launch {
            EnvoyApiProviderImpl.provide().createPixelEvent(
                body = CreatePixelEventBody(
                    eventName = pixelEvent ?: "androidCustomEvent"
                )
            ).collect { resource ->
                when (resource) {
                    is Success -> {
                        Log.d(TAG, "Pixel Event: Success -> ${resource.value}")
                    }

                    is Loading -> {
                        Log.d(TAG, "Pixel Event: Loading")
                    }

                    is Failure -> {
                        Log.d(TAG, "Pixel Event: Failure -> ${resource.throwable.message}")
                    }
                }
            }
        }
    }

    private fun getUserRewards() {
        viewModelScope.launch {
            EnvoyApiProviderImpl.provide().getUserReward(
                userId = USER_ID
            ).collect { resource ->
                when (resource) {
                    is Success -> {
                        Log.d(TAG, "User rewards: Success -> ${resource.value}")
                    }

                    is Loading -> {
                        Log.d(TAG, "User rewards: Loading")
                    }

                    is Failure -> {
                        Log.d(TAG, "User rewards: Failure -> ${resource.throwable.message}")
                    }
                }
            }
        }
    }

    private fun claimUserReward() {
        viewModelScope.launch {
            EnvoyApiProviderImpl.provide().claimUserReward(
                body = ClaimUserRewardBody(
                    userId = USER_ID
                )
            ).collect { resource ->
                when (resource) {
                    is Success -> {
                        Log.d(TAG, "Claim user reward: Success -> ${resource.value}")
                    }

                    is Loading -> {
                        Log.d(TAG, "Claim user reward: Loading")
                    }

                    is Failure -> {
                        Log.d(TAG, "Claim user reward: Failure -> ${resource.throwable.message}")
                    }
                }
            }
        }
    }

    private fun getUserCurrentRewards() {
        viewModelScope.launch {
            EnvoyApiProviderImpl.provide().getUserCurrentRewards(
                userId = USER_ID
            ).collect { resource ->
                when (resource) {
                    is Success -> {
                        Log.d(TAG, "User current rewards: Success -> ${resource.value}")
                    }

                    is Loading -> {
                        Log.d(TAG, "User current rewards: Loading")
                    }

                    is Failure -> {
                        Log.d(TAG, "User current rewards: Failure -> ${resource.throwable.message}")
                    }
                }
            }
        }
    }
}
