package com.envoy.androidsdk.domain.shared

interface HawkRepository {
    fun getHash(): String?

    fun setHash(hash: String)

    fun getLeadUiid(): String?

    fun setLeadUiid(leadUiid: String)

    /** `null` when it was never stored, i.e. before the first start of an SDK with the explicit deferred-link check. */
    fun isDeferredLinkCheckDone(): Boolean?

    fun setDeferredLinkCheckDone(done: Boolean)
}
