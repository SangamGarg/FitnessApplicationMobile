package com.muscleplay.app.core.network

data class HttpRequest(
    val url: String,
    val method: String = "GET",
    val headers: Map<String, String> = emptyMap(),
    val body: String? = null
)

data class HttpResponse(
    val statusCode: Int,
    val body: String,
    val headers: Map<String, String> = emptyMap()
)

expect class PlatformHttpClient() {
    suspend fun execute(request: HttpRequest): HttpResponse
}
