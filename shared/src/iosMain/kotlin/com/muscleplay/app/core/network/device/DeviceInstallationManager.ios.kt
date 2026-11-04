package com.muscleplay.app.core.network.device

actual class DeviceInstallationManager {
    private var cachedDeviceId: String = "ios_device_installation_uuid_123"

    actual fun getDeviceId(): String {
        return cachedDeviceId
    }

    actual fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            deviceId = getDeviceId(),
            platform = DevicePlatform.IOS,
            deviceName = "iPhone 15 Pro",
            appVersion = "1.0.0"
        )
    }
}
