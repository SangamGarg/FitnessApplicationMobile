package com.muscleplay.app.feature.workout

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Standalone session-state holder retained for compatibility.
 * WorkoutScreen currently uses WorkoutViewModel as the single owner of its session.
 */
@Deprecated("Use WorkoutViewModel for the WorkoutScreen session flow.")
data class WorkoutSessionUiState(
    val isStarted: Boolean = false,
    val isFinished: Boolean = false,
    val summary: WorkoutSessionSummary? = null,
    val errorMessage: String? = null
)

@Deprecated("Use WorkoutViewModel for the WorkoutScreen session flow.")
class WorkoutSessionViewModel {

    private var tracker = WorkoutSessionTracker()
    private val _uiState = MutableStateFlow(WorkoutSessionUiState())
    val uiState: StateFlow<WorkoutSessionUiState> = _uiState.asStateFlow()

    fun startWorkout() {
        if (tracker.isStarted && !tracker.isFinished) return
        tracker = WorkoutSessionTracker()
        tracker.start()
        _uiState.value = WorkoutSessionUiState(isStarted = true)
    }

    fun recordExercise(result: WorkoutExerciseResult) {
        if (!tracker.isStarted || tracker.isFinished) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "No active workout session."
            )
            return
        }
        try {
            tracker.recordExercise(result)
            _uiState.value = _uiState.value.copy(errorMessage = null)
        } catch (exception: IllegalArgumentException) {
            _uiState.value = _uiState.value.copy(
                errorMessage = exception.message ?: "Unable to record exercise."
            )
        }
    }

    fun finishWorkout() {
        if (!tracker.isStarted || tracker.isFinished) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "No active workout session."
            )
            return
        }
        _uiState.value = WorkoutSessionUiState(
            isStarted = true,
            isFinished = true,
            summary = tracker.finish()
        )
    }

    fun startAnotherWorkout() = startWorkout()
}
