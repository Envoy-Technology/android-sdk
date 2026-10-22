package com.envoy.androidsdk

import androidx.activity.ComponentActivity
import androidx.lifecycle.LifecycleOwner
import com.envoy.androidsdk.api.EnvoyApi
import com.envoy.androidsdk.deferredlink.DeferredLinkCoordinator
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
import com.envoy.androidsdk.domain.usecase.ClaimUserRewardUseCase
import com.envoy.androidsdk.domain.usecase.ClearManagedLinksUseCase
import com.envoy.androidsdk.domain.usecase.CreateLinkUseCase
import com.envoy.androidsdk.domain.usecase.CreatePixelEventUseCase
import com.envoy.androidsdk.domain.usecase.GetUserQuotaUseCase
import com.envoy.androidsdk.domain.usecase.GetUserRewardsUseCase
import com.envoy.androidsdk.domain.usecase.GetCurrentRewardsUseCase
import com.envoy.androidsdk.domain.usecase.ManageLinksUseCase
import com.envoy.androidsdk.domain.usecase.PrepLinkUseCase
import com.envoy.androidsdk.screenshot.AndroidScreenCapture
import com.envoy.androidsdk.screenshot.AndroidShareSheet
import com.envoy.androidsdk.screenshot.ScreenshotShare
import com.envoy.androidsdk.screenshot.ScreenshotSharingController
import com.envoy.androidsdk.screenshot.ScreenshotSharingEvent
import com.envoy.androidsdk.screenshot.ScreenshotSharingLifecycleObserver
import kotlinx.coroutines.flow.Flow

internal class EnvoyApiImpl(
    private val createLinkUseCase: CreateLinkUseCase,
    private val getUserQuotaUseCase: GetUserQuotaUseCase,
    private val createPixelEventUseCase: CreatePixelEventUseCase,
    private val getUserRewardsUseCase: GetUserRewardsUseCase,
    private val claimUserRewardUseCase: ClaimUserRewardUseCase,
    private val getCurrentRewardsUseCase: GetCurrentRewardsUseCase,
    private val prepLinkUseCase: PrepLinkUseCase,
    private val manageLinksUseCase: ManageLinksUseCase,
    private val clearManagedLinksUseCase: ClearManagedLinksUseCase,
    private val deferredLinks: DeferredLinkCoordinator,
    private val screenshotSharing: ScreenshotSharingController
) : EnvoyApi {

    override fun checkDeferredLink(onResult: ((found: Boolean) -> Unit)?) {
        deferredLinks.checkDeferredLink(onResult)
    }

    override fun createLink(body: CreateLinkBody): Flow<Resource<CreateLinkResponse>> {
        return createLinkUseCase.invoke(body = body)
    }

    override fun getUserQuota(userId: String): Flow<Resource<UserQuotaResponse>> {
        return getUserQuotaUseCase.invoke(userId = userId)
    }

    override fun createPixelEvent(
        body: CreatePixelEventBody
    ): Flow<Resource<Unit>> {
        return createPixelEventUseCase.invoke(body = body)
    }

    override fun getUserReward(userId: String): Flow<Resource<GetUserRewardResponse>> {
        return getUserRewardsUseCase.invoke(userId = userId)
    }

    override fun claimUserReward(body: ClaimUserRewardBody): Flow<Resource<ClaimUserRewardResponse>> {
        return claimUserRewardUseCase.invoke(body = body)
    }

    override fun getUserCurrentRewards(userId: String): Flow<Resource<UserCurrentRewardsResponse>> {
        return getCurrentRewardsUseCase.invoke(userId = userId)
    }

    override fun prepLink(body: PrepLinkRequest): Flow<Resource<Unit>> {
        return prepLinkUseCase.invoke(body = body)
    }

    override fun manageLinks(body: ManageLinksRequest): Flow<Resource<Unit>> {
        return manageLinksUseCase.invoke(body = body)
    }

    override fun clearManagedLinks(): Flow<Resource<Unit>> {
        return clearManagedLinksUseCase.invoke()
    }

    override fun startScreenshotSharing(
        activity: ComponentActivity,
        lifecycleOwner: LifecycleOwner,
        onEvent: ((ScreenshotSharingEvent) -> Unit)?,
        share: () -> ScreenshotShare?
    ) {
        val session = screenshotSharing.start(
            screenCapture = AndroidScreenCapture(activity),
            shareSheet = AndroidShareSheet(activity),
            onEvent = onEvent,
            share = share
        )
        val observer = ScreenshotSharingLifecycleObserver(session)
        session.detach = { lifecycleOwner.lifecycle.removeObserver(observer) }
        // Catches up with the current state, e.g. starts right away for a resumed screen.
        lifecycleOwner.lifecycle.addObserver(observer)
    }

    override fun stopScreenshotSharing() {
        screenshotSharing.stop()
    }
}
