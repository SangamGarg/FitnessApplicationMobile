package com.muscleplay.app.core.network.device

import android.os.Build
import java.util.UUID

actual class DeviceInstallationManager {
    private var cachedDeviceId: String? = null

    actual fun getDeviceId(): String {
        cachedDeviceId?.let { return it }
        val newId = UUID.randomUUID().toString()
        cachedDeviceId = newId
        return newId
    }

    actual fun getDeviceInfo(): DeviceInfo {
        val model = Build.MODEL ?: "Android Device"
        val manufacturer = Build.MANUFACTURER ?: ""
        val deviceName = if (model.startsWith(manufacturer, ignoreCase = true)) {
            model.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        } else {
            "${manufacturer.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }} $model".trim()
        }

        return DeviceInfo(
            deviceId = getDeviceId(),
            platform = DevicePlatform.ANDROID,
            deviceName = deviceName.ifBlank { "Android Device" },
            appVersion = "1.0.0"
        )
    }
}
