package com.muscleplay.app.core.network.api

import com.muscleplay.app.core.network.NetworkResult
import com.muscleplay.app.core.network.dto.AnalyticsHomeResponseDto
import com.muscleplay.app.core.network.dto.CalculatorHomeResponseDto
import com.muscleplay.app.core.network.dto.SmartCalculationRequest
import kotlinx.serialization.json.JsonElement

interface AnalyticsApiService {
    suspend fun fetchAnalyticsHome(accessToken: String): NetworkResult<AnalyticsHomeResponseDto>
    suspend fun fetchCalculatorHome(accessToken: String): NetworkResult<CalculatorHomeResponseDto>
    suspend fun calculateSmart(
        accessToken: String,
        calculatorKey: String,
        request: SmartCalculationRequest,
    ): NetworkResult<Map<String, JsonElement>>
}
