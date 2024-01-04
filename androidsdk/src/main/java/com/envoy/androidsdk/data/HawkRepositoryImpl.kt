package com.envoy.androidsdk.data

import com.envoy.androidsdk.data.HawkKeys.HASH
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
}

object HawkKeys {
    const val HASH = "hash"
}
