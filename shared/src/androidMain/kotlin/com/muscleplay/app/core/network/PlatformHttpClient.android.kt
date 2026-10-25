package com.muscleplay.app.core.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL

actual class PlatformHttpClient {
    actual suspend fun execute(request: HttpRequest): HttpResponse = withContext(Dispatchers.IO) {
        val url = URL(request.url)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = request.method
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000

        request.headers.forEach { (key, value) ->
            connection.setRequestProperty(key, value)
        }

        if (request.body != null && (request.method == "POST" || request.method == "PUT")) {
            connection.doOutput = true
            connection.outputStream.use { os ->
                os.write(request.body.toByteArray(Charsets.UTF_8))
            }
        }

        val statusCode = connection.responseCode
        val inputStream = if (statusCode in 200..299) connection.inputStream else connection.errorStream
        val responseBody = inputStream?.bufferedReader()?.use(BufferedReader::readText).orEmpty()

        HttpResponse(
            statusCode = statusCode,
            body = responseBody
        )
    }
}
