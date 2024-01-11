package com.envoy.androidsdk.domain.shared

interface HawkRepository {
    fun getHash(): String?

    fun setHash(hash: String)

    fun getLeadUiid(): String?

    fun setLeadUiid(leadUiid: String)
}
