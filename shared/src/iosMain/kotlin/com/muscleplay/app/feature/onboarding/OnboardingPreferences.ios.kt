package com.muscleplay.app.feature.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSUserDefaults

class IosOnboardingPreferences : OnboardingPreferences {
    private val userDefaults = NSUserDefaults.standardUserDefaults

    override fun isOnboardingCompleted(): Boolean {
        return userDefaults.boolForKey("is_onboarding_completed")
    }

    override fun setOnboardingCompleted(completed: Boolean) {
        userDefaults.setBool(completed, forKey = "is_onboarding_completed")
    }
}

@Composable
actual fun rememberOnboardingPreferences(): OnboardingPreferences {
    return remember { IosOnboardingPreferences() }
}
