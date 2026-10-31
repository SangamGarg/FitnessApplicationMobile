package com.muscleplay.app.core.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

actual class PlatformHttpClient {

    actual suspend fun execute(request: HttpRequest): HttpResponse =
        withContext(Dispatchers.IO) {

            val connection = URL(request.url)
                .openConnection() as HttpURLConnection

            try {
                connection.requestMethod = request.method
                connection.connectTimeout = 10_000
                connection.readTimeout = 15_000
                connection.useCaches = false

                request.headers.forEach { (key, value) ->
                    connection.setRequestProperty(key, value)
                }

                if (
                    request.body != null &&
                    request.method.uppercase() in setOf("POST", "PUT", "PATCH")
                ) {
                    connection.doOutput = true

                    if (connection.getRequestProperty("Content-Type") == null) {
                        connection.setRequestProperty(
                            "Content-Type",
                            "application/json; charset=UTF-8"
                        )
                    }

                    connection.outputStream.use { output ->
                        output.write(
                            request.body.toByteArray(Charsets.UTF_8)
                        )
                    }
                }

                val statusCode = connection.responseCode

                val inputStream = if (statusCode in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

                val responseBody = inputStream
                    ?.bufferedReader(Charsets.UTF_8)
                    ?.use { it.readText() }
                    .orEmpty()

                HttpResponse(
                    statusCode = statusCode,
                    body = responseBody
                )
            } finally {
                connection.disconnect()
            }
        }
}
