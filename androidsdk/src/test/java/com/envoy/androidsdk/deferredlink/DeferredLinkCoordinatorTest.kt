package com.envoy.androidsdk.deferredlink

import com.envoy.androidsdk.domain.shared.HawkRepository
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

private const val LINK = "https://envoy.is/l/abc?envoy_share_link_hash=hash1&envoy_lead_uuid=lead1&other=x"
private const val HOUR = 60 * 60 * 1000L

class DeferredLinkCoordinatorTest {

    private val mainThread = FakeMainThread()
    private val clipboard = FakeClipboard()
    private val storage = FakeStorage()
    private val appState = FakeAppState()
    private var installTime: Long? = 0L

    private fun coordinator() = DeferredLinkCoordinator(
        clipboard = clipboard,
        storage = storage,
        appState = appState,
        mainThread = mainThread,
        installTimeMillis = { installTime },
        nowMillis = { mainThread.now + HOUR }
    )

    private fun DeferredLinkCoordinator.check(): MutableList<Boolean> {
        val results = mutableListOf<Boolean>()
        checkDeferredLink { results += it }
        return results
    }

    @Test
    fun storesLinkParametersAndUsesCheckUp() {
        clipboard.text = LINK
        val coordinator = coordinator()

        val results = coordinator.check()
        assertTrue("result is never delivered synchronously", results.isEmpty())
        mainThread.runPending()

        assertEquals(listOf(true), results)
        assertEquals("hash1", storage.storedHash)
        assertEquals("lead1", storage.storedLeadUuid)
        assertEquals(true, storage.checkDone)

        val second = coordinator.check()
        mainThread.runPending()
        assertEquals(listOf(false), second)
        assertEquals(1, clipboard.reads)
    }

    @Test
    fun doesNotReadClipboardWithoutWebUrl() {
        clipboard.text = LINK
        clipboard.mayContainWebUrl = false
        val results = coordinator().check()
        mainThread.runPending()

        assertEquals(listOf(false), results)
        assertEquals(0, clipboard.reads)
        assertEquals(true, storage.checkDone)
    }

    @Test
    fun clipboardWithoutEnvoyParametersIsUsedUp() {
        clipboard.text = "https://example.com/?foo=bar"
        val results = coordinator().check()
        mainThread.runPending()

        assertEquals(listOf(false), results)
        assertNull(storage.storedHash)
        assertEquals(true, storage.checkDone)
    }

    @Test
    fun skipsCheckWhenInstalledMoreThanADayAgo() {
        clipboard.text = LINK
        installTime = -30 * HOUR
        val results = coordinator().check()
        mainThread.runPending()

        assertEquals(listOf(false), results)
        assertEquals(0, clipboard.reads)
        assertEquals(true, storage.checkDone)
    }

    @Test
    fun waitsForFocusBeforeTouchingClipboard() {
        clipboard.text = LINK
        appState.hasFocus = false
        val results = coordinator().check()
        mainThread.runPending()

        assertEquals(0, clipboard.detections)
        assertTrue(results.isEmpty())

        appState.gainFocus()
        mainThread.runPending()
        assertEquals(listOf(true), results)
    }

    @Test
    fun resultTimesOutAfterThreeSecondsButCheckGoesOn() {
        clipboard.text = LINK
        appState.hasFocus = false
        val results = coordinator().check()

        mainThread.advanceBy(3_000)
        assertEquals(listOf(false), results)

        appState.gainFocus()
        mainThread.runPending()
        assertEquals(listOf(false), results)
        assertEquals("hash1", storage.storedHash)
    }

    @Test
    fun checkNotFinishedWithin30SecondsIsNotUsedUp() {
        clipboard.text = LINK
        appState.hasFocus = false
        val coordinator = coordinator()
        coordinator.check()

        mainThread.advanceBy(30_000)
        assertEquals(false, storage.checkDone)

        appState.hasFocus = true
        val retry = coordinator.check()
        mainThread.runPending()
        assertEquals(listOf(true), retry)
    }

    @Test
    fun retriesOnceAfterDetectionFailureThenLeavesCheckForLater() {
        clipboard.failures = 2
        val results = coordinator().check()
        mainThread.advanceBy(1_000)

        assertEquals(2, clipboard.detections)
        assertEquals(listOf(false), results)
        assertEquals(false, storage.checkDone)
    }

    @Test
    fun recoversWhenDetectionFailsOnce() {
        clipboard.text = LINK
        clipboard.failures = 1
        val results = coordinator().check()
        mainThread.advanceBy(1_000)

        assertEquals(listOf(true), results)
    }

    @Test
    fun concurrentCallsShareTheRunningCheck() {
        clipboard.text = LINK
        appState.hasFocus = false
        val coordinator = coordinator()
        val first = coordinator.check()
        val second = coordinator.check()

        appState.gainFocus()
        mainThread.runPending()

        assertEquals(listOf(true), first)
        assertEquals(listOf(true), second)
        assertEquals(1, clipboard.reads)
    }

    @Test
    fun migrationMarksCheckDoneWhenEarlierSdkStoredALink() {
        storage.storedHash = "old"
        clipboard.text = LINK
        val results = coordinator().check()
        mainThread.runPending()

        assertEquals(listOf(false), results)
        assertEquals("old", storage.storedHash)
        assertEquals(0, clipboard.reads)
    }

