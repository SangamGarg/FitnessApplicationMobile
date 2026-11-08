package com.muscleplay.app.core.network

import com.muscleplay.app.core.network.api.AuthApiService
import com.muscleplay.app.core.network.api.DashboardApiService
import com.muscleplay.app.core.network.api.NotificationApiService
import com.muscleplay.app.core.network.api.WorkoutApiService
import com.muscleplay.app.core.network.auth.TokenStorageManager
import com.muscleplay.app.core.network.client.HttpClientFactory
import com.muscleplay.app.core.network.dto.AuthResponseDto
import com.muscleplay.app.core.network.dto.DashboardResponseDto
import com.muscleplay.app.core.network.dto.DeviceRegisterRequestDto
import com.muscleplay.app.core.network.dto.NotificationHistoryResponseDto
import com.muscleplay.app.core.network.dto.NotificationPreferenceRequestDto
import com.muscleplay.app.core.network.dto.OtpActionResponseDto
import com.muscleplay.app.core.network.dto.OtpRequestDto
import com.muscleplay.app.core.network.dto.RefreshTokenRequestDto
import com.muscleplay.app.core.network.dto.RefreshTokenResponseDto
import com.muscleplay.app.core.network.dto.ResetPasswordWithOtpRequestDto
import com.muscleplay.app.core.network.dto.SignInRequestDto
import com.muscleplay.app.core.network.dto.SignUpRequestDto
import com.muscleplay.app.core.network.dto.SyncWorkoutRequestDto
import com.muscleplay.app.core.network.dto.UnreadCountResponseDto
import com.muscleplay.app.core.network.dto.UserDetailsDto
import com.muscleplay.app.core.network.dto.UserDetailsResponseDto
import com.muscleplay.app.core.network.dto.UserDto
import com.muscleplay.app.core.network.dto.UserPatchRequestDto
import com.muscleplay.app.core.network.dto.UserRegisterResponseDto
import com.muscleplay.app.core.network.dto.VerifyEmailOtpRequestDto
import com.muscleplay.app.core.network.dto.WorkoutLogDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

private val jsonParser = Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
}

