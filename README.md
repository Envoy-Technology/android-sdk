<a name="introduction"></a>
# Overview

Envoy Android SDK aimed to deliver a fast solution to share content out of your Android Application.

## Add Envoy SDK to Your Application

### Requirements
* minSdkVersion: 26 (Android 8.0)
* compileSdkVersion: 34 (Android 13)

### Integration

In your project's settings.gradle, add the code snipet below:
```gradle
dependencyResolutionManagement {
		repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
		repositories {
			mavenCentral()
			maven { url 'https://jitpack.io' }
		}
	}
```

In the app module's build.gradle, besides whatever other dependencies you already have, include such an "implementation" line:

```gradle
dependencies {
    implementation 'com.github.Envoy-Technology:android-sdk:1.0.4' // replace "1.0.4" with the version you want to include
}
```

### Android manifest

The following permission is used in the Envoy library. You should include them in the Android manifest:

```
<uses-permission android:name="android.permission.INTERNET" />
```


## Usage example
To initialize the library, you should call the init() method. You can get your project api key from [account settings](https://dev-platform.envoy.is/dashboard/account/).
```kotlin
EnvoyApiProviderImpl.init(
                        apiKey = "api_key",
                        context = applicationContext
                    )
}
```

For every api request, you have to get the EnvoyApi with `EnvoyApiProviderImpl.provide`. Also, every request should be called from a `CoroutineScope` (the below examples are all called from a `viewModelScope`, but you can change that to your needs). The user id should be replaced by your user's id.

### Create link

Request is pretty flexible and have both required and optional values.

```kotlin
data class CreateLinkBody(
    @SerializedName("autoplay") val autoPlay: Boolean = false,
    @SerializedName("content_setting") val contentSetting: ContentSetting,
    @SerializedName("lifespan_after_click") val lifespanClick: LifespanClick? = null,
    @SerializedName("open_quota") val openQuota: Int? = null,
    @SerializedName("extra") val extra: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("sharer_id") val sharerId: String,
    @SerializedName("is_sandbox") val isSandbox: Boolean = false,
    @SerializedName("labels") val labels: List<Label>? = null
)

data class ContentSetting(
    @SerializedName("content_type") val type: ContentType,
    @SerializedName("content_name") val name: String,
    @SerializedName("content_description") val description: String,
    @SerializedName("common") val commonData: CommonData,
    @SerializedName("time_limit") val timeLimit: Int? = null,
    @SerializedName("time_start") val timeStart: Int? = null,
    @SerializedName("available_from") val availableFrom: String? = null,
    @SerializedName("available_to") val availableTo: String? = null,
    @SerializedName("video_orientation") val videoOrientation: VideoOrientation,
    @SerializedName("preview_title") val previewTitle: String? = null,
    @SerializedName("preview_description") val previewOrientation: String? = null,
    @SerializedName("preview_image") val previewImage: String? = null,
    @SerializedName("is_sandbox") val isSandbox: Boolean = false,
    @SerializedName("mandatory_email") val isEmailMandatory: Boolean = false,
    @SerializedName("modal_title") val modalTitle: String? = null,
    @SerializedName("button_text") val buttonText: String? = null,
    @SerializedName("content_protection") val contentProtection: Media? = null
)

data class CommonData(
    @SerializedName("source") val source: String?,
    @SerializedName("source_is_redirect") val isRedirect: Boolean,
    @SerializedName("poster") val poster: String?
)

data class LifespanClick(
    @SerializedName("value") val value: Int?,
    @SerializedName("unit") val unit: String?
)

data class Label(
    @SerializedName("id") val value: Int?,
    @SerializedName("text") val text: String?,
    @SerializedName("color") val color: String?
)

data class Media(
    @SerializedName("media") val mediaData: CommonData
)

enum class ContentType {
    VIDEO,
    AUDIO,
    HTML_PLAIN,
    HTML_SOURCE,
    SCREENSHOT
}
```

```kotlin
viewModelScope.launch {
            EnvoyApiProviderImpl.provide().createLink(
                body = CreateLinkBody(
                    contentSetting = ContentSetting(
                        type = ContentType.VIDEO,
                        name = "Content name",
                        description = "content description",
                        commonData = CommonData(
                            source = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                            isRedirect = false,
                            poster = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
                        ),
                        videoOrientation = VideoOrientation.vertical
                    ),
                    sharerId = "user_id"
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
```

### Create sandbox link

It is similar to the simple create link approach.

```kotlin
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
                    sharerId = "user_id"
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
```

### Get user quota

Get target user sharing quota.

```kotlin
viewModelScope.launch {
            EnvoyApiProviderImpl.provide().getUserQuota(
                userId = "user_id"
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
```

### Get pixel event

Log a pixel event

```kotlin
data class CreatePixelEventBody(
    @SerializedName("event_name") val eventName: String,
    @SerializedName("lead_uuid") val leadUuid: String? = null,
    @SerializedName("sharerUserId") val sharerUserId: String? = null,
    @SerializedName("extra") val extra: ExtraPixel? = null,
    @SerializedName("share_link_hash") var shareLinkHash: String? = null
)

data class ExtraPixel(
    @SerializedName("campaign") val campaign: String,
    @SerializedName("userType") val userType: String
)

enum class EventName {
    app_downloaded,
    account_created,
    trial_activated,
    payment_success
}
```

```kotlin
viewModelScope.launch {
            EnvoyApiProviderImpl.provide().createPixelEvent(
                body = CreatePixelEventBody(
                    eventName = EventName.app_downloaded.name
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
```

### Get user reward

Get rewards for a specific user

```kotlin
viewModelScope.launch {
            EnvoyApiProviderImpl.provide().getUserReward(
                userId = "user_id"
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
```

### Claim user reward

```kotlin
viewModelScope.launch {
            EnvoyApiProviderImpl.provide().claimUserReward(
                body = ClaimUserRewardBody(
                    userId = "user_id"
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
```

### Get current user reward

Get current rewards for a specific user

```kotlin
viewModelScope.launch {
            EnvoyApiProviderImpl.provide().getUserCurrentRewards(
                userId = "user_id"
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

                    else -> {
                        Log.d(TAG, "User current rewards: null")
                    }
                }
            }
        }
```



