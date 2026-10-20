package com.envoy.androidsdk.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.envoy.androidsdk.data.StorageKeys.DEFERRED_LINK_CHECK_DONE
import com.envoy.androidsdk.data.StorageKeys.HASH
import com.envoy.androidsdk.data.StorageKeys.LEAD_UUID
import com.envoy.androidsdk.domain.shared.SdkStorage

class SharedPreferencesStorage(
    private val preferences: SharedPreferences
) : SdkStorage {

    constructor(context: Context) : this(
        context.applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    )

    override fun getHash(): String? {
        return preferences.getString(HASH, null)
    }

    override fun setHash(hash: String) {
        preferences.edit { putString(HASH, hash) }
    }

    override fun getLeadUiid(): String? {
        return preferences.getString(LEAD_UUID, null)
    }

    override fun setLeadUiid(leadUiid: String) {
        preferences.edit { putString(LEAD_UUID, leadUiid) }
    }

    override fun isDeferredLinkCheckDone(): Boolean? {
        if (!preferences.contains(DEFERRED_LINK_CHECK_DONE)) return null
        return preferences.getBoolean(DEFERRED_LINK_CHECK_DONE, false)
    }

    override fun setDeferredLinkCheckDone(done: Boolean) {
        preferences.edit { putBoolean(DEFERRED_LINK_CHECK_DONE, done) }
    }

    private companion object {
        // Our own file, so nothing is shared with the host app's preferences.
        const val PREFERENCES_NAME = "com.envoy.androidsdk"
    }
}

object StorageKeys {
    const val HASH = "hash"
    const val LEAD_UUID = "lead_uuid"
    const val DEFERRED_LINK_CHECK_DONE = "isDeferredLinkCheckDone"
}
