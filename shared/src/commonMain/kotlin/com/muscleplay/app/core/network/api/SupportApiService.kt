package com.muscleplay.app.core.network.api

import com.muscleplay.app.core.network.NetworkResult
import com.muscleplay.app.core.network.dto.AppInfoContentResponseDto
import com.muscleplay.app.core.network.dto.SupportFaqPageDto
import com.muscleplay.app.core.network.dto.SupportHomeResponseDto

interface SupportApiService {
    suspend fun fetchSupportHome(accessToken: String): NetworkResult<SupportHomeResponseDto>
    suspend fun fetchAboutInfo(accessToken: String): NetworkResult<AppInfoContentResponseDto>
    suspend fun fetchPrivacyPolicy(accessToken: String): NetworkResult<AppInfoContentResponseDto>
    suspend fun fetchTermsConditions(accessToken: String): NetworkResult<AppInfoContentResponseDto>
    suspend fun searchFaqs(
        accessToken: String,
        categoryKey: String? = null,
        query: String? = null,
        page: Int = 0,
        size: Int = 20,
    ): NetworkResult<SupportFaqPageDto>
}
