package com.muscleplay.app.feature.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muscleplay.app.core.network.NetworkResult
import com.muscleplay.app.core.network.dto.GameCompleteRequestDto
import com.muscleplay.app.core.network.dto.GameDetailResponseDto
import com.muscleplay.app.core.network.dto.GameSessionResponseDto
import com.muscleplay.app.core.network.dto.GameStartRequestDto
import com.muscleplay.app.core.network.dto.GamificationHomeSummaryResponseDto
import com.muscleplay.app.core.network.dto.RewardHistoryItemDto
import com.muscleplay.app.feature.games.data.GamesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

sealed interface GamificationHomeUiState {
    data object Loading : GamificationHomeUiState
    data class Success(val summary: GamificationHomeSummaryResponseDto) : GamificationHomeUiState
    data class Error(val message: String) : GamificationHomeUiState
}

sealed interface GameDetailUiState {
    data object Idle : GameDetailUiState
    data object Loading : GameDetailUiState
    data class Success(val detail: GameDetailResponseDto) : GameDetailUiState
    data class Error(val message: String) : GameDetailUiState
}

sealed interface GameSessionUiState {
    data object Idle : GameSessionUiState
    data object Starting : GameSessionUiState
    data class InProgress(val sessionId: String, val gameType: String) : GameSessionUiState
    data class Completed(val sessionResponse: GameSessionResponseDto) : GameSessionUiState
    data class Error(val message: String) : GameSessionUiState
}

sealed interface RewardHistoryUiState {
    data object Loading : RewardHistoryUiState
    data class Success(
        val items: List<RewardHistoryItemDto>,
        val currentPage: Int,
        val totalPages: Int,
        val hasMorePages: Boolean,
        val isLoadingMore: Boolean = false,
    ) : RewardHistoryUiState
    data class Error(val message: String) : RewardHistoryUiState
    data object Empty : RewardHistoryUiState
}

sealed interface GameHistoryUiState {
    data object Loading : GameHistoryUiState
    data class Success(
        val items: List<GameSessionResponseDto>,
        val currentPage: Int,
        val totalPages: Int,
        val hasMorePages: Boolean,
        val isLoadingMore: Boolean = false,
    ) : GameHistoryUiState
    data class Error(val message: String) : GameHistoryUiState
    data object Empty : GameHistoryUiState
}

data class GamesUiState(
    val homeSummaryState: GamificationHomeUiState = GamificationHomeUiState.Loading,
    val selectedGameType: String? = null,
    val gameDetailState: GameDetailUiState = GameDetailUiState.Idle,
    val sessionState: GameSessionUiState = GameSessionUiState.Idle,
    val rewardHistoryState: RewardHistoryUiState = RewardHistoryUiState.Loading,
    val gameHistoryState: GameHistoryUiState = GameHistoryUiState.Loading,
)

