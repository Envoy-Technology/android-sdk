package com.envoy.androidsdk.data

import com.envoy.androidsdk.data.HawkKeys.HASH
import com.envoy.androidsdk.data.HawkKeys.LEAD_UUID
import com.envoy.androidsdk.domain.shared.HawkRepository
import com.orhanobut.hawk.DefaultHawkFacade

class HawkRepositoryImpl(
    private val hawk: DefaultHawkFacade
) : HawkRepository {

    override fun getHash(): String? {
        return hawk.get(HASH)
    }

    override fun setHash(hash: String) {
        hawk.put(HASH, hash)
    }

    override fun getLeadUiid(): String? {
        return hawk.get(LEAD_UUID)
    }

    override fun setLeadUiid(leadUiid: String) {
        hawk.put(LEAD_UUID, leadUiid)
    }
}

object HawkKeys {
    const val HASH = "hash"
    const val LEAD_UUID = "lead_uuid"
}
