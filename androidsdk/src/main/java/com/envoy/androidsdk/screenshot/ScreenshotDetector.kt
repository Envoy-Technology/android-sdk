package com.envoy.androidsdk.screenshot

import android.util.Log
import android.content.ContentResolver
import android.database.ContentObserver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import java.io.InputStream

private val TAG = ScreenshotDetector::class.java.name

/**
 * Detects when a user takes a screenshot on the device.
 *
 * Usage:
 * ```
 * val detector = ScreenshotDetector(contentResolver) { bitmap ->
 *     // Handle the screenshot bitmap
 *     val base64 = bitmap.toBase64()
 *     // Create link with screenshot...
 * }
 * detector.start()
 * // When done:
 * detector.stop()
 * ```
 *
 * Note: Requires READ_EXTERNAL_STORAGE or READ_MEDIA_IMAGES permission depending on API level.
 */
class ScreenshotDetector(
    private val contentResolver: ContentResolver,
    private val onScreenshotTaken: (Bitmap) -> Unit
) {
    private var contentObserver: ContentObserver? = null
    private var isRunning = false
    private var lastProcessedUri: Uri? = null
    private var lastProcessedTime: Long = 0

    private val handler = Handler(Looper.getMainLooper())

    /**
     * Starts listening for screenshot events.
     * Make sure to call [stop] when you no longer need to detect screenshots.
     */
    fun start() {
        Log.d(TAG, "Starting screenshot detection...")
        if (isRunning) return

        contentObserver = object : ContentObserver(handler) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                super.onChange(selfChange, uri)
                uri?.let { handlePotentialScreenshot(it) }
            }
        }

        val externalContentUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        contentResolver.registerContentObserver(
            externalContentUri,
            true,
            contentObserver!!
        )

        isRunning = true
    }

    /**
     * Stops listening for screenshot events.
     */
    fun stop() {
        contentObserver?.let {
            contentResolver.unregisterContentObserver(it)
        }
        contentObserver = null
        isRunning = false
    }

    private fun handlePotentialScreenshot(uri: Uri, attempt: Int = 0) {
        Log.d(TAG, "Potential screenshot detected: $uri (attempt=$attempt)")

        // The observer can fire with a base URI like `content://media/external` that
        // doesn't reference a specific image row. Querying such URIs throws
        // "Unknown URL" on some devices, so skip anything without a numeric id suffix.
        val lastSegment = uri.lastPathSegment
        if (lastSegment == null || lastSegment.toLongOrNull() == null) {
            Log.d(TAG, "Skipping non-numeric URI: $uri")
            return
        }

        val currentTime = System.currentTimeMillis()
        if (attempt == 0 &&
            uri == lastProcessedUri &&
            currentTime - lastProcessedTime < DEBOUNCE_TIME_MS
        ) {
            return
        }

        try {
            val projection = arrayOf(
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED
            )

            var rowFound = false
            var matchedScreenshot = false

            contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    rowFound = true
                    val dataIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
                    val nameIndex = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)

                    val path = if (dataIndex >= 0) cursor.getString(dataIndex) else null
                    val name = if (nameIndex >= 0) cursor.getString(nameIndex) else null

                    if (isScreenshot(path, name)) {
                        matchedScreenshot = true
                        val bitmap = loadBitmapFromUri(uri)
                        if (bitmap != null) {
                            lastProcessedUri = uri
                            lastProcessedTime = currentTime
                            handler.post { onScreenshotTaken(bitmap) }
                        } else if (attempt < MAX_RETRIES) {
                            // File not fully written yet; retry shortly.
                            Log.d(TAG, "Bitmap not ready, retrying $uri")
                            scheduleRetry(uri, attempt)
                            return
                        } else {
                            Log.w(TAG, "Giving up loading bitmap for $uri after $attempt retries")
                        }
                    }
                }
            }

            // Row may not exist yet (pending insert) — retry.
            if (!rowFound && attempt < MAX_RETRIES) {
                Log.d(TAG, "Row not ready, retrying $uri")
                scheduleRetry(uri, attempt)
                return
            }

            if (rowFound && !matchedScreenshot) {
                Log.d(TAG, "URI $uri is not a screenshot")
            }
        } catch (e: Exception) {
            // Silently handle exceptions - screenshot detection is best-effort
            Log.e(TAG, "Failed to handle potential screenshot", e)
            if (attempt < MAX_RETRIES) {
                scheduleRetry(uri, attempt)
            }
        }
    }

    private fun scheduleRetry(uri: Uri, attempt: Int) {
        handler.postDelayed({
            handlePotentialScreenshot(uri, attempt + 1)
        }, RETRY_DELAY_MS)
    }

    private fun isScreenshot(path: String?, name: String?): Boolean {
        Log.d(TAG, "Checking if $path or $name is a screenshot")
        val pathLower = path?.lowercase() ?: ""
        val nameLower = name?.lowercase() ?: ""

        return SCREENSHOT_KEYWORDS.any { keyword ->
            pathLower.contains(keyword) || nameLower.contains(keyword)
        }
    }

    private fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            inputStream?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        private const val DEBOUNCE_TIME_MS = 1000L
        private const val RETRY_DELAY_MS = 300L
        private const val MAX_RETRIES = 5

        private val SCREENSHOT_KEYWORDS = listOf(
            "screenshot",
            "screen_shot",
            "screen-shot",
            "screencapture",
            "screen_capture",
            "screen-capture"
        )
    }
}
