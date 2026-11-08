package com.muscleplay.app.navigation

import android.content.Intent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

actual object NotificationIntentHandler {
    private val _pendingRouteFlow = MutableStateFlow<String?>(null)
    actual val pendingRouteFlow: StateFlow<String?> = _pendingRouteFlow.asStateFlow()

    fun handleIntent(intent: Intent?) {
        val rawRoute = intent?.getStringExtra("targetRoute")
        if (!rawRoute.isNullOrBlank()) {
            val safeRoute = NotificationRouteWhitelist.mapToSafeRoute(rawRoute)
            if (!safeRoute.isNullOrBlank()) {
                _pendingRouteFlow.value = safeRoute
            }
        }
    }

    actual fun clearPendingRoute() {
        _pendingRouteFlow.value = null
    }
}
