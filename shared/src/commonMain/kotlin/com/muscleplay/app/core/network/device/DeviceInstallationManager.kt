package com.muscleplay.app.core.network.device

enum class DevicePlatform {
    ANDROID,
    IOS
}

data class DeviceInfo(
    val deviceId: String,
    val platform: DevicePlatform,
    val deviceName: String,
    val appVersion: String?
)

expect class DeviceInstallationManager() {
    fun getDeviceInfo(): DeviceInfo
    fun getDeviceId(): String
}
