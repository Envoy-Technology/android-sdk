package com.envoy.androidsdk.screenshot

import com.envoy.androidsdk.domain.model.ContentType
import com.envoy.androidsdk.domain.model.CreateLinkBody
import com.envoy.androidsdk.domain.model.CreateLinkResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private const val LINK = "https://envoy.is/l/abc"

class ScreenshotSharingControllerTest {

    private val linkCreator = FakeLinkCreator()
    private val screenCapture = FakeScreenCapture()
    private val shareSheet = FakeShareSheet()
    private val events = mutableListOf<String>()
    private val controller = ScreenshotSharingController(linkCreator)

    private fun share(id: String = "article-1") = ScreenshotShare(
        body = CreateLinkBody.forArticle(
            url = "https://news.example/$id",
            id = id,
            title = "Title",
            description = "Description",
            sharerId = "sharer-1",
            contentType = ContentType.SCREENSHOT
        ),
        message = "Read it free"
    )

    /** A resumed screen, as after its activity's onStart and onResume. */
    private fun start(
        share: ScreenshotShare? = share(),
        capture: FakeScreenCapture = screenCapture
    ): ScreenshotSharingController.Session =
        controller.start(capture, shareSheet, onEvent = { events += it.label() }, share = { share }).apply {
            onStarted()
            onResumed()
        }

    private fun ScreenshotSharingEvent.label() = when (this) {
        ScreenshotSharingEvent.CreatingLink -> "creatingLink"
        is ScreenshotSharingEvent.LinkCreated -> "linkCreated ${response.url}"
        ScreenshotSharingEvent.ShareSheetOpened -> "shareSheetOpened"
        is ScreenshotSharingEvent.Failed -> "failed ${throwable.message}"
    }

    // Sharing

    @Test
    fun screenshotCreatesTheLinkThenOpensShareSheetWithMessageAndLink() {
        start()

        screenCapture.takeScreenshot()
        assertEquals(listOf("creatingLink"), events)
        assertEquals(1, linkCreator.bodies.size)
        assertTrue(shareSheet.opened.isEmpty())

        linkCreator.finish()

        assertEquals(listOf("creatingLink", "linkCreated $LINK", "shareSheetOpened"), events)
        assertEquals(listOf("Read it free\n$LINK" to "Title"), shareSheet.opened)
    }

    @Test
    fun screenshotSendsTheBodyFromShare() {
        start(share("article-7"))

        screenCapture.takeScreenshot()

        val body = linkCreator.bodies.single()
        assertEquals(ContentType.SCREENSHOT, body.contentSetting.type)
        assertEquals("https://news.example/article-7", body.contentSetting.commonData?.source)
        assertEquals(mapOf("articleId" to "article-7"), body.extra)
    }

    @Test
    fun withoutMessageOnlyTheLinkIsShared() {
        start(share().copy(message = null))

        screenCapture.takeScreenshot()
        linkCreator.finish()

        assertEquals(LINK, shareSheet.opened.single().first)
    }

    @Test
    fun withoutShareScreenshotIsIgnored() {
        start(share = null)

        screenCapture.takeScreenshot()

        assertTrue(linkCreator.bodies.isEmpty())
        assertTrue(events.isEmpty())
    }

    @Test
    fun failedLinkReportsErrorAndOpensNoShareSheet() {
        start()

        screenCapture.takeScreenshot()
        linkCreator.finish(Result.failure(Throwable("Quota exceeded")))

        assertEquals(listOf("creatingLink", "failed Quota exceeded"), events)
        assertTrue(shareSheet.opened.isEmpty())
    }

    @Test
    fun linkWithoutUrlIsAFailure() {
        start()

        screenCapture.takeScreenshot()
        linkCreator.finish(Result.success(response(url = null)))

        assertEquals("failed The created link has no url", events.last())
        assertTrue(shareSheet.opened.isEmpty())
    }

    @Test
    fun afterFailedLinkNextScreenshotTriesAgain() {
        start()
        screenCapture.takeScreenshot()
        linkCreator.finish(Result.failure(Throwable("Offline")))

        screenCapture.takeScreenshot()

        assertEquals(2, linkCreator.bodies.size)
    }

    // Turning it on and off

    @Test
    fun screenshotsAreReportedOnlyWhileTheScreenIsStarted() {
        val session = controller.start(screenCapture, shareSheet, onEvent = null, share = { share() })
        assertEquals(0, screenCapture.registered)

        session.onStarted()
        assertEquals(1, screenCapture.registered)

        session.onStopped()
        assertEquals(0, screenCapture.registered)
    }

    @Test
    fun stopWhileCreatingLinkDropsIt() {
        start()
        screenCapture.takeScreenshot()

        controller.stop()

        assertEquals(1, linkCreator.cancelled)
        assertTrue(shareSheet.opened.isEmpty())
        assertEquals(0, screenCapture.registered)
    }