    @Test
    fun migrationKeepsCheckOpenWhenNothingWasStored() {
        coordinator()
        assertEquals(false, storage.checkDone)
    }

    @Test
    fun pixelEventsAreHeldUntilCheckEnds() {
        clipboard.text = LINK
        appState.hasFocus = false
        val coordinator = coordinator()
        coordinator.checkDeferredLink(null)

        var attribution: DeferredLinkCoordinator.Attribution? = null
        sendPixelEvent(coordinator) { attribution = it }
        assertNull("held while the check runs", attribution)

        appState.gainFocus()
        assertEquals(DeferredLinkCoordinator.Attribution("hash1", "lead1"), attribution)
    }

    @Test
    fun pixelEventsAreHeldForCheckRequestedFromAnotherThread() {
        clipboard.text = LINK
        val coordinator = coordinator()
        mainThread.onMainThread = false
        coordinator.checkDeferredLink(null)
        mainThread.onMainThread = true

        var attribution: DeferredLinkCoordinator.Attribution? = null
        sendPixelEvent(coordinator) { attribution = it }
        assertNull(attribution)

        mainThread.runPending()
        assertEquals("hash1", attribution?.shareLinkHash)
    }

    @Test
    fun heldPixelEventsAreReleasedInBackground() {
        appState.hasFocus = false
        val coordinator = coordinator()
        coordinator.checkDeferredLink(null)

        var released = false
        sendPixelEvent(coordinator) { released = true }
        assertFalse(released)

        appState.isInBackground.value = true
        assertTrue(released)
    }

    @Test
    fun pixelEventsAreNotHeldWithoutCheck() {
        storage.storedHash = "stored"
        var attribution: DeferredLinkCoordinator.Attribution? = null
        sendPixelEvent(coordinator()) { attribution = it }
        assertEquals("stored", attribution?.shareLinkHash)
    }

    @Test
    fun parsesOnlyEnvoyParameters() {
        assertEquals(
            mapOf("envoy_share_link_hash" to "a b", "envoy_lead_uuid" to "lead"),
            DeferredLinkCoordinator.linkParameters("  https://x.io/p?envoy_share_link_hash=a%20b&envoy_lead_uuid=lead&q=1\n")
        )
        assertEquals(emptyMap<String, String>(), DeferredLinkCoordinator.linkParameters("not a link at all"))
        assertEquals(emptyMap<String, String>(), DeferredLinkCoordinator.linkParameters("https://x.io/p?envoy_share_link_hash="))
    }

    @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
    private fun sendPixelEvent(
        coordinator: DeferredLinkCoordinator,
        onSent: (DeferredLinkCoordinator.Attribution) -> Unit
    ) {
        GlobalScope.launch(Dispatchers.Unconfined, start = CoroutineStart.UNDISPATCHED) {
            coordinator.awaitPixelEventRelease()
            onSent(coordinator.storedAttribution())
        }
    }
}

/** Runs posted actions on demand, on a virtual clock. */
private class FakeMainThread : MainThread {
    private class Task(val time: Long, val order: Int, val action: () -> Unit) {
        var cancelled = false
    }

    var now = 0L
    var onMainThread = true
    private var order = 0
    private val tasks = mutableListOf<Task>()

    override fun isCurrent() = onMainThread

    override fun post(action: () -> Unit) {
        postDelayed(0, action)
    }

    override fun postDelayed(delayMillis: Long, action: () -> Unit): () -> Unit {
        val task = Task(now + delayMillis, order++, action)
        tasks += task
        return { task.cancelled = true }
    }

    fun runPending() = advanceBy(0)

    fun advanceBy(millis: Long) {
        val until = now + millis
        while (true) {
            val next = tasks.filter { !it.cancelled && it.time <= until }
                .minWithOrNull(compareBy<Task>({ it.time }, { it.order })) ?: break
            tasks.remove(next)
            now = next.time
            next.action()
        }
        now = until
    }
}

private class FakeClipboard : DeferredLinkClipboard {
    var text: String? = null
    var mayContainWebUrl = true
    var failures = 0
    var detections = 0
    var reads = 0

    override fun mayContainWebUrl(): Boolean {
        detections++
        if (failures > 0) {
            failures--
            throw SecurityException("clipboard unavailable")
        }
        return mayContainWebUrl
    }

    override fun readText(): String? {
        reads++
        return text
    }
}

private class FakeStorage : HawkRepository {
    var storedHash: String? = null
    var storedLeadUuid: String? = null
    var checkDone: Boolean? = null

    override fun getHash() = storedHash
    override fun setHash(hash: String) {
        storedHash = hash
    }

    override fun getLeadUiid() = storedLeadUuid
    override fun setLeadUiid(leadUiid: String) {
        storedLeadUuid = leadUiid
    }

    override fun isDeferredLinkCheckDone() = checkDone
    override fun setDeferredLinkCheckDone(done: Boolean) {
        checkDone = done
    }
}

private class FakeAppState : AppState {
    override val isInBackground = MutableStateFlow(false)
    override var hasFocus = true
    private var pending: (() -> Unit)? = null

    override fun awaitFocus(action: () -> Unit) {
        pending = action
    }

    override fun stopAwaitingFocus() {
        pending = null
    }

    fun gainFocus() {
        hasFocus = true
        val action = pending
        pending = null
        action?.invoke()
    }
}
