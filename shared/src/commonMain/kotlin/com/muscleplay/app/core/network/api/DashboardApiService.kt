package com.muscleplay.app.core.network.api

import com.muscleplay.app.core.network.NetworkResult
import com.muscleplay.app.core.network.dto.DashboardResponseDto

interface DashboardApiService {
    suspend fun fetchDashboard(accessToken: String): NetworkResult<DashboardResponseDto>
}
