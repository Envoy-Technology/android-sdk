package com.envoy.androidsdk.deferredlink

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.view.textclassifier.TextClassifier

internal interface DeferredLinkClipboard {
    /**
     * Reports whether the clipboard may hold a web URL, without reading its content.
     * Only meaningful while the app has focus: without it Android reports an empty clipboard.
     */
    fun mayContainWebUrl(): Boolean

    /** Reads the clipboard content. On Android 12+ this shows the system "pasted from your clipboard" toast. */
    fun readText(): String?
}

internal class SystemDeferredLinkClipboard(context: Context) : DeferredLinkClipboard {

    private val clipboardManager = context.getSystemService(ClipboardManager::class.java)

    override fun mayContainWebUrl(): Boolean {
        // Reading the description doesn't show the clipboard access toast.
        val description = clipboardManager.primaryClipDescription ?: return false
        if (!description.hasMimeType("text/*")) return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            description.classificationStatus == ClipDescription.CLASSIFICATION_COMPLETE
        ) {
            return description.getConfidenceScore(TextClassifier.TYPE_URL) > 0f
        }
        // Not classified (yet, or before Android 12), so the content is worth reading.
        return true
    }

    override fun readText(): String? {
        val clip = clipboardManager.primaryClip ?: return null
        if (clip.itemCount == 0) return null
        return clip.getItemAt(0).text?.toString()
    }
}
