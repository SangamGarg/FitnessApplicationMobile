package com.muscleplay.app.feature.onboarding

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit

class AndroidOnboardingPreferences(context: Context) : OnboardingPreferences {
    private val preferences = context.applicationContext.getSharedPreferences("onboarding_prefs", Context.MODE_PRIVATE)

    override fun isOnboardingCompleted(): Boolean {
        return preferences.getBoolean("is_onboarding_completed", false)
    }

    override fun setOnboardingCompleted(completed: Boolean) {
        preferences.edit {
            putBoolean("is_onboarding_completed", completed)
        }
    }
}

@Composable
actual fun rememberOnboardingPreferences(): OnboardingPreferences {
    val context = LocalContext.current
    return remember(context) { AndroidOnboardingPreferences(context) }
}
