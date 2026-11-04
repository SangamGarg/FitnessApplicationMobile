package com.muscleplay.app.core.network.api

import com.muscleplay.app.core.network.NetworkResult
import com.muscleplay.app.core.network.dto.*

interface NotificationApiService {
    suspend fun registerDevice(accessToken: String, request: DeviceRegisterRequestDto): NetworkResult<Boolean>
    suspend fun unregisterDevice(accessToken: String, deviceId: String): NetworkResult<Boolean>
    suspend fun updateNotificationPreference(accessToken: String, deviceId: String, request: NotificationPreferenceRequestDto): NetworkResult<Boolean>
    suspend fun fetchNotificationHistory(accessToken: String, page: Int = 0, size: Int = 20, unreadOnly: Boolean = false): NetworkResult<NotificationHistoryResponseDto>
    suspend fun fetchUnreadCount(accessToken: String): NetworkResult<UnreadCountResponseDto>
    suspend fun markNotificationAsRead(accessToken: String, id: String): NetworkResult<Boolean>
    suspend fun markAllNotificationsAsRead(accessToken: String): NetworkResult<Boolean>
}
