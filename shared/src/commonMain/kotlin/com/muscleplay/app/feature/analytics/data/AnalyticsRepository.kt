package com.muscleplay.app.feature.analytics.data

import com.muscleplay.app.core.network.ApiClient
import com.muscleplay.app.core.network.NetworkResult
import com.muscleplay.app.core.network.auth.TokenStorageManager
import com.muscleplay.app.core.network.dto.AnalyticsHomeResponseDto
import com.muscleplay.app.core.network.dto.CalculatorHomeResponseDto
import com.muscleplay.app.core.network.dto.SmartCalculationRequest
import kotlinx.serialization.json.JsonElement

interface AnalyticsRepository {
    suspend fun fetchAnalyticsHome(forceRefresh: Boolean = false): NetworkResult<AnalyticsHomeResponseDto>
    suspend fun fetchCalculatorHome(forceRefresh: Boolean = false): NetworkResult<CalculatorHomeResponseDto>
    suspend fun calculateSmart(
        calculatorKey: String,
        request: SmartCalculationRequest,
    ): NetworkResult<Map<String, JsonElement>>
    fun clearCache()
}

class AnalyticsRepositoryImpl(
    private val apiClient: ApiClient,
    private val tokenStorageManager: TokenStorageManager,
) : AnalyticsRepository {

    private var cachedAnalyticsHome: AnalyticsHomeResponseDto? = null
    private var cachedCalculatorHome: CalculatorHomeResponseDto? = null

    override suspend fun fetchAnalyticsHome(forceRefresh: Boolean): NetworkResult<AnalyticsHomeResponseDto> {
        if (!forceRefresh && cachedAnalyticsHome != null) {
            return NetworkResult.Success(cachedAnalyticsHome!!)
        }
        val accessToken = tokenStorageManager.accessToken ?: ""
        return when (val result = apiClient.fetchAnalyticsHome(accessToken)) {
            is NetworkResult.Success -> {
                cachedAnalyticsHome = result.data
                result
            }

            else -> result
        }
    }

    override suspend fun fetchCalculatorHome(forceRefresh: Boolean): NetworkResult<CalculatorHomeResponseDto> {
        if (!forceRefresh && cachedCalculatorHome != null) {
            return NetworkResult.Success(cachedCalculatorHome!!)
        }
        val accessToken = tokenStorageManager.accessToken ?: ""
        return when (val result = apiClient.fetchCalculatorHome(accessToken)) {
            is NetworkResult.Success -> {
                cachedCalculatorHome = result.data
                result
            }

            else -> result
        }
    }

    override suspend fun calculateSmart(
        calculatorKey: String,
        request: SmartCalculationRequest,
    ): NetworkResult<Map<String, JsonElement>> {
        val accessToken = tokenStorageManager.accessToken ?: ""
        return apiClient.calculateSmart(accessToken, calculatorKey, request)
    }

    override fun clearCache() {
        cachedAnalyticsHome = null
        cachedCalculatorHome = null
    }
}
