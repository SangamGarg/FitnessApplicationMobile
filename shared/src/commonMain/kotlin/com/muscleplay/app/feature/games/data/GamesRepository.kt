package com.muscleplay.app.feature.games.data

import com.muscleplay.app.core.network.ApiClient
import com.muscleplay.app.core.network.NetworkResult
import com.muscleplay.app.core.network.auth.TokenStorageManager
import com.muscleplay.app.core.network.dto.GameCompleteRequestDto
import com.muscleplay.app.core.network.dto.GameDetailResponseDto
import com.muscleplay.app.core.network.dto.GameHistoryPageResponse
import com.muscleplay.app.core.network.dto.GameSessionResponseDto
import com.muscleplay.app.core.network.dto.GameStartRequestDto
import com.muscleplay.app.core.network.dto.GamificationHomeSummaryResponseDto
import com.muscleplay.app.core.network.dto.RewardHistoryPageResponse

interface GamesRepository {
    suspend fun fetchGamificationHome(forceRefresh: Boolean = false): NetworkResult<GamificationHomeSummaryResponseDto>
    suspend fun fetchGameDetails(gameType: String): NetworkResult<GameDetailResponseDto>
    suspend fun startGameSession(request: GameStartRequestDto): NetworkResult<GameSessionResponseDto>
    suspend fun completeGameSession(sessionId: String, request: GameCompleteRequestDto): NetworkResult<GameSessionResponseDto>
    suspend fun fetchRewardHistory(page: Int = 0, size: Int = 20): NetworkResult<RewardHistoryPageResponse>
    suspend fun fetchGameHistory(page: Int = 0, size: Int = 20): NetworkResult<GameHistoryPageResponse>
    fun clearCache()
}

class GamesRepositoryImpl(
    private val apiClient: ApiClient,
    private val tokenStorageManager: TokenStorageManager,
) : GamesRepository {

    private var cachedHomeSummary: GamificationHomeSummaryResponseDto? = null

    override suspend fun fetchGamificationHome(forceRefresh: Boolean): NetworkResult<GamificationHomeSummaryResponseDto> {
        if (!forceRefresh && cachedHomeSummary != null) {
            return NetworkResult.Success(cachedHomeSummary!!)
        }
        val accessToken = tokenStorageManager.accessToken ?: ""
        return when (val result = apiClient.fetchGamificationHome(accessToken)) {
            is NetworkResult.Success -> {
                cachedHomeSummary = result.data
                result
            }

            else -> result
        }
    }

    override suspend fun fetchGameDetails(gameType: String): NetworkResult<GameDetailResponseDto> {
        val accessToken = tokenStorageManager.accessToken ?: ""
        return apiClient.fetchGameDetails(accessToken, gameType)
    }

    override suspend fun startGameSession(request: GameStartRequestDto): NetworkResult<GameSessionResponseDto> {
        val accessToken = tokenStorageManager.accessToken ?: ""
        return apiClient.startGameSession(accessToken, request)
    }

    override suspend fun completeGameSession(
        sessionId: String,
        request: GameCompleteRequestDto,
    ): NetworkResult<GameSessionResponseDto> {
        val accessToken = tokenStorageManager.accessToken ?: ""
        val result = apiClient.completeGameSession(accessToken, sessionId, request)
        if (result is NetworkResult.Success) {
            cachedHomeSummary = null
        }
        return result
    }

    override suspend fun fetchRewardHistory(page: Int, size: Int): NetworkResult<RewardHistoryPageResponse> {
        val accessToken = tokenStorageManager.accessToken ?: ""
        return apiClient.fetchRewardHistory(accessToken, page, size)
    }

    override suspend fun fetchGameHistory(page: Int, size: Int): NetworkResult<GameHistoryPageResponse> {
        val accessToken = tokenStorageManager.accessToken ?: ""
        return apiClient.fetchGameHistory(accessToken, page, size)
    }

    override fun clearCache() {
        cachedHomeSummary = null
    }
}
