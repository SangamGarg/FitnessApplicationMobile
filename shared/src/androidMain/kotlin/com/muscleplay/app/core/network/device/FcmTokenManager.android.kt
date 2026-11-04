package com.muscleplay.app.core.network.device

import com.google.firebase.messaging.FirebaseMessaging
import com.muscleplay.app.core.network.auth.MusclePlayFirebaseMessagingService
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual suspend fun fetchFcmToken(): String? {
    val cachedToken = MusclePlayFirebaseMessagingService.latestFcmToken
    if (!cachedToken.isNullOrBlank()) {
        return cachedToken
    }

    return try {
        suspendCancellableCoroutine { continuation ->
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful && !task.result.isNullOrBlank()) {
                    val token = task.result
                    MusclePlayFirebaseMessagingService.latestFcmToken = token
                    continuation.resume(token)
                } else {
                    continuation.resume(null)
                }
            }
        }
    } catch (e: Throwable) {
        null
    }
}
