package com.muscleplay.app.feature.onboarding

class OnboardingPreferences {
    private var isCompleted: Boolean = false

    fun isOnboardingCompleted(): Boolean = isCompleted

    fun setOnboardingCompleted(completed: Boolean) {
        isCompleted = completed
    }
}
