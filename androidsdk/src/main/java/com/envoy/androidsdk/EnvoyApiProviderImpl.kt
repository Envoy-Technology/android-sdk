package com.envoy.androidsdk

import android.app.Application
import android.content.Context
import android.util.Log
import com.envoy.androidsdk.api.EnvoyApi
import com.envoy.androidsdk.api.EnvoyApiProvider
import com.envoy.androidsdk.data.EnvoyRepositoryImpl
import com.envoy.androidsdk.data.EnvoyServiceApi
import com.envoy.androidsdk.data.SharedPreferencesStorage
import com.envoy.androidsdk.data.network.RetrofitFactory
import com.envoy.androidsdk.data.network.RetrofitFactoryImpl
import com.envoy.androidsdk.data.network.model.SdkConfig
import com.envoy.androidsdk.deferredlink.AndroidAppState
import com.envoy.androidsdk.deferredlink.AndroidMainThread
import com.envoy.androidsdk.deferredlink.DeferredLinkCoordinator
import com.envoy.androidsdk.deferredlink.SystemDeferredLinkClipboard
import com.envoy.androidsdk.domain.EnvoyRepository
import com.envoy.androidsdk.domain.shared.InitException
import com.envoy.androidsdk.domain.shared.SdkStorage
import com.envoy.androidsdk.domain.usecase.UseCaseFactory
import com.envoy.androidsdk.screenshot.FlowLinkCreator
import com.envoy.androidsdk.screenshot.ScreenshotSharingController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.coroutines.CoroutineContext

private val TAG = EnvoyApiProviderImpl::class.java.name

object EnvoyApiProviderImpl : EnvoyApiProvider {

    private var envoyApi: EnvoyApi? = null

    private lateinit var retrofitFactory: RetrofitFactory
    private lateinit var coroutineContext: CoroutineContext
    private lateinit var repository: EnvoyRepository
    private lateinit var useCaseFactory: UseCaseFactory
    private lateinit var storage: SdkStorage
    private lateinit var deferredLinks: DeferredLinkCoordinator
    private lateinit var mainThread: AndroidMainThread

    private val isInitialized = MutableStateFlow(false)

    override fun init(
        apiKey: String,
        coroutineContext: CoroutineContext,
        context: Context
    ) {
        if (!isInitialized.value) {
            val sdkConfig = SdkConfig(
                // for testing
//                baseUrl = "https://dev-api.envoy.is/partner/",
                // for release
                baseUrl = "https://api.envoy.is/partner/",
                apiKey = apiKey
            )
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

            useCaseFactory = UseCaseFactory(repository)

            storage = SharedPreferencesStorage(context)

            // The clipboard is read only by EnvoyApi.checkDeferredLink, so the host app decides when.
            val application = context.applicationContext as Application
            mainThread = AndroidMainThread()
            deferredLinks = DeferredLinkCoordinator(
                clipboard = SystemDeferredLinkClipboard(application),
                storage = storage,
                appState = AndroidAppState(application, mainThread),
                mainThread = mainThread,
                installTimeMillis = { firstInstallTime(application) }
            )

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
            val createLinkUseCase = useCaseFactory.getCreateLinkUseCase()
            envoyApi = EnvoyApiImpl(
                createLinkUseCase = createLinkUseCase,
                getUserQuotaUseCase = useCaseFactory.getUserQuotaUseCase(),
                createPixelEventUseCase = useCaseFactory.getCreatePixelEventUseCase(deferredLinks),
                getUserRewardsUseCase = useCaseFactory.getUserRewardsUseCase(),
                claimUserRewardUseCase = useCaseFactory.getClaimUserRewardUseCase(),
                getCurrentRewardsUseCase = useCaseFactory.getUserCurrentRewardsUseCase(),
                prepLinkUseCase = useCaseFactory.prepLinkUseCase(),
                manageLinksUseCase = useCaseFactory.getManageLinksUseCase(),
                clearManagedLinksUseCase = useCaseFactory.getClearManagedLinksUseCase(),
                deferredLinks = deferredLinks,
                screenshotSharing = ScreenshotSharingController(
                    linkCreator = FlowLinkCreator(createLink = { createLinkUseCase(it) }, mainThread = mainThread),
                    log = { Log.w(TAG, it) }
                )
            )
            envoyApi as EnvoyApi
        } else {
            envoyApi as EnvoyApi
        }
    }

    /** Kept across app updates. */
    private fun firstInstallTime(context: Context): Long? =
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).firstInstallTime
        } catch (e: Exception) {
            Log.w(TAG, "Envoy SDK could not read the app install time", e)
            null
        }
}
