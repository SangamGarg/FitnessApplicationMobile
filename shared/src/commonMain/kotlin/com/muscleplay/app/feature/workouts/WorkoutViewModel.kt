package com.muscleplay.app.feature.workouts

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow

data class WorkoutUiState(
    val exerciseName: String = "Push-ups",
    val completedReps: Int = 0,
    val targetReps: Int = 10,
    val isCompleted: Boolean = false,
)

class WorkoutViewModel(
    private val repository: WorkoutRepository =
        InMemoryWorkoutRepository(),
) : ViewModel() {

    val uiState: StateFlow<WorkoutUiState> =
        repository.workoutState

    fun completeRep() {

        repository.completeRep()
    }

    fun resetWorkout() {
        repository.resetWorkout()
    }
}
