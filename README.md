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
    implementation("com.github.Envoy-Technology:android-sdk:1.0.13") // replace "1.0.13" with the version you want to include
}
```

### Android manifest

The following permission is used in the Envoy library. You should include them in the Android manifest:

```
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.DETECT_SCREEN_CAPTURE" />
```

`DETECT_SCREEN_CAPTURE` is used by screenshot sharing. It is granted at install without a prompt and only tells the app that a screenshot was taken, not its image.


## Usage example
To initialize the library, you should call the init() method. You can get your project api key from [account settings](https://dev-platform.envoy.is/dashboard/account/).
```kotlin
EnvoyApiProviderImpl.init(
                        apiKey = "api_key",
                        context = applicationContext
                    )
```

### Check deferred link (required for install attribution)

> **Upgrading from 1.0.9 or earlier:**
> - `EnvoyApiProviderImpl.init` no longer reads the clipboard. If you don't call `checkDeferredLink`, installs are not attributed to the share link that brought the user: pixel events are sent without `share_link_hash` and `lead_uuid`.
> - If an earlier SDK version already stored a share link on this install, `checkDeferredLink` doesn't read the clipboard again.
> - `EnvoyApi` has a new method, `checkDeferredLink`. Classes that implement it, such as test doubles or wrappers, must implement it; a wrapper should forward it to `EnvoyApiProviderImpl.provide()`.

Envoy landing pages copy the share link to the clipboard before redirecting to Google Play. To attribute the install to that link, call `checkDeferredLink` after initialization, and log `app_downloaded` once per install right after that call. The SDK doesn't read the clipboard on its own, so you decide when it happens. If you need user consent first (e.g. under GDPR/ePrivacy in the EU), call it right after consent is given.

Call `init` in `Application.onCreate` or in your first activity's `onCreate`, before it is resumed: the SDK tracks the focused activity from then on, and Android gives clipboard access only to the app that has focus.

```kotlin
EnvoyApiProviderImpl.init(apiKey = "api_key", context = applicationContext)
EnvoyApiProviderImpl.provide().checkDeferredLink { found ->
    // found == true when Envoy link parameters were stored. Optional, e.g. to route the user.
}
// Once per install. Held until the check above ends, so it carries the attribution.
viewModelScope.launch {
    EnvoyApiProviderImpl.provide()
        .createPixelEvent(CreatePixelEventBody(eventName = EventName.app_downloaded.name))
        .collect { /* ... */ }
}
```

Don't log `app_downloaded` in the callback: it is called on every call of `checkDeferredLink`, with `false` once the check has been used up, so the event would be sent on every launch.

What it does:
- Runs once per install. It works from the first launch; once more than a day has passed since install, the clipboard no longer relates to the install, so it isn't touched and the check completes with `false`. This also covers apps that add the call in a later version, or get consent much later.
- It first checks, without reading the content, whether the clipboard holds text that may be a web URL (on Android 12+ using the system's classification of the clip). It reads the clipboard only if it does, so Android 12+ shows its "pasted from your clipboard" toast only in that case.
- It works with the clipboard only while an activity of the app has window focus, and waits for that if needed (e.g. while a dialog or a permission prompt is shown).
- It keeps only the `envoy_share_link_hash` and `envoy_lead_uuid` query parameters (stored by the SDK and sent with pixel events). The rest of the clipboard content is discarded.
- Calls made while a check is in progress get the result of that check.
- The callback is called on the main thread, never before `checkDeferredLink` returns, and at most 3 seconds later. The check itself can take longer.
- If the check can't finish (reading the clipboard fails twice in a row, or the app doesn't get focus within 30 seconds), it is not used up and a later call, e.g. on the next launch, tries again.

Pixel events:
- Events collected while a check is requested or in progress are held until the check ends and then sent with the attribution it found, if any. The returned flow emits `Loading` right away. An event whose collecting coroutine is cancelled while it is held (e.g. its `viewModelScope` is cleared) is not sent.
- If the app goes to the background first, held events are sent right away, since the app may be killed. Events collected in the background aren't held.
- Events collected when no check is in progress are sent right away, with whatever attribution is already stored.
- `checkDeferredLink` and `createPixelEvent` can be called from any thread.

Disclose this clipboard access and the attribution data in your privacy policy and in the Google Play Data safety form.

For every api request, you have to get the EnvoyApi with `EnvoyApiProviderImpl.provide`. Also, every request should be called from a `CoroutineScope` (the below examples are all called from a `viewModelScope`, but you can change that to your needs). The user id should be replaced by your user's id.

### Create link

Request is pretty flexible and have both required and optional values.

```kotlin
data class CreateLinkBody(
    @SerializedName("autoplay") val autoPlay: Boolean = false,
    @SerializedName("content_setting") val contentSetting: ContentSetting,
    @SerializedName("lifespan_after_click") val lifespanClick: LifespanClick? = null,
    @SerializedName("open_quota") val openQuota: Int? = null,
    @SerializedName("extra") val extra: Map<String, String>? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("sharer_id") val sharerId: String,
    @SerializedName("is_sandbox") val isSandbox: Boolean = false,
    @SerializedName("labels") val labels: List<Label>? = null,
    @SerializedName("is_carousel_link") val isCarouselLink: Boolean = false
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
                            poster = "https://envoy-misc.s3.eu-west-2.amazonaws.com/73ac27c5-dbbb-4ead-903b-d8382039bb2b_d365cfb1-94b6-4de7-a97b-55cb99768149.jpg"
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

### Share a link when the user takes a screenshot
When the user takes a screenshot of a screen with screenshot sharing turned on, the SDK creates that screen's link and opens the system share sheet with it, so the user shares a gift link instead of the image. Turn it on only on screens that show shareable content, such as an article. It stays on while the screen is started and turns off when it's destroyed:

```kotlin
class ArticleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EnvoyApiProviderImpl.provide().startScreenshotSharing(activity = this) {
            ScreenshotShare(           // return null to ignore the screenshot
                body = CreateLinkBody.forArticle(
                    url = article.url,
                    id = article.id,
                    title = article.title,
                    description = article.lead,
                    sharerId = user.id,
                    contentType = ContentType.SCREENSHOT,  // or ContentType.HTML_SOURCE
                    imageUrl = article.imageUrl
                ),
                message = "Your friend wants to gift you this article"
            )
        }
    }
}
```

In a fragment, pass its activity and `viewLifecycleOwner`: `startScreenshotSharing(requireActivity(), viewLifecycleOwner) { ... }`.

- The lambda is called on each screenshot and can return any `CreateLinkBody`. `forArticle` sends the article URL as the content source and its id in `extra` as `articleId`. With `SCREENSHOT` the gift landing shows a screenshot of the page, rendered by Envoy from the URL; with `HTML_SOURCE` it shows the page itself.
- The share sheet previews the text with the body's title and its `previewImage` (the `imageUrl` of `forArticle`), loaded together with the link (the thumbnail needs Android 10+). If the image isn't loaded within 2 seconds, the share sheet opens without it. Only the message and the link are shared; email apps use the title as the subject. The image is kept in the app's cache directory and served by the SDK's own `FileProvider`.
- Screenshots taken while the link is being created, or before the user is back from the share sheet, are ignored. A later screenshot of the same body reuses the link created earlier in the app session.
- If the link can't be created, no share sheet is shown. When the screen stops, a link still being created is dropped.
- Needs Android 14 or later; on earlier versions it does nothing.
- Pass `onEvent` to follow the progress: `CreatingLink`, `LinkCreated`, `ShareSheetOpened` or `Failed`.

> **Upgrading from 1.0.12 or earlier:** `ScreenshotDetector`, `ScreenshotLinkHelper` and `Bitmap.toBase64` are removed, together with the need for `READ_MEDIA_IMAGES` / `READ_EXTERNAL_STORAGE`. Use `startScreenshotSharing`.

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

Log a pixel event. The SDK adds the install attribution (`share_link_hash`, `lead_uuid`) stored by `checkDeferredLink` (see [Check deferred link](#check-deferred-link-required-for-install-attribution)) when the event is sent, replacing any values set in the body.

```kotlin
data class CreatePixelEventBody(
    @SerializedName("event_name") val eventName: String,
    @SerializedName("lead_uuid") val leadUuid: String? = null,
    @SerializedName("sharerUserId") val sharerUserId: String? = null,
    @SerializedName("extra") val extra: Map<String, String>? = null,
    @SerializedName("share_link_hash") var shareLinkHash: String? = null
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

### Prepare link

Prepare link's content before it's gifted. Function specific for SCREENSHOT types of links.

```kotlin
viewModelScope.launch {
            EnvoyApiProviderImpl.provide().prepLink(
                url = "url"
            ).collect { resource ->
                when (resource) {
                    is Success -> {
                        Log.d(TAG, "Prep link: Success -> ${resource.value}")
                    }

                    is Loading -> {
                        Log.d(TAG, "Prep link: Loading")
                    }

                    is Failure -> {
                        Log.d(TAG, "Prep link: Failure -> ${resource.throwable.message}")
                    }
                }
            }
        }
```

### Manage links

Reset current carousel link selection and select the links identified by the provided hashes.

```kotlin
data class ManageLinksRequest(
    val linkHashes: List<String>
)
```

```kotlin
viewModelScope.launch {
            EnvoyApiProviderImpl.provide().manageLinks(
                body = ManageLinksRequest(
                    linkHashes = listOf("aBc123", "xYz789")
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
```

### Clear managed links

Unselect all marketing links from the API links carousel.

```kotlin
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
```

