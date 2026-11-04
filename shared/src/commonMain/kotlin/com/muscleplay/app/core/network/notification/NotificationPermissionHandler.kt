package com.muscleplay.app.core.network.notification

import androidx.compose.runtime.Composable

@Composable
expect fun RequestNotificationPermissionEffect(
    onRequestCompleted: () -> Unit
)
