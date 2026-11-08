package com.muscleplay.app.navigation

import kotlinx.coroutines.flow.StateFlow

expect object NotificationIntentHandler {
    val pendingRouteFlow: StateFlow<String?>
    fun clearPendingRoute()
}