class ApiClient(
    val baseUrl: String = getPlatformBaseUrl(),
    private val ktorClient: HttpClient = HttpClientFactory.create(),
    var tokenStorageManager: TokenStorageManager? = null,
) : AuthApiService, WorkoutApiService, NotificationApiService, DashboardApiService {

    override suspend fun sendSignupOtp(email: String): NetworkResult<OtpActionResponseDto> {
        return executeKtorRequest(
            url = "$baseUrl/otp/signup/send",
            httpMethod = HttpMethod.Post,
            bodyObj = OtpRequestDto(email = email)
        )
    }

    override suspend fun verifySignupOtp(
        email: String,
        otp: String,
    ): NetworkResult<OtpActionResponseDto> {
        return executeKtorRequest(
            url = "$baseUrl/otp/signup/verify",
            httpMethod = HttpMethod.Post,
            bodyObj = VerifyEmailOtpRequestDto(email = email, otp = otp)
        )
    }

    override suspend fun sendForgotPasswordOtp(email: String): NetworkResult<OtpActionResponseDto> {
        return executeKtorRequest(
            url = "$baseUrl/otp/forgot-password/send",
            httpMethod = HttpMethod.Post,
            bodyObj = OtpRequestDto(email = email)
        )
    }

    override suspend fun verifyForgotPasswordOtp(
        email: String,
        otp: String,
    ): NetworkResult<OtpActionResponseDto> {
        return executeKtorRequest(
            url = "$baseUrl/otp/forgot-password/verify",
            httpMethod = HttpMethod.Post,
            bodyObj = VerifyEmailOtpRequestDto(email = email, otp = otp)
        )
    }

    override suspend fun resetPasswordWithOtp(request: ResetPasswordWithOtpRequestDto): NetworkResult<OtpActionResponseDto> {
        return executeKtorRequest(
            url = "$baseUrl/otp/forgot-password/reset",
            httpMethod = HttpMethod.Post,
            bodyObj = request
        )
    }

    override suspend fun loginRegister(firebaseIdToken: String): NetworkResult<UserRegisterResponseDto> {
        return executeKtorRequest(
            url = "$baseUrl/auth/loginRegister",
            httpMethod = HttpMethod.Post,
            headers = mapOf("Authorization" to "Bearer $firebaseIdToken")
        )
    }

    override suspend fun refreshToken(refreshToken: String): NetworkResult<RefreshTokenResponseDto> {
        return executeKtorRequest(
            url = "$baseUrl/auth/refresh-token",
            httpMethod = HttpMethod.Post,
            bodyObj = RefreshTokenRequestDto(refreshToken = refreshToken)
        )
    }

    override suspend fun logout(
        refreshToken: String,
        logoutEverywhere: Boolean,
        accessToken: String,
    ): NetworkResult<Boolean> {
        val extraHeader =
            if (accessToken.isNotBlank()) mapOf("Authorization" to "Bearer $accessToken") else emptyMap()
        return executeKtorRequest(
            url = "$baseUrl/auth/logout?logoutEverywhere=$logoutEverywhere",
            httpMethod = HttpMethod.Post,
            headers = extraHeader,
            bodyObj = RefreshTokenRequestDto(refreshToken = refreshToken)
        )
    }

    override suspend fun addUserDetails(
        accessToken: String,
        details: UserDetailsDto,
    ): NetworkResult<UserDetailsResponseDto> {
        val authHeader =
            if (accessToken.isNotBlank()) mapOf("Authorization" to "Bearer $accessToken") else emptyMap()
        return executeKtorRequest(
            url = "$baseUrl/auth/add-details",
            httpMethod = HttpMethod.Post,
            headers = authHeader,
            bodyObj = details
        )
    }

    override suspend fun patchUserDetails(
        accessToken: String,
        patch: UserPatchRequestDto,
    ): NetworkResult<UserDetailsResponseDto> {
        val authHeader =
            if (accessToken.isNotBlank()) mapOf("Authorization" to "Bearer $accessToken") else emptyMap()
        return executeKtorRequest(
            url = "$baseUrl/auth/patch-details",
            httpMethod = HttpMethod.Patch,
            headers = authHeader,
            bodyObj = patch
        )
    }

    override suspend fun getUserDetails(accessToken: String): NetworkResult<UserDetailsResponseDto> {
        val authHeader =
            if (accessToken.isNotBlank()) mapOf("Authorization" to "Bearer $accessToken") else emptyMap()
        return executeKtorRequest(
            url = "$baseUrl/auth/get-details",
            httpMethod = HttpMethod.Get,
            headers = authHeader
        )
    }

    override suspend fun deleteUserAccount(accessToken: String): NetworkResult<Boolean> {
        val authHeader =
            if (accessToken.isNotBlank()) mapOf("Authorization" to "Bearer $accessToken") else emptyMap()
        return executeKtorRequest(
            url = "$baseUrl/auth/delete",
            httpMethod = HttpMethod.Delete,
            headers = authHeader
        )
    }

    override suspend fun signIn(request: SignInRequestDto): NetworkResult<AuthResponseDto> {
        return executeKtorRequest(
            url = "$baseUrl/auth/signin",
            httpMethod = HttpMethod.Post,
            bodyObj = request
        )
    }

    override suspend fun signUp(request: SignUpRequestDto): NetworkResult<AuthResponseDto> {
        return executeKtorRequest(
            url = "$baseUrl/auth/signup",
            httpMethod = HttpMethod.Post,
            bodyObj = request
        )
    }

    override suspend fun getUserProfile(authToken: String): NetworkResult<UserDto> {
        val authHeader =
            if (authToken.isNotBlank()) mapOf("Authorization" to "Bearer $authToken") else emptyMap()
        return executeKtorRequest(
            url = "$baseUrl/user/profile",
            httpMethod = HttpMethod.Get,
            headers = authHeader
        )
    }

    override suspend fun fetchWorkoutHistory(authToken: String): NetworkResult<List<WorkoutLogDto>> {
        val authHeader =
            if (authToken.isNotBlank()) mapOf("Authorization" to "Bearer $authToken") else emptyMap()
        return executeKtorRequest(
            url = "$baseUrl/workouts/history",
            httpMethod = HttpMethod.Get,
            headers = authHeader
        )
    }

    override suspend fun syncWorkoutLogs(
        authToken: String,
        request: SyncWorkoutRequestDto,
    ): NetworkResult<Boolean> {
        val authHeader =
            if (authToken.isNotBlank()) mapOf("Authorization" to "Bearer $authToken") else emptyMap()
        return executeKtorRequest(
            url = "$baseUrl/workouts/sync",
            httpMethod = HttpMethod.Post,
            headers = authHeader,
            bodyObj = request
        )
    }

    override suspend fun registerDevice(
        accessToken: String,
        request: DeviceRegisterRequestDto,
    ): NetworkResult<Boolean> {
        val authHeader =
            if (accessToken.isNotBlank()) mapOf("Authorization" to "Bearer $accessToken") else emptyMap()
        return executeKtorRequest(
            url = "$baseUrl/devices/register",
            httpMethod = HttpMethod.Post,
            headers = authHeader,
            bodyObj = request
        )
    }

    override suspend fun unregisterDevice(
        accessToken: String,
        deviceId: String,
    ): NetworkResult<Boolean> {
        val authHeader =
            if (accessToken.isNotBlank()) mapOf("Authorization" to "Bearer $accessToken") else emptyMap()
        return executeKtorRequest(
            url = "$baseUrl/devices/$deviceId",
            httpMethod = HttpMethod.Delete,
            headers = authHeader
        )
    }

    override suspend fun updateNotificationPreference(
        accessToken: String,
        deviceId: String,
        request: NotificationPreferenceRequestDto,
    ): NetworkResult<Boolean> {
        val authHeader =
            if (accessToken.isNotBlank()) mapOf("Authorization" to "Bearer $accessToken") else emptyMap()
        return executeKtorRequest(
            url = "$baseUrl/devices/$deviceId/notifications",
            httpMethod = HttpMethod.Patch,
            headers = authHeader,
            bodyObj = request
        )
    }

    override suspend fun fetchNotificationHistory(
        accessToken: String,
        page: Int,
        size: Int,
        unreadOnly: Boolean,
    ): NetworkResult<NotificationHistoryResponseDto> {
        val authHeader =
            if (accessToken.isNotBlank()) mapOf("Authorization" to "Bearer $accessToken") else emptyMap()
        return executeKtorRequest(
            url = "$baseUrl/notifications?page=$page&size=$size&unreadOnly=$unreadOnly",
            httpMethod = HttpMethod.Get,
            headers = authHeader
        )
    }

    override suspend fun fetchUnreadCount(accessToken: String): NetworkResult<UnreadCountResponseDto> {
        val authHeader =
            if (accessToken.isNotBlank()) mapOf("Authorization" to "Bearer $accessToken") else emptyMap()
        return executeKtorRequest(
            url = "$baseUrl/notifications/unread-count",
            httpMethod = HttpMethod.Get,
            headers = authHeader
        )
    }

    override suspend fun markNotificationAsRead(
        accessToken: String,
        id: String,
    ): NetworkResult<Boolean> {
        val authHeader =
            if (accessToken.isNotBlank()) mapOf("Authorization" to "Bearer $accessToken") else emptyMap()
        return executeKtorRequest(
            url = "$baseUrl/notifications/$id/read",
            httpMethod = HttpMethod.Patch,
            headers = authHeader
        )
    }

    override suspend fun markAllNotificationsAsRead(accessToken: String): NetworkResult<Boolean> {
        val authHeader =
            if (accessToken.isNotBlank()) mapOf("Authorization" to "Bearer $accessToken") else emptyMap()
        return executeKtorRequest(
            url = "$baseUrl/notifications/read-all",
            httpMethod = HttpMethod.Patch,
            headers = authHeader
        )
    }

    override suspend fun fetchDashboard(accessToken: String): NetworkResult<DashboardResponseDto> {
        val authHeader =
            if (accessToken.isNotBlank()) mapOf("Authorization" to "Bearer $accessToken") else emptyMap()
        return executeKtorRequest(
            url = "$baseUrl/dashboard",
            httpMethod = HttpMethod.Get,
            headers = authHeader
        )
    }

    private suspend inline fun <reified T> executeKtorRequest(
        url: String,
        httpMethod: HttpMethod = HttpMethod.Get,
        headers: Map<String, String> = emptyMap(),
        bodyObj: Any? = null,
    ): NetworkResult<T> = withContext(Dispatchers.Default) {
        val storage = tokenStorageManager
        val isAuthEndpoint = url.contains("/auth/loginRegister") ||
                url.contains("/auth/refresh-token") ||
                url.contains("/auth/signin") ||
                url.contains("/auth/signup") ||
                url.contains("/otp/")

        val currentToken = storage?.accessToken
        val authHeaders =
            if (!headers.containsKey("Authorization") && !currentToken.isNullOrBlank() && !isAuthEndpoint) {
                headers + ("Authorization" to "Bearer $currentToken")
            } else {
                headers
            }

        var result = sendKtorRequest<T>(url, httpMethod, authHeaders, bodyObj)

        // Handle 401 Unauthorized for protected endpoints with automatic thread-safe token refresh retry
        if (result is NetworkResult.Error && result.statusCode == 401 && !isAuthEndpoint && storage != null) {
            val failedToken = authHeaders["Authorization"]?.removePrefix("Bearer ")
            val refreshResult = storage.performTokenRefresh(
                refreshCall = { rToken -> refreshToken(rToken) },
                failedToken = failedToken
            )

            if (refreshResult is NetworkResult.Success) {
                val newAccessToken = refreshResult.data
                val retriedHeaders = headers + ("Authorization" to "Bearer $newAccessToken")
                result = sendKtorRequest<T>(url, httpMethod, retriedHeaders, bodyObj)
            }
        }

        result
    }

    private suspend inline fun <reified T> sendKtorRequest(
        url: String,
        httpMethod: HttpMethod,
        headers: Map<String, String>,
        bodyObj: Any?,
    ): NetworkResult<T> {
        return try {
            val response = ktorClient.request(url) {
                method = httpMethod
                headers.forEach { (key, value) ->
                    header(key, value)
                }
                if (bodyObj != null) {
                    contentType(ContentType.Application.Json)
                    setBody(bodyObj)
                }
            }

            if (response.status.isSuccess()) {
                if (T::class == Boolean::class) {
                    @Suppress("UNCHECKED_CAST")
                    NetworkResult.Success(true as T)
                } else {
                    val parsed: T = response.body()
                    NetworkResult.Success(parsed)
                }
            } else {
                val errorBody = response.bodyAsText()
                val errorMsg = parseErrorMessage(errorBody, "Server error (${response.status.value})")
                NetworkResult.Error(response.status.value, errorMsg)
            }
        } catch (e: Throwable) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            NetworkResult.Exception(e)
        }
    }

    private fun parseErrorMessage(json: String, fallback: String): String {
        if (json.isBlank()) return fallback
        return try {
            val element = jsonParser.parseToJsonElement(json)
            val jsonObject = element as? JsonObject ?: return fallback
            jsonObject["message"]?.let { (it as? JsonPrimitive)?.content }
                ?: jsonObject["error"]?.let { (it as? JsonPrimitive)?.content }
                ?: jsonObject["status"]?.let { (it as? JsonPrimitive)?.content }
                ?: fallback
        } catch (e: Throwable) {
            fallback
        }
    }
}
