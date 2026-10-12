package com.envoy.androidsdk.deferredlink

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.view.ViewTreeObserver
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.lang.ref.WeakReference

/** App lifecycle as seen by the deferred-link check. Everything except [isInBackground] is main-thread only. */
internal interface AppState {
    /** Set when the app goes to the background, cleared when it comes back. */
    val isInBackground: StateFlow<Boolean>

    /** Whether the resumed activity has window focus, which Android requires for clipboard access. */
    val hasFocus: Boolean

    /** Runs [action] the next time the resumed activity gains window focus. Replaces an earlier one. */
    fun awaitFocus(action: () -> Unit)

    fun stopAwaitingFocus()
}

internal class AndroidAppState(
    application: Application,
    mainThread: MainThread
) : AppState, Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {

    override val isInBackground = MutableStateFlow(false)

    private var resumedActivity: WeakReference<Activity>? = null
    private var pendingFocusAction: (() -> Unit)? = null
    private val focusListener = ViewTreeObserver.OnWindowFocusChangeListener { hasFocus ->
        if (hasFocus) onFocusGained()
    }

    init {
        application.registerActivityLifecycleCallbacks(this)
        mainThread.runOrPost { ProcessLifecycleOwner.get().lifecycle.addObserver(this) }
    }

    override val hasFocus: Boolean
        get() = resumedActivity?.get()?.hasWindowFocus() == true

    override fun awaitFocus(action: () -> Unit) {
        pendingFocusAction = action
    }

    override fun stopAwaitingFocus() {
        pendingFocusAction = null
    }

    private fun onFocusGained() {
        val action = pendingFocusAction ?: return
        pendingFocusAction = null
        action()
    }

    // Process lifecycle: ON_STOP comes once the last activity has stopped, ignoring configuration changes.
    override fun onStart(owner: LifecycleOwner) {
        isInBackground.value = false
    }

    override fun onStop(owner: LifecycleOwner) {
        isInBackground.value = true
    }

    // Activity lifecycle

    override fun onActivityResumed(activity: Activity) {
        resumedActivity?.get()?.let(::stopObservingFocus)
        resumedActivity = WeakReference(activity)
        activity.window?.decorView?.viewTreeObserver?.addOnWindowFocusChangeListener(focusListener)
        if (activity.hasWindowFocus()) onFocusGained()
    }

    override fun onActivityPaused(activity: Activity) {
        if (resumedActivity?.get() === activity) {
            stopObservingFocus(activity)
            resumedActivity = null
        }
    }

    private fun stopObservingFocus(activity: Activity) {
        val observer = activity.window?.decorView?.viewTreeObserver ?: return
        if (observer.isAlive) observer.removeOnWindowFocusChangeListener(focusListener)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit
}
