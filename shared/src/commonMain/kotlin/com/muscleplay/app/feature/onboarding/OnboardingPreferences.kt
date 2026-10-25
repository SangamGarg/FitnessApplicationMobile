package com.muscleplay.app.feature.onboarding

import androidx.compose.runtime.Composable

interface OnboardingPreferences {
    fun isOnboardingCompleted(): Boolean
    fun setOnboardingCompleted(completed: Boolean)
}

@Composable
expect fun rememberOnboardingPreferences(): OnboardingPreferences
