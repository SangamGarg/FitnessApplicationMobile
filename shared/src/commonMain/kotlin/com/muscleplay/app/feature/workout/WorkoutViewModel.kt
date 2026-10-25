package com.muscleplay.app.feature.workout

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class WorkoutUiState(
    val exerciseName: String = "Push-ups",
    val completedReps: Int = 0,
    val targetReps: Int = 10,
    val isCompleted: Boolean = false,
)

class WorkoutViewModel(
    private val repository: WorkoutRepository = InMemoryWorkoutRepository(),
) : ViewModel() {

    val uiState: StateFlow<WorkoutUiState> = repository.workoutState

    private var tracker = WorkoutSessionTracker()

    private val _summary = MutableStateFlow<WorkoutSessionSummary?>(null)
    val summary: StateFlow<WorkoutSessionSummary?> = _summary.asStateFlow()

    init {
        tracker.start()
    }

    fun completeRep() {
        if (_summary.value != null) return
        repository.completeRep()
    }

    fun finishWorkout() {
        if (_summary.value != null) return

        val current = uiState.value
        tracker.recordExercise(
            WorkoutExerciseResult(
                exerciseId = current.exerciseName.trim()
                    .lowercase()
                    .replace(" ", "_"),
                exerciseName = current.exerciseName,
                completedReps = current.completedReps,
                targetReps = current.targetReps,
                isCompleted = current.isCompleted
            )
        )
        _summary.value = tracker.finish()
    }

    fun startAnotherWorkout() {
        resetSession()
    }

    fun resetWorkout() {
        resetSession()
    }

    private fun resetSession() {
        repository.resetWorkout()
        _summary.value = null
        tracker = WorkoutSessionTracker()
        tracker.start()
    }
}
