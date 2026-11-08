package com.muscleplay.app.core.network

object IosBaseUrlConfig {
    var baseUrl: String = "http://127.0.0.1:8080/api/v1/app"
}

actual fun getPlatformBaseUrl(): String {
    return IosBaseUrlConfig.baseUrl
}
