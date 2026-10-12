package com.envoy.androidsdk.deferredlink

import android.os.Handler
import android.os.Looper

/** The thread the deferred-link state is touched on. */
internal interface MainThread {
    fun isCurrent(): Boolean

    fun post(action: () -> Unit)

    /** Returns a function that cancels the action if it hasn't run yet. */
    fun postDelayed(delayMillis: Long, action: () -> Unit): () -> Unit
}

internal fun MainThread.runOrPost(action: () -> Unit) {
    if (isCurrent()) action() else post(action)
}

internal class AndroidMainThread : MainThread {

    private val handler = Handler(Looper.getMainLooper())

    override fun isCurrent(): Boolean = Looper.myLooper() == Looper.getMainLooper()

    override fun post(action: () -> Unit) {
        handler.post(action)
    }

    override fun postDelayed(delayMillis: Long, action: () -> Unit): () -> Unit {
        val runnable = Runnable(action)
        handler.postDelayed(runnable, delayMillis)
        return { handler.removeCallbacks(runnable) }
    }
}
