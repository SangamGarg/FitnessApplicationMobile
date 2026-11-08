package com.muscleplay.app.core.network

object AndroidBaseUrlConfig {
    var baseUrl: String? = null
}

actual fun getPlatformBaseUrl(): String {
    return AndroidBaseUrlConfig.baseUrl ?: ""
}
