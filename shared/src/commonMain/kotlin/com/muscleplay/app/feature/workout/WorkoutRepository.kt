package com.muscleplay.app.feature.workout

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface WorkoutRepository {
    val workoutState: StateFlow<WorkoutUiState>

    fun completeRep()
    fun resetWorkout()
}

class InMemoryWorkoutRepository : WorkoutRepository {

    private val _workoutState = MutableStateFlow(WorkoutUiState())

    override val workoutState: StateFlow<WorkoutUiState> =
        _workoutState.asStateFlow()

    override fun completeRep() {
        val current = _workoutState.value
        if (current.isCompleted) return

        val updatedReps = (current.completedReps + 1)
            .coerceAtMost(current.targetReps)

        _workoutState.value = current.copy(
            completedReps = updatedReps,
            isCompleted = updatedReps >= current.targetReps
        )
    }

    override fun resetWorkout() {
        _workoutState.value = _workoutState.value.copy(
            completedReps = 0,
            isCompleted = false
        )
    }
}
