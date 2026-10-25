package com.muscleplay.app.core.ui

import androidx.activity.compose.BackHandler as AndroidBackHandler
import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    AndroidBackHandler(enabled = enabled, onBack = onBack)
}
