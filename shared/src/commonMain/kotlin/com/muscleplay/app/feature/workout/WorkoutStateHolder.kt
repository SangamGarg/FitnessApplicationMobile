package com.muscleplay.app.feature.workout

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Legacy standalone state holder retained for compatibility.
 * WorkoutScreen uses WorkoutRepository and WorkoutViewModel instead.
 */
@Deprecated("Use WorkoutRepository and WorkoutViewModel.")
class WorkoutStateHolder {

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    fun completeRep() {
        _uiState.value = _uiState.value.let { state ->
            if (state.isCompleted) state
            else {
                val updatedReps = (state.completedReps + 1)
                    .coerceAtMost(state.targetReps)
                state.copy(
                    completedReps = updatedReps,
                    isCompleted = updatedReps >= state.targetReps
                )
            }
        }
    }

    fun resetWorkout() {
        _uiState.value = _uiState.value.copy(
            completedReps = 0,
            isCompleted = false
        )
    }
}
