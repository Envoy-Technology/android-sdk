package com.envoy.androidsdk.deferredlink

import com.envoy.androidsdk.domain.shared.HawkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import java.net.URI
import java.net.URISyntaxException
import java.net.URLDecoder

private const val SHARE_LINK_HASH = "envoy_share_link_hash"
private const val LEAD_UUID = "envoy_lead_uuid"
private const val ONE_DAY_MILLIS = 24 * 60 * 60 * 1000L

/**
 * State of the deferred-link check and of the pixel events held while it runs.
 * The state is touched only on the main thread, except [holds], which is thread-safe.
 */
internal class DeferredLinkCoordinator(
    private val clipboard: DeferredLinkClipboard,
    private val storage: HawkRepository,
    private val appState: AppState,
    private val mainThread: MainThread,
    private val installTimeMillis: () -> Long?,
    private val nowMillis: () -> Long = System::currentTimeMillis,
    private val attributionWindowMillis: Long = ONE_DAY_MILLIS,
    private val completionTimeoutMillis: Long = 3_000,
    private val checkTimeLimitMillis: Long = 30_000,
    private val failureRetryDelayMillis: Long = 1_000
) {

    data class Attribution(val shareLinkHash: String?, val leadUuid: String?)

    private class Check(val id: Int, val cancelTimeLimit: () -> Unit) {
        var hasFailedOnce = false
    }

    private class WaitingResult(val onResult: (Boolean) -> Unit, val cancelTimeout: () -> Unit)

    private var check: Check? = null
    private var checkCount = 0
    private val waitingResults = sortedMapOf<Int, WaitingResult>()
    private var resultCount = 0

    /** Checks requested but not started yet, plus the running one. Pixel events are held while it's above zero. */
    private val holds = MutableStateFlow(0)

    init {
        migrateCheckDone()
    }

    // Entry points, callable from any thread

    fun checkDeferredLink(onResult: ((Boolean) -> Unit)?) {
        // Held right away, so an event logged before the check starts on the main thread waits for it.
        holds.update { it + 1 }
        mainThread.runOrPost {
            startCheck(onResult)
            holds.update { it - 1 }
        }
    }

    /**
     * Suspends while a check is requested or running, so the pixel event sent afterwards carries the attribution
     * it finds. Returns right away in the background, since the app may be killed there.
     */
    suspend fun awaitPixelEventRelease() {
        combine(holds, appState.isInBackground) { holds, isInBackground -> holds == 0 || isInBackground }
            .first { it }
    }

    fun storedAttribution(): Attribution =
        Attribution(shareLinkHash = storage.getHash(), leadUuid = storage.getLeadUiid())

    // Check

    private fun startCheck(onResult: ((Boolean) -> Unit)?) {
        if (check != null) {
            waitForResult(onResult)
            return
        }
        if (storage.isDeferredLinkCheckDone() == true) {
            reply(false, onResult)
            return
        }
        if (isInstalledTooLongAgo()) {
            // The clipboard no longer relates to the install.
            storage.setDeferredLinkCheckDone(true)
            reply(false, onResult)
            return
        }

        val id = ++checkCount
        // Not used up, so a later call can retry.
        val cancelTimeLimit = mainThread.postDelayed(checkTimeLimitMillis) { endCheck(id, found = false) }
        check = Check(id, cancelTimeLimit)
        holds.update { it + 1 }
        waitForResult(onResult)
        detect(id)
    }

    private fun isInstalledTooLongAgo(): Boolean {
        val installTime = installTimeMillis() ?: return false
        return nowMillis() - installTime > attributionWindowMillis
    }

    private fun detect(id: Int) {
        if (!appState.hasFocus) {
            // Android gives clipboard access only to the app that has focus.
            appState.awaitFocus { detect(id) }
            return
        }
        handleDetection(runCatching { clipboard.mayContainWebUrl() }, id)
    }

    private fun handleDetection(result: Result<Boolean>, id: Int) {
        // Ignore a late answer to a check that already ended.
        val check = check?.takeIf { it.id == id } ?: return
        val mayContainWebUrl = result.getOrNull()
        when {
            mayContainWebUrl == null && !check.hasFailedOnce -> {
                check.hasFailedOnce = true
                mainThread.postDelayed(failureRetryDelayMillis) {
                    if (this.check?.id == id) detect(id)
                }
            }
            // Not used up, so a later call can retry.
            mayContainWebUrl == null -> endCheck(id, found = false)
            !mayContainWebUrl -> {
                storage.setDeferredLinkCheckDone(true)
                endCheck(id, found = false)
            }
            else -> {
                // Used up before reading, so a failing read doesn't show the clipboard toast again.
                storage.setDeferredLinkCheckDone(true)
                val content = runCatching { clipboard.readText() }.getOrNull()
                endCheck(id, found = storeLinkParameters(content))
            }
        }
    }

    private fun endCheck(id: Int, found: Boolean) {
        val check = check?.takeIf { it.id == id } ?: return
        check.cancelTimeLimit()
        appState.stopAwaitingFocus()
        this.check = null
        // Released before the results, so events held during the check go before anything logged from them.
        holds.update { it - 1 }
        waitingResults.keys.toList().forEach { answer(it, found) }
    }

    private fun storeLinkParameters(clipboardContent: String?): Boolean {
        val parameters = linkParameters(clipboardContent ?: return false)
        parameters[SHARE_LINK_HASH]?.let(storage::setHash)
        parameters[LEAD_UUID]?.let(storage::setLeadUiid)
        return parameters.containsKey(SHARE_LINK_HASH) || parameters.containsKey(LEAD_UUID)
    }

    // Results

    private fun waitForResult(onResult: ((Boolean) -> Unit)?) {
        onResult ?: return
        val token = ++resultCount
        // A caller waits at most `completionTimeoutMillis`; the check itself can go on and still attribute held events.
        val cancelTimeout = mainThread.postDelayed(completionTimeoutMillis) { answer(token, found = false) }
        waitingResults[token] = WaitingResult(onResult, cancelTimeout)
    }

    private fun answer(token: Int, found: Boolean) {
        val waiting = waitingResults.remove(token) ?: return
        waiting.cancelTimeout()
        reply(found, waiting.onResult)
    }

    /** Always posted, so a result never arrives before `checkDeferredLink` returns. */
    private fun reply(found: Boolean, onResult: ((Boolean) -> Unit)?) {
        onResult ?: return
        mainThread.post { onResult(found) }
    }

    /**
     * SDKs before the explicit check read the clipboard on every start until they found a link,
     * so an update from them must not check again once a link was stored.
     */
    private fun migrateCheckDone() {
        if (storage.isDeferredLinkCheckDone() != null) return
        val attribution = storedAttribution()
        storage.setDeferredLinkCheckDone(attribution.shareLinkHash != null || attribution.leadUuid != null)
    }

    companion object {
        /** Envoy query parameters of the link in [clipboardContent]; the rest is discarded. */
        internal fun linkParameters(clipboardContent: String): Map<String, String> {
            val query = try {
                URI(clipboardContent.trim()).rawQuery
            } catch (_: URISyntaxException) {
                null
            } ?: return emptyMap()
            return query.split('&')
                .mapNotNull { parameter ->
                    val name = decode(parameter.substringBefore('='))
                    val value = parameter.substringAfter('=', missingDelimiterValue = "")
                    if (name == SHARE_LINK_HASH || name == LEAD_UUID) name to decode(value) else null
                }
                .filter { (_, value) -> value.isNotEmpty() }
                .toMap()
        }

        private fun decode(value: String): String =
            try {
                URLDecoder.decode(value, "UTF-8")
            } catch (_: IllegalArgumentException) {
                value
            }
    }
}
