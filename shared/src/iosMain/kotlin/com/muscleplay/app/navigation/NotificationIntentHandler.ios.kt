package com.muscleplay.app.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

actual object NotificationIntentHandler {
    private val _pendingRouteFlow = MutableStateFlow<String?>(null)
    actual val pendingRouteFlow: StateFlow<String?> = _pendingRouteFlow.asStateFlow()

    actual fun clearPendingRoute() {
        _pendingRouteFlow.value = null
    }
}
