package com.muscleplay.app.core.network.dto

data class WorkoutLogDto(
    val id: String,
    val title: String,
    val category: String,
    val durationMinutes: Int,
    val caloriesBurned: Int,
    val completedAtIso: String
)

data class SyncWorkoutRequestDto(
    val workoutLogs: List<WorkoutLogDto>
)
