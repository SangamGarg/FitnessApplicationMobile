package com.muscleplay.app.feature.workout

import kotlin.time.TimeSource

data class WorkoutExerciseResult(
    val exerciseId: String,
    val exerciseName: String,
    val completedReps: Int,
    val targetReps: Int? = null,
    val isCompleted: Boolean = false
)

data class WorkoutSessionSummary(
    val durationSeconds: Long,
    val exercises: List<WorkoutExerciseResult>,
    val totalRepetitions: Int,
    val completedExercises: Int,
    val totalExercises: Int
) {
    val completionPercentage: Int
        get() = if (totalExercises == 0) 0
        else (completedExercises * 100) / totalExercises
}

class WorkoutSessionTracker {

    private var startedAt: TimeSource.Monotonic.ValueTimeMark? = null
    private val exerciseResults =
        linkedMapOf<String, WorkoutExerciseResult>()
    private var finishedSummary: WorkoutSessionSummary? = null

    val isStarted: Boolean get() = startedAt != null
    val isFinished: Boolean get() = finishedSummary != null

    fun start() {
        check(!isStarted) { "A workout session has already started." }
        exerciseResults.clear()
        finishedSummary = null
        startedAt = TimeSource.Monotonic.markNow()
    }

    fun recordExercise(result: WorkoutExerciseResult) {
        check(isStarted) {
            "Start the workout before recording exercise results."
        }
        check(!isFinished) {
            "Cannot record exercises after finishing the workout."
        }
        require(result.exerciseId.isNotBlank()) {
            "Exercise ID cannot be blank."
        }
        require(result.exerciseName.isNotBlank()) {
            "Exercise name cannot be blank."
        }
        require(result.completedReps >= 0) {
            "Completed repetitions cannot be negative."
        }
        require(result.targetReps == null || result.targetReps >= 0) {
            "Target repetitions cannot be negative."
        }

        // Re-recording an exercise ID updates its latest result.
        exerciseResults[result.exerciseId] = result
    }

    fun finish(): WorkoutSessionSummary {
        finishedSummary?.let { return it }

        val startMark = checkNotNull(startedAt) {
            "Cannot finish a workout that has not started."
        }
        val results = exerciseResults.values.toList()

        return WorkoutSessionSummary(
            durationSeconds = startMark.elapsedNow().inWholeSeconds.coerceAtLeast(0),
            exercises = results,
            totalRepetitions = results.sumOf { it.completedReps },
            completedExercises = results.count { it.isCompleted },
            totalExercises = results.size
        ).also { finishedSummary = it }
    }

    fun currentResults(): List<WorkoutExerciseResult> =
        exerciseResults.values.toList()
}
