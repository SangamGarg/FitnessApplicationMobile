package com.muscleplay.app.feature.home

import com.muscleplay.app.core.network.NetworkResult
import com.muscleplay.app.core.network.dto.DashboardResponseDto
import com.muscleplay.app.feature.home.data.DashboardRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data class Success(
        val data: DashboardResponseDto,
        val isRefreshing: Boolean = false,
    ) : DashboardUiState
    data class Error(
        val statusCode: Int,
        val message: String,
    ) : DashboardUiState
    data object Empty : DashboardUiState
}

class DashboardViewModel(
    private val repository: DashboardRepository,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main + Job()),
) {
    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var fetchJob: Job? = null

    init {
        loadDashboard()
    }

    fun loadDashboard(isRefresh: Boolean = false) {
        if (fetchJob?.isActive == true && !isRefresh) return

        fetchJob?.cancel()
        fetchJob = coroutineScope.launch {
            val currentState = _uiState.value
            if (isRefresh && currentState is DashboardUiState.Success) {
                _uiState.value = currentState.copy(isRefreshing = true)
            } else if (!isRefresh) {
                _uiState.value = DashboardUiState.Loading
            }

            when (val result = repository.getDashboard()) {
                is NetworkResult.Success -> {
                    val data = result.data
                    if (data.user == null && data.activity == null && data.workoutAnalytics == null && data.recommendedWorkout == null) {
                        _uiState.value = DashboardUiState.Empty
                    } else {
                        _uiState.value = DashboardUiState.Success(data = data, isRefreshing = false)
                    }
                }

                is NetworkResult.Error -> {
                    _uiState.value = DashboardUiState.Error(result.statusCode, result.message)
                }

                is NetworkResult.Exception -> {
                    _uiState.value = DashboardUiState.Error(
                        -1,
                        result.throwable.message ?: "An unexpected network error occurred."
                    )
                }
            }
        }
    }

    fun refresh() {
        loadDashboard(isRefresh = true)
    }
}
