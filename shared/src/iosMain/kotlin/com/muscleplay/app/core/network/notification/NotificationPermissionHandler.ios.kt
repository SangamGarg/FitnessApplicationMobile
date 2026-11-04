package com.muscleplay.app.core.network.notification

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
actual fun RequestNotificationPermissionEffect(
    onRequestCompleted: () -> Unit
) {
    LaunchedEffect(Unit) {
        onRequestCompleted()
    }
}