class GamesViewModel(
    private val gamesRepository: GamesRepository? = null,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GamesUiState())
    val uiState: StateFlow<GamesUiState> = _uiState.asStateFlow()

    init {
        loadGamificationHome()
    }

    fun loadGamificationHome(forceRefresh: Boolean = false) {
        if (!forceRefresh && _uiState.value.homeSummaryState is GamificationHomeUiState.Success) {
            return
        }

        val repo = gamesRepository ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(homeSummaryState = GamificationHomeUiState.Loading)
            when (val result = repo.fetchGamificationHome(forceRefresh)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        homeSummaryState = GamificationHomeUiState.Success(result.data)
                    )
                }

                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        homeSummaryState = GamificationHomeUiState.Error(
                            "Unable to load games. Please try again."
                        )
                    )
                }

                is NetworkResult.Exception -> {
                    _uiState.value = _uiState.value.copy(
                        homeSummaryState = GamificationHomeUiState.Error(
                            "Unable to load games. Please try again."
                        )
                    )
                }
            }
        }
    }

    fun selectGame(gameType: String?) {
        if (gameType.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(
                selectedGameType = null,
                gameDetailState = GameDetailUiState.Idle
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            selectedGameType = gameType,
            gameDetailState = GameDetailUiState.Loading
        )

        val repo = gamesRepository ?: return
        viewModelScope.launch {
            when (val result = repo.fetchGameDetails(gameType)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        gameDetailState = GameDetailUiState.Success(result.data)
                    )
                }

                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        gameDetailState = GameDetailUiState.Error("Unable to load game details. Please try again.")
                    )
                }

                is NetworkResult.Exception -> {
                    _uiState.value = _uiState.value.copy(
                        gameDetailState = GameDetailUiState.Error("Unable to load game details. Please try again.")
                    )
                }
            }
        }
    }

    fun startGameSession(gameType: String, onSessionStarted: (String) -> Unit = {}) {
        val repo = gamesRepository ?: run {
            onSessionStarted("offline_${Random.nextInt(1000, 9999)}")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(sessionState = GameSessionUiState.Starting)
            val clientReqId = "req_${Random.nextInt(100000, 999999)}"
            when (val result = repo.startGameSession(GameStartRequestDto(gameType, clientReqId))) {
                is NetworkResult.Success -> {
                    val sId = result.data.sessionId.ifBlank { clientReqId }
                    _uiState.value = _uiState.value.copy(
                        sessionState = GameSessionUiState.InProgress(sId, gameType)
                    )
                    onSessionStarted(sId)
                }

                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        sessionState = GameSessionUiState.Error("Unable to start game session. Please try again.")
                    )
                }

                is NetworkResult.Exception -> {
                    _uiState.value = _uiState.value.copy(
                        sessionState = GameSessionUiState.Error("Unable to start game session. Please try again.")
                    )
                }
            }
        }
    }

    fun completeGameSession(sessionId: String, reps: Int, durationSeconds: Int) {
        val repo = gamesRepository ?: return
        viewModelScope.launch {
            val req = GameCompleteRequestDto(
                repsDetected = reps,
                durationSeconds = durationSeconds,
                trackerVersion = "1.0.0",
                modelConfidence = 1.0
            )
            when (val result = repo.completeGameSession(sessionId, req)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        sessionState = GameSessionUiState.Completed(result.data)
                    )
                    loadGamificationHome(forceRefresh = true)
                }

                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        sessionState = GameSessionUiState.Error("Unable to save game completion. Please try again.")
                    )
                }

                is NetworkResult.Exception -> {
                    _uiState.value = _uiState.value.copy(
                        sessionState = GameSessionUiState.Error("Unable to save game completion. Please try again.")
                    )
                }
            }
        }
    }

    fun loadRewardHistory(forceRefresh: Boolean = false) {
        val current = _uiState.value.rewardHistoryState
        if (!forceRefresh && current is RewardHistoryUiState.Success && current.items.isNotEmpty()) {
            return
        }

        val repo = gamesRepository ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(rewardHistoryState = RewardHistoryUiState.Loading)
            when (val result = repo.fetchRewardHistory(page = 0, size = 20)) {
                is NetworkResult.Success -> {
                    val pageData = result.data
                    val items = pageData.content
                    val totalPages = pageData.page.totalPages
                    val currentPage = pageData.page.number
                    val hasMore = currentPage < totalPages - 1

                    if (items.isEmpty()) {
                        _uiState.value = _uiState.value.copy(rewardHistoryState = RewardHistoryUiState.Empty)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            rewardHistoryState = RewardHistoryUiState.Success(
                                items = items,
                                currentPage = currentPage,
                                totalPages = totalPages,
                                hasMorePages = hasMore,
                                isLoadingMore = false
                            )
                        )
                    }
                }

                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        rewardHistoryState = RewardHistoryUiState.Error("Unable to load reward history. Please try again.")
                    )
                }

                is NetworkResult.Exception -> {
                    _uiState.value = _uiState.value.copy(
                        rewardHistoryState = RewardHistoryUiState.Error("Unable to load reward history. Please try again.")
                    )
                }
            }
        }
    }

    fun loadGameHistory(forceRefresh: Boolean = false) {
        val current = _uiState.value.gameHistoryState
        if (!forceRefresh && current is GameHistoryUiState.Success && current.items.isNotEmpty()) {
            return
        }

        val repo = gamesRepository ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(gameHistoryState = GameHistoryUiState.Loading)
            when (val result = repo.fetchGameHistory(page = 0, size = 20)) {
                is NetworkResult.Success -> {
                    val pageData = result.data
                    val items = pageData.content
                    val totalPages = pageData.page.totalPages
                    val currentPage = pageData.page.number
                    val hasMore = currentPage < totalPages - 1

                    if (items.isEmpty()) {
                        _uiState.value = _uiState.value.copy(gameHistoryState = GameHistoryUiState.Empty)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            gameHistoryState = GameHistoryUiState.Success(
                                items = items,
                                currentPage = currentPage,
                                totalPages = totalPages,
                                hasMorePages = hasMore,
                                isLoadingMore = false
                            )
                        )
                    }
                }

                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        gameHistoryState = GameHistoryUiState.Error("Unable to load game history. Please try again.")
                    )
                }

                is NetworkResult.Exception -> {
                    _uiState.value = _uiState.value.copy(
                        gameHistoryState = GameHistoryUiState.Error("Unable to load game history. Please try again.")
                    )
                }
            }
        }
    }

    fun resetSessionState() {
        _uiState.value = _uiState.value.copy(sessionState = GameSessionUiState.Idle)
    }
}
