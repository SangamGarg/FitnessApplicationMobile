package com.muscleplay.app.core.network.client

import com.muscleplay.app.core.network.auth.AndroidAppContext
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.Cache
import java.io.File
import java.util.concurrent.TimeUnit

actual fun createPlatformHttpClient(): HttpClient {
    return HttpClient(OkHttp) {
        engine {
            config {
                connectTimeout(15, TimeUnit.SECONDS)
                readTimeout(15, TimeUnit.SECONDS)
                writeTimeout(15, TimeUnit.SECONDS)

                AndroidAppContext.applicationContext?.let { ctx ->
                    val cacheDir = File(ctx.cacheDir, "muscleplay_http_cache")
                    cache(Cache(cacheDir, 10L * 1024L * 1024L))
                }
            }
        }
    }
}
