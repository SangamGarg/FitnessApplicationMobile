package com.muscleplay.app.feature.games

import android.content.Context
import androidx.core.content.edit

class ExerciseRulesPreferences(context: Context) {

    private val preferences = context.getSharedPreferences(
        "exercise_rules_preferences",
        Context.MODE_PRIVATE
    )

    fun shouldShowRules(exerciseId: String): Boolean {
        return !preferences.getBoolean(
            "${exerciseId}_dont_show_again",
            false
        )
    }

    fun setDontShowAgain(
        exerciseId: String,
        dontShowAgain: Boolean,
    ) {
        preferences.edit {
            putBoolean(
                "${exerciseId}_dont_show_again",
                dontShowAgain
            )
        }
    }
}
