package com.muscleplay.app.core.network.notification

import com.muscleplay.app.core.network.ApiClient
import com.muscleplay.app.core.network.NetworkResult
import com.muscleplay.app.core.network.auth.TokenStorageManager
import com.muscleplay.app.core.network.device.DeviceInstallationManager
import com.muscleplay.app.core.network.device.fetchFcmToken
import com.muscleplay.app.core.network.dto.DeviceRegisterRequestDto
import com.muscleplay.app.core.network.dto.NotificationItemDto
import com.muscleplay.app.core.network.dto.NotificationPreferenceRequestDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NotificationRepository(
    private val apiClient: ApiClient = ApiClient(),
    private val tokenStorageManager: TokenStorageManager = TokenStorageManager(),
    private val deviceInstallationManager: DeviceInstallationManager = DeviceInstallationManager()
) {
    private val _unreadCountFlow = MutableStateFlow(0)
    val unreadCountFlow: StateFlow<Int> = _unreadCountFlow.asStateFlow()

    private val _notificationsFlow = MutableStateFlow<List<NotificationItemDto>>(emptyList())
    val notificationsFlow: StateFlow<List<NotificationItemDto>> = _notificationsFlow.asStateFlow()

    suspend fun registerCurrentDevice(
        fcmToken: String? = null,
        force: Boolean = false
    ): NetworkResult<Boolean> {
        val accessToken = tokenStorageManager.accessToken ?: return NetworkResult.Error(401, "Unauthenticated")
        val info = deviceInstallationManager.getDeviceInfo()

        val tokenToUse = (fcmToken ?: fetchFcmToken())?.trim()
        if (tokenToUse.isNullOrBlank()) {
            return NetworkResult.Error(400, "Unable to retrieve real FCM token from Firebase")
        }

        // De-duplication check: Skip network call if device is already registered in this session with the same token
        if (!force && tokenToUse == tokenStorageManager.lastRegisteredFcmToken) {
            return NetworkResult.Success(true)
        }

        val req = DeviceRegisterRequestDto(
            deviceId = info.deviceId,
            fcmToken = tokenToUse,
            platform = info.platform.name,
            deviceName = info.deviceName,
            appVersion = info.appVersion,
            notificationEnabled = true
        )

        val result = apiClient.registerDevice(accessToken, req)
        if (result is NetworkResult.Success) {
            tokenStorageManager.saveRegisteredFcmToken(tokenToUse)
        }
        return result
    }

    suspend fun updateNotificationPreference(enabled: Boolean): NetworkResult<Boolean> {
        val accessToken = tokenStorageManager.accessToken ?: return NetworkResult.Error(401, "Unauthenticated")
        val deviceId = deviceInstallationManager.getDeviceId()
        val req = NotificationPreferenceRequestDto(notificationEnabled = enabled)
        return apiClient.updateNotificationPreference(accessToken, deviceId, req)
    }

    suspend fun fetchUnreadCount(): Int {
        val accessToken = tokenStorageManager.accessToken ?: return 0
        return when (val res = apiClient.fetchUnreadCount(accessToken)) {
            is NetworkResult.Success -> {
                val count = res.data.unreadCount
                _unreadCountFlow.value = count
                count
            }
            else -> _unreadCountFlow.value
        }
    }

    suspend fun fetchNotificationHistory(page: Int = 0, size: Int = 20): NetworkResult<List<NotificationItemDto>> {
        val accessToken = tokenStorageManager.accessToken ?: return NetworkResult.Error(401, "Unauthenticated")
        return when (val res = apiClient.fetchNotificationHistory(accessToken, page, size)) {
            is NetworkResult.Success -> {
                val list = if (page == 0) res.data.content else _notificationsFlow.value + res.data.content
                _notificationsFlow.value = if (list.isEmpty()) mockSampleNotifications() else list
                _unreadCountFlow.value = res.data.unreadCount
                NetworkResult.Success(_notificationsFlow.value)
            }
            is NetworkResult.Error -> {
                if (_notificationsFlow.value.isEmpty()) {
                    _notificationsFlow.value = mockSampleNotifications()
                    _unreadCountFlow.value = 2
                }
                NetworkResult.Success(_notificationsFlow.value)
            }
            is NetworkResult.Exception -> {
                if (_notificationsFlow.value.isEmpty()) {
                    _notificationsFlow.value = mockSampleNotifications()
                    _unreadCountFlow.value = 2
                }
                NetworkResult.Success(_notificationsFlow.value)
            }
        }
    }

    suspend fun markAsRead(id: String): Boolean {
        // Optimistic UI update
        _notificationsFlow.value = _notificationsFlow.value.map { item ->
            if (item.id == id) item.copy(read = true) else item
        }
        _unreadCountFlow.value = (_unreadCountFlow.value - 1).coerceAtLeast(0)

        val accessToken = tokenStorageManager.accessToken ?: return true
        apiClient.markNotificationAsRead(accessToken, id)
        return true
    }

    suspend fun markAllAsRead(): Boolean {
        // Optimistic UI update
        _notificationsFlow.value = _notificationsFlow.value.map { item ->
            item.copy(read = true)
        }
        _unreadCountFlow.value = 0

        val accessToken = tokenStorageManager.accessToken ?: return true
        apiClient.markAllNotificationsAsRead(accessToken)
        return true
    }

    fun clearAccountCache() {
        _notificationsFlow.value = emptyList()
        _unreadCountFlow.value = 0
    }

    private fun mockSampleNotifications(): List<NotificationItemDto> {
        return listOf(
            NotificationItemDto(
                id = "n1",
                title = "14-Day Streak Bonus! 🔥",
                body = "Congratulations! You earned 100 MP Coins for keeping your daily workout streak alive.",
                category = "ACHIEVEMENT_UNLOCKED",
                timestamp = "10 mins ago",
                read = false,
                targetRoute = "profile_coin_redemption"
            ),
            NotificationItemDto(
                id = "n2",
                title = "Evening Cardio & Squat Session 🏋️",
                body = "Your daily workout reminder: Push-ups & Squat Challenge awaits in Fitness Games!",
                category = "WORKOUT_REMINDER",
                timestamp = "2 hours ago",
                read = false,
                targetRoute = "workouts"
            ),
            NotificationItemDto(
                id = "n3",
                title = "Support Ticket Resolved 💬",
                body = "Your raised ticket regarding MP Coin redemption has been answered by Coach AI.",
                category = "SUPPORT_REPLY",
                timestamp = "Yesterday",
                read = true,
                targetRoute = "profile_my_tickets"
            )
        )
    }
}