    @Test
    fun screenStoppedWhileCreatingLinkDropsIt() {
        val session = start()
        screenCapture.takeScreenshot()

        session.onPaused()
        session.onStopped()

        assertEquals(1, linkCreator.cancelled)
    }

    @Test
    fun endedSessionIsDetachedFromItsLifecycle() {
        val session = start()
        var detached = false
        session.detach = { detached = true }

        session.end()

        assertTrue(detached)
    }

    @Test
    fun startingAnotherScreenEndsTheEarlierOne() {
        val first = start()
        var firstDetached = false
        first.detach = { firstDetached = true }
        screenCapture.takeScreenshot()

        val otherCapture = FakeScreenCapture()
        start(share("article-2"), otherCapture)
        first.onResumed()

        assertTrue(firstDetached)
        assertEquals(1, linkCreator.cancelled)
        assertEquals(0, screenCapture.registered)
        assertEquals(1, otherCapture.registered)
    }

    // Repeated screenshots

    @Test
    fun screenshotsWhileCreatingLinkOrSharingAreIgnored() {
        val session = start()

        screenCapture.takeScreenshot()
        screenCapture.takeScreenshot()
        linkCreator.finish()
        screenCapture.takeScreenshot()

        assertEquals(1, linkCreator.bodies.size)
        assertEquals(1, shareSheet.opened.size)

        // Back from the share sheet
        session.onPaused()
        session.onResumed()
        screenCapture.takeScreenshot()

        assertEquals(2, shareSheet.opened.size)
    }

    @Test
    fun screenshotOfSameContentAgainReusesTheLink() {
        start()
        screenCapture.takeScreenshot()
        linkCreator.finish()

        controller.stop()
        start()
        screenCapture.takeScreenshot()

        assertEquals(1, linkCreator.bodies.size)
        assertEquals(2, shareSheet.opened.size)
        assertEquals("shareSheetOpened", events.last())
        assertEquals("linkCreated $LINK", events[events.size - 2])
    }

    @Test
    fun screenshotOfOtherContentCreatesAnotherLink() {
        start()
        screenCapture.takeScreenshot()
        linkCreator.finish()

        start(share("article-2"))
        screenCapture.takeScreenshot()

        assertEquals(2, linkCreator.bodies.size)
    }

    @Test
    fun whenShareSheetCantOpenNextScreenshotIsHandled() {
        shareSheet.canOpen = false
        start()
        screenCapture.takeScreenshot()
        linkCreator.finish()

        shareSheet.canOpen = true
        screenCapture.takeScreenshot()

        assertEquals(1, shareSheet.opened.size)
    }

    // Screen not resumed

    @Test
    fun linkReadyWhilePausedOpensShareSheetOnResume() {
        val session = start()
        screenCapture.takeScreenshot()

        session.onPaused()
        linkCreator.finish()
        assertTrue(shareSheet.opened.isEmpty())

        session.onResumed()

        assertEquals(1, shareSheet.opened.size)
    }

    @Test
    fun stopWhileWaitingForResumeOpensNoShareSheet() {
        val session = start()
        screenCapture.takeScreenshot()
        session.onPaused()
        linkCreator.finish()

        session.onStopped()
        session.onStarted()
        session.onResumed()

        assertTrue(shareSheet.opened.isEmpty())
    }
}

private fun response(url: String? = LINK) = CreateLinkResponse(
    url = url,
    remainingQuota = 3,
    isEmailMandatory = false,
    modalTitle = null,
    buttonText = null
)

private class FakeLinkCreator : LinkCreator {
    val bodies = mutableListOf<CreateLinkBody>()
    var cancelled = 0
    private val pending = mutableListOf<(Result<CreateLinkResponse>) -> Unit>()

    override fun createLink(body: CreateLinkBody, onResult: (Result<CreateLinkResponse>) -> Unit): () -> Unit {
        bodies += body
        pending += onResult
        return {
            cancelled++
            pending.remove(onResult)
        }
    }

    fun finish(result: Result<CreateLinkResponse> = Result.success(response())) {
        pending.removeAt(0)(result)
    }
}

private class FakeScreenCapture : ScreenCapture {
    private val callbacks = mutableListOf<() -> Unit>()
    val registered get() = callbacks.size

    override fun register(onCaptured: () -> Unit): (() -> Unit) {
        callbacks += onCaptured
        return { callbacks.remove(onCaptured) }
    }

    fun takeScreenshot() {
        callbacks.toList().forEach { it() }
    }
}

private class FakeShareSheet : ShareSheet {
    var canOpen = true
    val opened = mutableListOf<Pair<String, String?>>()

    override fun open(text: String, title: String?): Boolean {
        if (!canOpen) return false
        opened += text to title
        return true
    }
}
