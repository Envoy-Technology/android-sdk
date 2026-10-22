package com.envoy.androidsdk.screenshot

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import com.envoy.androidsdk.deferredlink.MainThread
import com.envoy.androidsdk.domain.model.CreateLinkBody
import com.envoy.androidsdk.domain.model.CreateLinkResponse
import com.envoy.androidsdk.domain.shared.Failure
import com.envoy.androidsdk.domain.shared.Loading
import com.envoy.androidsdk.domain.shared.Resource
import com.envoy.androidsdk.domain.shared.Success
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Needs no permission to read the screen: Android only reports that a screenshot was taken. */
internal class AndroidScreenCapture(private val activity: Activity) : ScreenCapture {

    override fun register(onCaptured: () -> Unit): (() -> Unit)? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return null
        val callback = Activity.ScreenCaptureCallback { onCaptured() }
        activity.registerScreenCaptureCallback(activity.mainExecutor, callback)
        return { activity.unregisterScreenCaptureCallback(callback) }
    }
}

internal class AndroidShareSheet(private val activity: Activity) : ShareSheet {

    override fun open(text: String, title: String?): Boolean {
        if (activity.isFinishing || activity.isDestroyed) return false
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            // Shown as the preview title in the share sheet.
            title?.let { putExtra(Intent.EXTRA_TITLE, it) }
        }
        return try {
            activity.startActivity(Intent.createChooser(send, null))
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }
}

internal class FlowLinkCreator(
    private val createLink: (CreateLinkBody) -> Flow<Resource<CreateLinkResponse>>,
    private val mainThread: MainThread,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) : LinkCreator {

    override fun createLink(body: CreateLinkBody, onResult: (Result<CreateLinkResponse>) -> Unit): () -> Unit {
        // Touched only on the main thread.
        var cancelled = false
        val job = scope.launch {
            val result = when (val resource = createLink(body).first { it !is Loading }) {
                is Success -> Result.success(resource.value)
                is Failure -> Result.failure(resource.throwable)
                is Loading -> return@launch
            }
            mainThread.post { if (!cancelled) onResult(result) }
        }
        return {
            cancelled = true
            job.cancel()
        }
    }
}

/** Drives a session with the lifecycle of its screen, and ends it when the screen is destroyed. */
internal class ScreenshotSharingLifecycleObserver(
    private val session: ScreenshotSharingController.Session
) : DefaultLifecycleObserver {

    override fun onStart(owner: LifecycleOwner) = session.onStarted()
    override fun onResume(owner: LifecycleOwner) = session.onResumed()
    override fun onPause(owner: LifecycleOwner) = session.onPaused()
    override fun onStop(owner: LifecycleOwner) = session.onStopped()
    override fun onDestroy(owner: LifecycleOwner) = session.end()
}
