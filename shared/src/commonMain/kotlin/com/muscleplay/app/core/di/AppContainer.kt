package com.muscleplay.app.core.di

import com.muscleplay.app.core.network.ApiClient
import com.muscleplay.app.feature.auth.AuthRepository
import com.muscleplay.app.feature.diet.data.InMemoryMealDao
import com.muscleplay.app.feature.diet.data.MealDao
import com.muscleplay.app.feature.workout.InMemoryWorkoutRepository
import com.muscleplay.app.feature.workout.WorkoutRepository

class AppContainer {

    val workoutRepository: WorkoutRepository =
        InMemoryWorkoutRepository()

    val apiClient: ApiClient = ApiClient()

    val authRepository: AuthRepository = AuthRepository(apiClient = apiClient)

    val mealDao: MealDao = InMemoryMealDao()
}
