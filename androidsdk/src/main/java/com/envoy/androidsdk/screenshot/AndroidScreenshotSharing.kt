package com.envoy.androidsdk.screenshot

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
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
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

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

    override fun open(content: ShareContent): Boolean {
        if (activity.isFinishing || activity.isDestroyed) return false
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, content.text)
            content.title?.let {
                // Shown as the preview title in the share sheet, and used as the subject by email apps.
                putExtra(Intent.EXTRA_TITLE, it)
                putExtra(Intent.EXTRA_SUBJECT, it)
            }
            // Shown as the preview thumbnail in the share sheet (Android 10+); only the text is shared.
            content.imageUri?.let {
                clipData = ClipData.newRawUri(null, Uri.parse(it))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
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

/**
 * Downloads images to the cache directory, where [EnvoyFileProvider] serves them to the share sheet.
 * An image downloaded earlier is reused.
 */
internal class AndroidImageLoader(
    private val context: Context,
    private val mainThread: MainThread,
    private val timeoutMillis: Long = 2_000,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) : ImageLoader {

    override fun load(url: String, onResult: (String?) -> Unit): () -> Unit {
        // Touched only on the main thread.
        var cancelled = false
        val job = scope.launch {
            val uri = withTimeoutOrNull(timeoutMillis) {
                runInterruptible { runCatching { imageFile(url) }.getOrNull() }
            }?.let { EnvoyFileProvider.uriFor(context, it).toString() }
            mainThread.post { if (!cancelled) onResult(uri) }
        }
        return {
            cancelled = true
            job.cancel()
        }
    }

    private fun imageFile(url: String): File? {
        val dir = File(context.cacheDir, EnvoyFileProvider.SHARE_DIR).apply { mkdirs() }
        val name = MessageDigest.getInstance("SHA-256").digest(url.toByteArray())
            .joinToString("") { "%02x".format(it) }
        dir.listFiles { file -> file.name.startsWith("$name.") }?.firstOrNull()?.let { return it }

        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = timeoutMillis.toInt()
            connection.readTimeout = timeoutMillis.toInt()
            if (connection.responseCode != HttpURLConnection.HTTP_OK) return null
            val extension = when (connection.contentType?.substringBefore(';')?.trim()) {
                "image/jpeg" -> "jpg"
                "image/png" -> "png"
                "image/webp" -> "webp"
                "image/gif" -> "gif"
                else -> return null
            }
            if (connection.contentLengthLong > MAX_IMAGE_BYTES) return null
            val partial = File(dir, "$name.part")
            connection.inputStream.use { input ->
                partial.outputStream().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var total = 0L
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        total += read
                        if (total > MAX_IMAGE_BYTES) {
                            partial.delete()
                            return null
                        }
                        output.write(buffer, 0, read)
                    }
                }
            }
            val file = File(dir, "$name.$extension")
            return if (partial.renameTo(file)) file else null
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val MAX_IMAGE_BYTES = 5L * 1024 * 1024
    }
}

/** Serves the share sheet preview images. Its own class, so it doesn't clash with the host app's FileProvider. */
class EnvoyFileProvider : FileProvider() {

    internal companion object {
        /** In the cache directory, as declared in res/xml/envoy_file_paths.xml. */
        const val SHARE_DIR = "envoy_share"

        fun uriFor(context: Context, file: File): Uri =
            getUriForFile(context, "${context.packageName}.envoy.fileprovider", file)
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
