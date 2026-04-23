package com.envoy.androidsdk

import android.graphics.Bitmap
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
import com.envoy.androidsdk.domain.model.PrepLinkRequest
import com.envoy.androidsdk.domain.model.VideoOrientation
import com.envoy.androidsdk.domain.shared.Failure
import com.envoy.androidsdk.domain.shared.Loading
import com.envoy.androidsdk.domain.shared.Success
import com.envoy.androidsdk.screenshot.ScreenshotLinkHelper
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

        list.add(
            ButtonState(
                text = "Prep Link",
                onClick = { prepLink() }
            )
        )

        list.add(
            ButtonState(
                text = "Create Carousel Links & Manage Links",
                onClick = { createCarouselLinksAndManage() }
            )
        )

        list.add(
            ButtonState(
                text = "Clear Managed Links",
                onClick = { clearManagedLinks() }
            )
        )

        return list
    }

    private fun getLink() {
        viewModelScope.launch {
            EnvoyApiProviderImpl.provide().createLink(
                body = CreateLinkBody(
                    contentSetting = ContentSetting(
                        type = ContentType.AUDIO,
                        name = "Content name",
                        description = "content description",
                        commonData = CommonData(
                            source = "https://commondatastorage.googleapis.com/codeskulptor-demos/pyman_assets/theygotcha.ogg",
                            isRedirect = false,
                            poster = "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4"
                        ),
                        videoOrientation = VideoOrientation.VERTICAL
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

    private fun prepLink() {
        viewModelScope.launch {
            EnvoyApiProviderImpl.provide().prepLink(
                body = PrepLinkRequest(
                    url = "https://grokipedia.com/page/Elon_Musk"
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

    private fun createCarouselLinksAndManage() {
        viewModelScope.launch {
            Log.d(TAG, "Creating 2 carousel links...")

            // Create first carousel link
            val link1Result = runCatching {
                var url1: String? = null
                EnvoyApiProviderImpl.provide().createLink(
                    body = CreateLinkBody(
                        contentSetting = ContentSetting(
                            type = ContentType.AUDIO,
                            name = "Carousel Link 1",
                            description = "First carousel link",
                            commonData = CommonData(
                                source = "https://commondatastorage.googleapis.com/codeskulptor-demos/pyman_assets/theygotcha.ogg",
                                isRedirect = false,
                                poster = "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4"
                            ),
                            videoOrientation = VideoOrientation.VERTICAL
                        ),
                        sharerId = USER_ID,
                        isCarouselLink = true
                    )
                ).collect { resource ->
                    when (resource) {
                        is Success -> {
                            Log.d(TAG, "Carousel Link 1: Success -> ${resource.value}")
                            url1 = resource.value.url
                        }

                        is Loading -> {
                            Log.d(TAG, "Carousel Link 1: Loading")
                        }

                        is Failure -> {
                            Log.d(TAG, "Carousel Link 1: Failure -> ${resource.throwable.message}")
                        }
                    }
                }
                url1
            }.getOrNull()

            // Create second carousel link
            val link2Result = runCatching {
                var url2: String? = null
                EnvoyApiProviderImpl.provide().createLink(
                    body = CreateLinkBody(
                        contentSetting = ContentSetting(
                            type = ContentType.AUDIO,
                            name = "Carousel Link 2",
                            description = "Second carousel link",
                            commonData = CommonData(
                                source = "https://commondatastorage.googleapis.com/codeskulptor-demos/pyman_assets/theygotcha.ogg",
                                isRedirect = false,
                                poster = "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4"
                            ),
                            videoOrientation = VideoOrientation.VERTICAL
                        ),
                        sharerId = USER_ID,
                        isCarouselLink = true
                    )
                ).collect { resource ->
                    when (resource) {
                        is Success -> {
                            Log.d(TAG, "Carousel Link 2: Success -> ${resource.value}")
                            url2 = resource.value.url
                        }

                        is Loading -> {
                            Log.d(TAG, "Carousel Link 2: Loading")
                        }

                        is Failure -> {
                            Log.d(TAG, "Carousel Link 2: Failure -> ${resource.throwable.message}")
                        }
                    }
                }
                url2
            }.getOrNull()

            // Manage links if both URLs are available
            val urls = listOfNotNull(link1Result, link2Result)
            if (urls.isNotEmpty()) {
                Log.d(TAG, "Managing links with urls: $urls")
                manageLinks(urls)
            } else {
                Log.d(TAG, "Could not create carousel links, skipping manage-links")
            }
        }
    }

    private suspend fun manageLinks(links: List<String>) {
        EnvoyApiProviderImpl.provide().manageLinks(
            body = com.envoy.androidsdk.domain.model.ManageLinksRequest(
                links = links
            )
        ).collect { resource ->
            when (resource) {
                is Success -> {
                    Log.d(TAG, "Manage links: Success")
                }

                is Loading -> {
                    Log.d(TAG, "Manage links: Loading")
                }

                is Failure -> {
                    Log.d(TAG, "Manage links: Failure -> ${resource.throwable.message}")
                }
            }
        }
    }

    private fun clearManagedLinks() {
        viewModelScope.launch {
            EnvoyApiProviderImpl.provide().clearManagedLinks().collect { resource ->
                when (resource) {
                    is Success -> {
                        Log.d(TAG, "Clear managed links: Success")
                    }

                    is Loading -> {
                        Log.d(TAG, "Clear managed links: Loading")
                    }

                    is Failure -> {
                        Log.d(TAG, "Clear managed links: Failure -> ${resource.throwable.message}")
                    }
                }
            }
        }
    }

    fun createScreenshotLink(bitmap: Bitmap) {
        viewModelScope.launch {
            val linkBody = ScreenshotLinkHelper.createScreenshotLinkBody(
                bitmap = bitmap,
                sharerId = USER_ID,
                contentName = "Screenshot",
                contentDescription = "Shared screenshot from Android app"
            )

            EnvoyApiProviderImpl.provide().createLink(body = linkBody).collect { resource ->
                when (resource) {
                    is Success -> {
                        Log.d(TAG, "Screenshot Link: Success -> ${resource.value}")
                    }

                    is Loading -> {
                        Log.d(TAG, "Screenshot Link: Loading")
                    }

                    is Failure -> {
                        Log.d(TAG, "Screenshot Link: Failure -> ${resource.throwable.message}")
                    }
                }
            }
        }
    }
}
