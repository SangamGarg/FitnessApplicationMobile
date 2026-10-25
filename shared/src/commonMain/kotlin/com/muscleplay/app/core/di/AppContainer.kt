package com.muscleplay.app.core.di

import com.muscleplay.app.feature.onboarding.OnboardingPreferences
import com.muscleplay.app.feature.workout.InMemoryWorkoutRepository
import com.muscleplay.app.feature.workout.WorkoutRepository

class AppContainer {

    val workoutRepository: WorkoutRepository =
        InMemoryWorkoutRepository()

    val onboardingPreferences: OnboardingPreferences =
        OnboardingPreferences()
}
