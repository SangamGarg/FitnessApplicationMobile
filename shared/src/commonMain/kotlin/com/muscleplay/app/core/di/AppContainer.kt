package com.muscleplay.app.core.di

import com.muscleplay.app.core.network.ApiClient
import com.muscleplay.app.core.network.auth.TokenStorageManager
import com.muscleplay.app.core.network.notification.NotificationRepository
import com.muscleplay.app.feature.auth.AuthRepository
import com.muscleplay.app.feature.diet.data.InMemoryMealDao
import com.muscleplay.app.feature.diet.data.MealDao
import com.muscleplay.app.feature.workout.InMemoryWorkoutRepository
import com.muscleplay.app.feature.workout.WorkoutRepository

class AppContainer {

    val workoutRepository: WorkoutRepository =
        InMemoryWorkoutRepository()

    val tokenStorageManager: TokenStorageManager = TokenStorageManager()

    val apiClient: ApiClient = ApiClient(tokenStorageManager = tokenStorageManager)

    val authRepository: AuthRepository = AuthRepository(
        apiClient = apiClient,
        tokenStorageManager = tokenStorageManager
    )

    val notificationRepository: NotificationRepository = NotificationRepository(
        apiClient = apiClient,
        tokenStorageManager = tokenStorageManager
    )

    val mealDao: MealDao = InMemoryMealDao()
}
