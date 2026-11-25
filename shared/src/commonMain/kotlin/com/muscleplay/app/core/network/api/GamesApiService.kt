package com.muscleplay.app.core.network.api

import com.muscleplay.app.core.network.NetworkResult
import com.muscleplay.app.core.network.dto.GameCompleteRequestDto
import com.muscleplay.app.core.network.dto.GameDetailResponseDto
import com.muscleplay.app.core.network.dto.GameHistoryPageResponse
import com.muscleplay.app.core.network.dto.GameSessionResponseDto
import com.muscleplay.app.core.network.dto.GameStartRequestDto
import com.muscleplay.app.core.network.dto.GamificationHomeSummaryResponseDto
import com.muscleplay.app.core.network.dto.RewardHistoryPageResponse

interface GamesApiService {
    suspend fun fetchGamificationHome(accessToken: String): NetworkResult<GamificationHomeSummaryResponseDto>
    suspend fun fetchGameDetails(accessToken: String, gameType: String): NetworkResult<GameDetailResponseDto>
    suspend fun startGameSession(accessToken: String, request: GameStartRequestDto): NetworkResult<GameSessionResponseDto>
    suspend fun completeGameSession(
        accessToken: String,
        sessionId: String,
        request: GameCompleteRequestDto,
    ): NetworkResult<GameSessionResponseDto>
    suspend fun fetchRewardHistory(accessToken: String, page: Int = 0, size: Int = 20): NetworkResult<RewardHistoryPageResponse>
    suspend fun fetchGameHistory(accessToken: String, page: Int = 0, size: Int = 20): NetworkResult<GameHistoryPageResponse>
}
