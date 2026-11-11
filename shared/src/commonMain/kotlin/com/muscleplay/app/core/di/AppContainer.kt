package com.muscleplay.app.core.di

import com.muscleplay.app.core.network.ApiClient
import com.muscleplay.app.core.network.auth.TokenStorageManager
import com.muscleplay.app.core.network.auth.UserSessionRepository
import com.muscleplay.app.core.network.auth.UserSessionRepositoryImpl
import com.muscleplay.app.core.network.notification.NotificationRepository
import com.muscleplay.app.feature.auth.AuthRepository
import com.muscleplay.app.feature.diet.data.InMemoryMealDao
import com.muscleplay.app.feature.diet.data.MealDao
import com.muscleplay.app.feature.home.data.DashboardRepository
import com.muscleplay.app.feature.home.data.DashboardRepositoryImpl
import com.muscleplay.app.feature.workout.InMemoryWorkoutRepository
import com.muscleplay.app.feature.workout.WorkoutRepository

class AppContainer {

    val workoutRepository: WorkoutRepository =
        InMemoryWorkoutRepository()

    val tokenStorageManager: TokenStorageManager = TokenStorageManager()

    val userSessionRepository: UserSessionRepository = UserSessionRepositoryImpl()

    val apiClient: ApiClient = ApiClient(tokenStorageManager = tokenStorageManager)

    val authRepository: AuthRepository = AuthRepository(
        apiClient = apiClient,
        tokenStorageManager = tokenStorageManager,
        userSessionRepository = userSessionRepository
    )

    val notificationRepository: NotificationRepository = NotificationRepository(
        apiClient = apiClient,
        tokenStorageManager = tokenStorageManager
    )

    val dashboardRepository: DashboardRepository = DashboardRepositoryImpl(
        apiClient = apiClient,
        tokenStorageManager = tokenStorageManager,
        userSessionRepository = userSessionRepository
    )

    val profileRepository: com.muscleplay.app.feature.profile.data.ProfileRepository =
        com.muscleplay.app.feature.profile.data.ProfileRepositoryImpl(
            apiClient = apiClient,
            tokenStorageManager = tokenStorageManager
        )

    val supportRepository: com.muscleplay.app.feature.profile.support.data.SupportRepository =
        com.muscleplay.app.feature.profile.support.data.SupportRepositoryImpl(
            apiClient = apiClient,
            tokenStorageManager = tokenStorageManager
        )

    val emailSupportRepository: com.muscleplay.app.feature.profile.support.data.EmailSupportRepository =
        com.muscleplay.app.feature.profile.support.data.EmailSupportRepositoryImpl(
            apiClient = apiClient,
            tokenStorageManager = tokenStorageManager
        )

    val ticketRepository: com.muscleplay.app.feature.profile.support.data.TicketRepository =
        com.muscleplay.app.feature.profile.support.data.TicketRepositoryImpl(
            apiClient = apiClient,
            tokenStorageManager = tokenStorageManager
        )

    val mealDao: MealDao = InMemoryMealDao()
}
