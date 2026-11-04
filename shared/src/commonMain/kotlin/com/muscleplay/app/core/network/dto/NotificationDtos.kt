package com.muscleplay.app.core.network.dto

data class DeviceRegisterRequestDto(
    val deviceId: String,
    val fcmToken: String,
    val platform: String = "ANDROID",
    val deviceName: String = "Android Device",
    val appVersion: String = "1.0.0",
    val notificationEnabled: Boolean = true
)

data class NotificationPreferenceRequestDto(
    val notificationEnabled: Boolean
)

data class NotificationItemDto(
    val id: String,
    val title: String,
    val body: String,
    val category: String = "SYSTEM_ANNOUNCEMENT",
    val timestamp: String = "",
    val read: Boolean = false,
    val targetRoute: String? = null
)

data class NotificationHistoryResponseDto(
    val content: List<NotificationItemDto> = emptyList(),
    val page: Int = 0,
    val size: Int = 20,
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val last: Boolean = true,
    val unreadCount: Int = 0
)

data class UnreadCountResponseDto(
    val unreadCount: Int = 0
)
