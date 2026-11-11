package com.muscleplay.app.feature.profile.support.data

import com.muscleplay.app.core.network.ApiClient
import com.muscleplay.app.core.network.NetworkResult
import com.muscleplay.app.core.network.auth.TokenStorageManager
import com.muscleplay.app.core.network.dto.AppInfoContentResponseDto
import com.muscleplay.app.core.network.dto.SupportFaqPageDto
import com.muscleplay.app.core.network.dto.SupportHomeResponseDto

interface SupportRepository {
    suspend fun fetchSupportHome(): NetworkResult<SupportHomeResponseDto>
    suspend fun fetchAboutInfo(forceRefresh: Boolean = false): NetworkResult<AppInfoContentResponseDto>
    suspend fun fetchPrivacyPolicy(forceRefresh: Boolean = false): NetworkResult<AppInfoContentResponseDto>
    suspend fun fetchTermsConditions(forceRefresh: Boolean = false): NetworkResult<AppInfoContentResponseDto>
    suspend fun searchFaqs(
        categoryKey: String? = null,
        query: String? = null,
        page: Int = 0,
        size: Int = 20,
    ): NetworkResult<SupportFaqPageDto>
}

class SupportRepositoryImpl(
    private val apiClient: ApiClient,
    private val tokenStorageManager: TokenStorageManager,
) : SupportRepository {

    private var cachedAboutInfo: AppInfoContentResponseDto? = null
    private var cachedPrivacyPolicy: AppInfoContentResponseDto? = null
    private var cachedTermsConditions: AppInfoContentResponseDto? = null

    override suspend fun fetchSupportHome(): NetworkResult<SupportHomeResponseDto> {
        val accessToken = tokenStorageManager.accessToken ?: ""
        return apiClient.fetchSupportHome(accessToken)
    }

    override suspend fun fetchAboutInfo(forceRefresh: Boolean): NetworkResult<AppInfoContentResponseDto> {
        if (!forceRefresh && cachedAboutInfo != null) {
            return NetworkResult.Success(cachedAboutInfo!!)
        }
        val accessToken = tokenStorageManager.accessToken ?: ""
        return when (val result = apiClient.fetchAboutInfo(accessToken)) {
            is NetworkResult.Success -> {
                cachedAboutInfo = result.data
                result
            }

            else -> result
        }
    }

    override suspend fun fetchPrivacyPolicy(forceRefresh: Boolean): NetworkResult<AppInfoContentResponseDto> {
        if (!forceRefresh && cachedPrivacyPolicy != null) {
            return NetworkResult.Success(cachedPrivacyPolicy!!)
        }
        val accessToken = tokenStorageManager.accessToken ?: ""
        return when (val result = apiClient.fetchPrivacyPolicy(accessToken)) {
            is NetworkResult.Success -> {
                cachedPrivacyPolicy = result.data
                result
            }

            else -> result
        }
    }

    override suspend fun fetchTermsConditions(forceRefresh: Boolean): NetworkResult<AppInfoContentResponseDto> {
        if (!forceRefresh && cachedTermsConditions != null) {
            return NetworkResult.Success(cachedTermsConditions!!)
        }
        val accessToken = tokenStorageManager.accessToken ?: ""
        return when (val result = apiClient.fetchTermsConditions(accessToken)) {
            is NetworkResult.Success -> {
                cachedTermsConditions = result.data
                result
            }

            else -> result
        }
    }

    override suspend fun searchFaqs(
        categoryKey: String?,
        query: String?,
        page: Int,
        size: Int,
    ): NetworkResult<SupportFaqPageDto> {
        val accessToken = tokenStorageManager.accessToken ?: ""
        return apiClient.searchFaqs(accessToken, categoryKey, query, page, size)
    }
}
