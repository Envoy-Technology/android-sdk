package com.envoy.androidsdk.api

import com.envoy.androidsdk.domain.model.ClaimUserRewardBody
import com.envoy.androidsdk.domain.model.ClaimUserRewardResponse
import com.envoy.androidsdk.domain.model.CreateLinkBody
import com.envoy.androidsdk.domain.model.CreateLinkResponse
import com.envoy.androidsdk.domain.model.CreatePixelEventBody
import com.envoy.androidsdk.domain.model.GetUserRewardResponse
import com.envoy.androidsdk.domain.model.UserCurrentRewardsResponse
import com.envoy.androidsdk.domain.model.UserQuotaResponse
import com.envoy.androidsdk.domain.shared.Resource
import kotlinx.coroutines.flow.Flow

interface EnvoyApi {

    fun createLink(body: CreateLinkBody): Flow<Resource<CreateLinkResponse>>

    fun getUserQuota(userId: String): Flow<Resource<UserQuotaResponse>>

    fun createPixelEvent(body: CreatePixelEventBody): Flow<Resource<Unit>>

    fun getUserReward(userId: String): Flow<Resource<GetUserRewardResponse>>

    fun claimUserReward(body: ClaimUserRewardBody): Flow<Resource<ClaimUserRewardResponse>>

    fun getUserCurrentRewards(userId: String): Flow<Resource<UserCurrentRewardsResponse>>
}
