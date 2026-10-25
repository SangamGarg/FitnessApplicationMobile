package com.muscleplay.app.core.network.api

import com.muscleplay.app.core.network.NetworkResult
import com.muscleplay.app.core.network.dto.SyncWorkoutRequestDto
import com.muscleplay.app.core.network.dto.WorkoutLogDto

interface WorkoutApiService {
    suspend fun fetchWorkoutHistory(authToken: String): NetworkResult<List<WorkoutLogDto>>
    suspend fun syncWorkoutLogs(authToken: String, request: SyncWorkoutRequestDto): NetworkResult<Boolean>
}
