package com.muscleplay.app.core.network

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.*
import kotlin.coroutines.resume

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual class PlatformHttpClient {
    actual suspend fun execute(request: HttpRequest): HttpResponse = suspendCancellableCoroutine { continuation ->
        val nsUrl = NSURL.URLWithString(request.url) ?: run {
            continuation.resume(HttpResponse(400, "Invalid URL"))
            return@suspendCancellableCoroutine
        }
        val mutableRequest = NSMutableURLRequest.requestWithURL(nsUrl).apply {
            setHTTPMethod(request.method)
            request.headers.forEach { (key, value) ->
                setValue(value, forHTTPHeaderField = key)
            }
            if (request.body != null) {
                val data = NSString.create(string = request.body).dataUsingEncoding(NSUTF8StringEncoding)
                setHTTPBody(data)
            }
        }

        val task = NSURLSession.sharedSession.dataTaskWithRequest(mutableRequest) { data, response, error ->
            if (error != null) {
                continuation.resume(HttpResponse(500, error.localizedDescription))
                return@dataTaskWithRequest
            }
            val httpResponse = response as? NSHTTPURLResponse
            val statusCode = httpResponse?.statusCode?.toInt() ?: 500
            val bodyString = data?.let { NSString.create(data = it, encoding = NSUTF8StringEncoding)?.toString() }.orEmpty()

            continuation.resume(
                HttpResponse(
                    statusCode = statusCode,
                    body = bodyString
                )
            )
        }
        task.resume()
    }
}
