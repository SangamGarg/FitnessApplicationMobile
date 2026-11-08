package com.muscleplay.app.feature.home.data

import com.muscleplay.app.core.network.ApiClient
import com.muscleplay.app.core.network.NetworkResult
import com.muscleplay.app.core.network.auth.TokenStorageManager
import com.muscleplay.app.core.network.auth.UserSessionRepository
import com.muscleplay.app.core.network.dto.DashboardResponseDto

interface DashboardRepository {
    suspend fun getDashboard(): NetworkResult<DashboardResponseDto>
}

class DashboardRepositoryImpl(
    private val apiClient: ApiClient,
    private val tokenStorageManager: TokenStorageManager,
    private val userSessionRepository: UserSessionRepository? = null,
) : DashboardRepository {
    override suspend fun getDashboard(): NetworkResult<DashboardResponseDto> {
        val accessToken = tokenStorageManager.accessToken ?: ""
        val result = apiClient.fetchDashboard(accessToken)
        if (result is NetworkResult.Success && result.data.user != null) {
            userSessionRepository?.updateProfileFromSummary(result.data.user)
        }
        return result
    }
}
