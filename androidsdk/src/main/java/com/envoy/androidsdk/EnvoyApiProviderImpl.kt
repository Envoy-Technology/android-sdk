package com.envoy.androidsdk

import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.CountDownTimer
import android.util.Log
import com.envoy.androidsdk.api.EnvoyApi
import com.envoy.androidsdk.api.EnvoyApiProvider
import com.envoy.androidsdk.data.EnvoyRepositoryImpl
import com.envoy.androidsdk.data.EnvoyServiceApi
import com.envoy.androidsdk.data.HawkRepositoryImpl
import com.envoy.androidsdk.data.network.RetrofitFactory
import com.envoy.androidsdk.data.network.RetrofitFactoryImpl
import com.envoy.androidsdk.data.network.model.SdkConfig
import com.envoy.androidsdk.domain.EnvoyRepository
import com.envoy.androidsdk.domain.shared.HawkRepository
import com.envoy.androidsdk.domain.shared.InitException
import com.envoy.androidsdk.domain.usecase.UseCaseFactory
import com.orhanobut.hawk.DefaultHawkFacade
import com.orhanobut.hawk.Hawk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.coroutines.CoroutineContext

private val TAG = EnvoyApiProviderImpl::class.java.name
private const val SHARER_HASH = "envoy_share_link_hash"
private const val LEAD_UUID = "envoy_lead_uuid"
private const val ONE_SECOND = 1000L
private const val CLIP_BOARD_TIMER = 5000L // 5 seconds

object EnvoyApiProviderImpl : EnvoyApiProvider {

    private var envoyApi: EnvoyApi? = null

    private lateinit var retrofitFactory: RetrofitFactory
    private lateinit var coroutineContext: CoroutineContext
    private lateinit var repository: EnvoyRepository
    private lateinit var useCaseFactory: UseCaseFactory
    private lateinit var hawkRepository: HawkRepository

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

            val builder = Hawk.init(context)
            hawkRepository = HawkRepositoryImpl(
                DefaultHawkFacade(builder)
            )

            // we cannot access the clipboard manager before the view is done, so we have to delay accessing it
            val clipBoardTimer = object : CountDownTimer(CLIP_BOARD_TIMER, ONE_SECOND) {
                override fun onTick(millisUntilFinished: Long) {
                    // NOP
                }

                override fun onFinish() {
                    val clipBoardManager =
                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val copiedString = clipBoardManager.primaryClip?.getItemAt(0)?.text?.toString()
                    if (hawkRepository.getHash() == null && copiedString?.contains(SHARER_HASH) == true) {
                        val uri = Uri.parse(copiedString)
                        val hash = uri.getQueryParameters(SHARER_HASH).first()
                        hawkRepository.setHash(hash)
                        if (copiedString.contains(LEAD_UUID)) {
                            val leadUuid = uri.getQueryParameters(LEAD_UUID).first()
                            hawkRepository.setLeadUiid(leadUuid)
                        }

                    }
                }
            }

            clipBoardTimer.start()

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
            envoyApi = EnvoyApiImpl(
                createLinkUseCase = useCaseFactory.getCreateLinkUseCase(),
                createSandboxLinkUseCase = useCaseFactory.getCreateSandboxLinkUseCase(),
                getUserQuotaUseCase = useCaseFactory.getUserQuotaUseCase(),
                createPixelEventUseCase = useCaseFactory.getCreatePixelEventUseCase(hawkRepository),
                getUserRewardsUseCase = useCaseFactory.getUserRewardsUseCase(),
                claimUserRewardUseCase = useCaseFactory.getClaimUserRewardUseCase(),
                getCurrentRewardsUseCase = useCaseFactory.getUserCurrentRewardsUseCase()
            )
            envoyApi as EnvoyApi
        } else {
            envoyApi as EnvoyApi
        }
    }
}
