package com.muscleplay.app.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class WorkoutLogDto(
    val id: String = "",
    val title: String = "",
    val category: String = "",
    val durationMinutes: Int = 0,
    val caloriesBurned: Int = 0,
    val completedAtIso: String = "",
)

@Serializable
data class SyncWorkoutRequestDto(
    val workoutLogs: List<WorkoutLogDto> = emptyList(),
)
