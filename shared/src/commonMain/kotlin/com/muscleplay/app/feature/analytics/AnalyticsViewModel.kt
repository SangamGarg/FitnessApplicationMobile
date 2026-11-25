package com.muscleplay.app.feature.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muscleplay.app.core.network.NetworkResult
import com.muscleplay.app.core.network.dto.AnalyticsHomeResponseDto
import com.muscleplay.app.core.network.dto.CalculatorHomeResponseDto
import com.muscleplay.app.core.network.dto.SmartCalculationRequest
import com.muscleplay.app.feature.analytics.data.AnalyticsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement

sealed interface AnalyticsHomeUiState {
    data object Loading : AnalyticsHomeUiState
    data class Success(val homeData: AnalyticsHomeResponseDto) : AnalyticsHomeUiState
    data class Error(val message: String) : AnalyticsHomeUiState
}

sealed interface CalculatorHomeUiState {
    data object Loading : CalculatorHomeUiState
    data class Success(val homeData: CalculatorHomeResponseDto) : CalculatorHomeUiState
    data class Error(val message: String) : CalculatorHomeUiState
}

sealed interface CalculationUiState {
    data object Idle : CalculationUiState
    data object Calculating : CalculationUiState
    data class Success(val resultMap: Map<String, JsonElement>) : CalculationUiState
    data class Error(val message: String) : CalculationUiState
}

data class AnalyticsUiState(
    val analyticsHomeState: AnalyticsHomeUiState = AnalyticsHomeUiState.Loading,
    val calculatorHomeState: CalculatorHomeUiState = CalculatorHomeUiState.Loading,
    val calculationState: CalculationUiState = CalculationUiState.Idle,
    val isHealthSnapshotExpanded: Boolean = false,
)

class AnalyticsViewModel(
    private val analyticsRepository: AnalyticsRepository? = null,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        loadAnalyticsHome()
    }

    fun loadAnalyticsHome(forceRefresh: Boolean = false) {
        if (!forceRefresh && _uiState.value.analyticsHomeState is AnalyticsHomeUiState.Success) {
            return
        }

        val repo = analyticsRepository ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(analyticsHomeState = AnalyticsHomeUiState.Loading)
            when (val result = repo.fetchAnalyticsHome(forceRefresh)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        analyticsHomeState = AnalyticsHomeUiState.Success(result.data)
                    )
                }

                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        analyticsHomeState = AnalyticsHomeUiState.Error(
                            "Unable to load analytics. Please check your connection and try again."
                        )
                    )
                }

                is NetworkResult.Exception -> {
                    _uiState.value = _uiState.value.copy(
                        analyticsHomeState = AnalyticsHomeUiState.Error(
                            "Unable to load analytics. Please check your connection and try again."
                        )
                    )
                }
            }
        }
    }

    fun loadCalculatorHome(forceRefresh: Boolean = false) {
        if (!forceRefresh && _uiState.value.calculatorHomeState is CalculatorHomeUiState.Success) {
            return
        }

        val repo = analyticsRepository ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(calculatorHomeState = CalculatorHomeUiState.Loading)
            when (val result = repo.fetchCalculatorHome(forceRefresh)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        calculatorHomeState = CalculatorHomeUiState.Success(result.data)
                    )
                }

                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        calculatorHomeState = CalculatorHomeUiState.Error(
                            "Unable to load calculator home. Please check your connection and try again."
                        )
                    )
                }

                is NetworkResult.Exception -> {
                    _uiState.value = _uiState.value.copy(
                        calculatorHomeState = CalculatorHomeUiState.Error(
                            "Unable to load calculator home. Please check your connection and try again."
                        )
                    )
                }
            }
        }
    }

    fun toggleHealthSnapshotExpanded() {
        _uiState.value = _uiState.value.copy(
            isHealthSnapshotExpanded = !_uiState.value.isHealthSnapshotExpanded
        )
    }

    fun performSmartCalculation(calculatorKey: String, inputs: Map<String, JsonElement>) {
        if (_uiState.value.calculationState is CalculationUiState.Calculating) return

        val repo = analyticsRepository
        if (repo == null) {
            _uiState.value = _uiState.value.copy(
                calculationState = CalculationUiState.Error("Offline mode: Connect to internet for live calculation.")
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(calculationState = CalculationUiState.Calculating)
            when (val result = repo.calculateSmart(calculatorKey, SmartCalculationRequest(inputs))) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        calculationState = CalculationUiState.Success(result.data)
                    )
                }

                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        calculationState = CalculationUiState.Error(result.message)
                    )
                }

                is NetworkResult.Exception -> {
                    _uiState.value = _uiState.value.copy(
                        calculationState = CalculationUiState.Error(
                            result.throwable.message ?: "Unable to complete calculation. Please try again."
                        )
                    )
                }
            }
        }
    }

    fun resetCalculationState() {
        _uiState.value = _uiState.value.copy(calculationState = CalculationUiState.Idle)
    }
}
