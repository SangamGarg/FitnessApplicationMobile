package com.muscleplay.app.core.di

import com.muscleplay.app.core.network.ApiClient
import com.muscleplay.app.feature.workout.InMemoryWorkoutRepository
import com.muscleplay.app.feature.workout.WorkoutRepository

class AppContainer {

    val workoutRepository: WorkoutRepository =
        InMemoryWorkoutRepository()

    val apiClient: ApiClient = ApiClient()
}
