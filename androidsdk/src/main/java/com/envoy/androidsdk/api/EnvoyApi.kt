package com.envoy.androidsdk.api

import androidx.activity.ComponentActivity
import androidx.lifecycle.LifecycleOwner
import com.envoy.androidsdk.domain.model.ClaimUserRewardBody
import com.envoy.androidsdk.domain.model.ClaimUserRewardResponse
import com.envoy.androidsdk.domain.model.CreateLinkBody
import com.envoy.androidsdk.domain.model.CreateLinkResponse
import com.envoy.androidsdk.domain.model.CreatePixelEventBody
import com.envoy.androidsdk.domain.model.GetUserRewardResponse
import com.envoy.androidsdk.domain.model.ManageLinksRequest
import com.envoy.androidsdk.domain.model.PrepLinkRequest
import com.envoy.androidsdk.domain.model.UserCurrentRewardsResponse
import com.envoy.androidsdk.domain.model.UserQuotaResponse
import com.envoy.androidsdk.domain.shared.Resource
import com.envoy.androidsdk.screenshot.ScreenshotShare
import com.envoy.androidsdk.screenshot.ScreenshotSharingEvent
import kotlinx.coroutines.flow.Flow

interface EnvoyApi {

    /**
     * Reads the clipboard once per install, within a day of installing, and calls [onResult] with `true`
     * when Envoy link parameters were found and stored.
     * The clipboard is read only when it may contain a web URL, so Android shows its clipboard access toast
     * only then, and only while an activity of the app has window focus.
     * Calls made while a check is in progress get its result.
     * If the check can't finish (the clipboard fails twice or the app doesn't get focus within 30 seconds),
     * it is not used up and a later call tries again.
     * Pixel events logged while the check runs are sent after it, so they carry the attribution.
     * Can be called from any thread; [onResult] is called on the main thread, never before this returns,
     * and at most 3 seconds later.
     */
    fun checkDeferredLink(onResult: ((found: Boolean) -> Unit)? = null)

    fun createLink(body: CreateLinkBody): Flow<Resource<CreateLinkResponse>>

    fun getUserQuota(userId: String): Flow<Resource<UserQuotaResponse>>

    fun createPixelEvent(body: CreatePixelEventBody): Flow<Resource<Unit>>

    fun getUserReward(userId: String): Flow<Resource<GetUserRewardResponse>>

    fun claimUserReward(body: ClaimUserRewardBody): Flow<Resource<ClaimUserRewardResponse>>

    fun getUserCurrentRewards(userId: String): Flow<Resource<UserCurrentRewardsResponse>>

    fun prepLink(body: PrepLinkRequest): Flow<Resource<Unit>>

    fun manageLinks(body: ManageLinksRequest): Flow<Resource<Unit>>

    fun clearManagedLinks(): Flow<Resource<Unit>>

    /**
     * Turns screenshot sharing on for a screen, such as an article, while [lifecycleOwner] is started:
     * pass the activity, or a fragment's `viewLifecycleOwner` together with its activity.
     * On each screenshot [share] is asked what to share; return null to ignore that screenshot.
     * The SDK then creates the link (or reuses the one created for the same body earlier in the app session)
     * and opens the system share sheet with it.
     * Screenshots taken while a link is being created, or before the user is back from the share sheet, are ignored.
     * When the screen stops, a link still being created is dropped; when it's destroyed, sharing is turned off.
     * Calling it again, from any screen, replaces the earlier screen.
     * Needs Android 14 or later, and no permission to read the screen; on earlier versions it does nothing.
     * Call it on the main thread; [share] and [onEvent] are called on the main thread.
     */
    fun startScreenshotSharing(
        activity: ComponentActivity,
        lifecycleOwner: LifecycleOwner = activity,
        onEvent: ((ScreenshotSharingEvent) -> Unit)? = null,
        share: () -> ScreenshotShare?
    )

    /** Turns screenshot sharing off before the screen is destroyed. Main thread only. */
    fun stopScreenshotSharing()
}

