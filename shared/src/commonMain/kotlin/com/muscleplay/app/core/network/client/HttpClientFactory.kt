package com.muscleplay.app.core.network.client

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

expect fun createPlatformHttpClient(): HttpClient

object HttpClientFactory {
    fun create(
        isDebug: Boolean = true,
    ): HttpClient {
        return createPlatformHttpClient().config {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    coerceInputValues = true
                })
            }

            install(HttpTimeout) {
                connectTimeoutMillis = 15_000L
                requestTimeoutMillis = 30_000L
                socketTimeoutMillis = 15_000L
            }

            if (isDebug) {
                install(Logging) {
                    logger = Logger.SIMPLE
                    level = LogLevel.HEADERS
                    sanitizeHeader { header -> header.equals("Authorization", ignoreCase = true) }
                }
            }
        }
    }
}
