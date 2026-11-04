package com.muscleplay.app.core.network

import com.muscleplay.app.core.network.api.AuthApiService
import com.muscleplay.app.core.network.api.NotificationApiService
import com.muscleplay.app.core.network.api.WorkoutApiService
import com.muscleplay.app.core.network.auth.TokenStorageManager
import com.muscleplay.app.core.network.dto.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ApiClient(
    val baseUrl: String = "http://192.168.1.3:8080/api/v1/app",
    private val httpClient: PlatformHttpClient = PlatformHttpClient(),
    var tokenStorageManager: TokenStorageManager? = null
) : AuthApiService, WorkoutApiService, NotificationApiService {

    override suspend fun sendSignupOtp(email: String): NetworkResult<OtpActionResponseDto> {
        return executeRequest(
            url = "$baseUrl/otp/signup/send",
            method = "POST",
            body = "{\"email\":\"$email\"}"
        ) { body ->
            OtpActionResponseDto(
                status = SimpleJsonParser.parseString(body, "status") ?: "SUCCESS",
                statusCode = SimpleJsonParser.parseInt(body, "statusCode") ?: 200,
                message = SimpleJsonParser.parseString(body, "message") ?: "OTP sent to $email",
                code = SimpleJsonParser.parseString(body, "code"),
                expiresInSeconds = SimpleJsonParser.parseLong(body, "expiresInSeconds") ?: 300L,
                verificationToken = SimpleJsonParser.parseString(body, "verificationToken")
            )
        }
    }

    override suspend fun verifySignupOtp(
        email: String,
        otp: String,
    ): NetworkResult<OtpActionResponseDto> {
        return executeRequest(
            url = "$baseUrl/otp/signup/verify",
            method = "POST",
            body = "{\"email\":\"$email\",\"otp\":\"$otp\"}"
        ) { body ->
            OtpActionResponseDto(
                status = SimpleJsonParser.parseString(body, "status") ?: "SUCCESS",
                statusCode = SimpleJsonParser.parseInt(body, "statusCode") ?: 200,
                message = SimpleJsonParser.parseString(body, "message")
                    ?: "OTP verified successfully",
                verificationToken = SimpleJsonParser.parseString(body, "verificationToken")
            )
        }
    }

    override suspend fun sendForgotPasswordOtp(email: String): NetworkResult<OtpActionResponseDto> {
        return executeRequest(
            url = "$baseUrl/otp/forgot-password/send",
            method = "POST",
            body = "{\"email\":\"$email\"}"
        ) { body ->
            OtpActionResponseDto(
                status = SimpleJsonParser.parseString(body, "status") ?: "SUCCESS",
                statusCode = SimpleJsonParser.parseInt(body, "statusCode") ?: 200,
                message = SimpleJsonParser.parseString(body, "message") ?: "OTP sent to $email",
                code = SimpleJsonParser.parseString(body, "code"),
                expiresInSeconds = SimpleJsonParser.parseLong(body, "expiresInSeconds") ?: 300L,
                verificationToken = SimpleJsonParser.parseString(body, "verificationToken")
            )
        }
    }

    override suspend fun verifyForgotPasswordOtp(
        email: String,
        otp: String,
    ): NetworkResult<OtpActionResponseDto> {
        return executeRequest(
            url = "$baseUrl/otp/forgot-password/verify",
            method = "POST",
            body = "{\"email\":\"$email\",\"otp\":\"$otp\"}"
        ) { body ->
            OtpActionResponseDto(
                status = SimpleJsonParser.parseString(body, "status") ?: "SUCCESS",
                statusCode = SimpleJsonParser.parseInt(body, "statusCode") ?: 200,
                message = SimpleJsonParser.parseString(body, "message")
                    ?: "OTP verified successfully",
                verificationToken = SimpleJsonParser.parseString(body, "verificationToken")
            )
        }
    }

    override suspend fun resetPasswordWithOtp(request: ResetPasswordWithOtpRequestDto): NetworkResult<OtpActionResponseDto> {
        return executeRequest(
            url = "$baseUrl/otp/forgot-password/reset",
            method = "POST",
            body = "{\"email\":\"${request.email}\",\"verificationToken\":\"${request.verificationToken}\",\"newPassword\":\"${request.newPassword}\"}"
        ) { body ->
            OtpActionResponseDto(
                status = SimpleJsonParser.parseString(body, "status") ?: "SUCCESS",
                statusCode = SimpleJsonParser.parseInt(body, "statusCode") ?: 200,
                message = SimpleJsonParser.parseString(body, "message")
                    ?: "Password reset successfully"
            )
        }
    }

    override suspend fun loginRegister(firebaseIdToken: String): NetworkResult<UserRegisterResponseDto> {
        return executeRequest(
            url = "$baseUrl/auth/loginRegister",
            method = "POST",
            headers = mapOf("Authorization" to "Bearer $firebaseIdToken")
        ) { body ->
            UserRegisterResponseDto(
                status = SimpleJsonParser.parseString(body, "status") ?: "SUCCESS",
                statusCode = SimpleJsonParser.parseInt(body, "statusCode") ?: 200,
                message = SimpleJsonParser.parseString(body, "message"),
                tokenType = SimpleJsonParser.parseString(body, "tokenType") ?: "Bearer",
                accessToken = SimpleJsonParser.parseString(body, "accessToken"),
                refreshToken = SimpleJsonParser.parseString(body, "refreshToken"),
                isNewUser = SimpleJsonParser.parseBoolean(body, "isNewUser"),
                profileComplete = SimpleJsonParser.parseBoolean(body, "profileComplete"),
                userDetailsDto = UserDetailsDto(
                    name = SimpleJsonParser.parseString(body, "name"),
                    email = SimpleJsonParser.parseString(body, "email")
                )
            )
        }
    }

    override suspend fun refreshToken(refreshToken: String): NetworkResult<RefreshTokenResponseDto> {
        return executeRequest(
            url = "$baseUrl/auth/refresh-token",
            method = "POST",
            body = "{\"refreshToken\":\"$refreshToken\"}"
        ) { body ->
            RefreshTokenResponseDto(
                status = SimpleJsonParser.parseString(body, "status") ?: "SUCCESS",
                statusCode = SimpleJsonParser.parseInt(body, "statusCode") ?: 200,
                message = SimpleJsonParser.parseString(body, "message"),
                tokenType = SimpleJsonParser.parseString(body, "tokenType") ?: "Bearer",
                accessToken = SimpleJsonParser.parseString(body, "accessToken"),
                refreshToken = SimpleJsonParser.parseString(body, "refreshToken")
            )
        }
    }

    override suspend fun logout(
        refreshToken: String,
        logoutEverywhere: Boolean,
        accessToken: String
    ): NetworkResult<Boolean> {
        val extraHeader = if (accessToken.isNotBlank()) mapOf("Authorization" to "Bearer $accessToken") else emptyMap()
        return executeRequest(
            url = "$baseUrl/auth/logout?logoutEverywhere=$logoutEverywhere",
            method = "POST",
            headers = extraHeader,
            body = "{\"refreshToken\":\"$refreshToken\"}"
        ) { true }
    }

    override suspend fun addUserDetails(
        accessToken: String,
        details: UserDetailsDto,
    ): NetworkResult<UserDetailsResponseDto> {
        val authHeader = if (accessToken.isNotBlank()) mapOf("Authorization" to "Bearer $accessToken") else emptyMap()
        return executeRequest(
            url = "$baseUrl/auth/add-details",
            method = "POST",
            headers = authHeader,
            body = "{\"name\":\"${details.name ?: ""}\",\"gender\":\"${details.gender ?: ""}\",\"age\":${details.age ?: 25},\"heightCm\":${details.heightCm ?: 175f},\"weightKg\":${details.weightKg ?: 70f},\"activityLevel\":\"${details.activityLevel ?: ""}\",\"goal\":\"${details.goal ?: ""}\"}"
        ) { body ->
            UserDetailsResponseDto(
                status = SimpleJsonParser.parseString(body, "status") ?: "SUCCESS",
                statusCode = SimpleJsonParser.parseInt(body, "statusCode") ?: 200,
                message = SimpleJsonParser.parseString(body, "message") ?: "Profile completed",
                userDetailsDto = details
            )
        }
    }

    override suspend fun patchUserDetails(
        accessToken: String,
        patch: UserPatchRequestDto,
    ): NetworkResult<UserDetailsResponseDto> {
        val authHeader = if (accessToken.isNotBlank()) mapOf("Authorization" to "Bearer $accessToken") else emptyMap()
        return executeRequest(
            url = "$baseUrl/auth/patch-details",
            method = "PATCH",
            headers = authHeader,
            body = "{\"name\":\"${patch.name ?: ""}\"}"
        ) { body ->
            UserDetailsResponseDto(
                status = SimpleJsonParser.parseString(body, "status") ?: "SUCCESS",
                statusCode = SimpleJsonParser.parseInt(body, "statusCode") ?: 200,
                message = SimpleJsonParser.parseString(body, "message") ?: "Profile updated",
                userDetailsDto = UserDetailsDto(name = patch.name)
            )
        }
    }

    override suspend fun getUserDetails(accessToken: String): NetworkResult<UserDetailsResponseDto> {
        val authHeader = if (accessToken.isNotBlank()) mapOf("Authorization" to "Bearer $accessToken") else emptyMap()
        return executeRequest(
            url = "$baseUrl/auth/get-details",
            method = "GET",
            headers = authHeader
        ) { body ->
            UserDetailsResponseDto(
                status = SimpleJsonParser.parseString(body, "status") ?: "SUCCESS",
                statusCode = SimpleJsonParser.parseInt(body, "statusCode") ?: 200,
                userDetailsDto = UserDetailsDto(
                    name = SimpleJsonParser.parseString(body, "name"),
                    email = SimpleJsonParser.parseString(body, "email"),
                    gender = SimpleJsonParser.parseString(body, "gender"),
                    age = SimpleJsonParser.parseInt(body, "age"),
                    heightCm = SimpleJsonParser.parseFloat(body, "heightCm"),
                    weightKg = SimpleJsonParser.parseFloat(body, "weightKg"),
                    activityLevel = SimpleJsonParser.parseString(body, "activityLevel"),
                    goal = SimpleJsonParser.parseString(body, "goal")
                )
            )
        }
    }

    override suspend fun deleteUserAccount(accessToken: String): NetworkResult<Boolean> {
        val authHeader = if (accessToken.isNotBlank()) mapOf("Authorization" to "Bearer $accessToken") else emptyMap()
        return executeRequest(
            url = "$baseUrl/auth/delete",
            method = "DELETE",
            headers = authHeader
        ) { true }
    }

    override suspend fun signIn(request: SignInRequestDto): NetworkResult<AuthResponseDto> {
        return executeRequest(
            url = "$baseUrl/auth/signin",
            method = "POST",
            body = "{\"email\":\"${request.email}\",\"password\":\"${request.passwordHash}\"}"
        ) { body ->
            AuthResponseDto(
                token = SimpleJsonParser.parseString(body, "token") ?: "",
                refreshToken = SimpleJsonParser.parseString(body, "refreshToken") ?: "",
                user = UserDto(
                    id = SimpleJsonParser.parseString(body, "id") ?: "",
                    name = SimpleJsonParser.parseString(body, "name") ?: "",
                    email = request.email,
                    streakDays = SimpleJsonParser.parseInt(body, "streakDays") ?: 0,
                    membershipStatus = SimpleJsonParser.parseString(body, "membershipStatus") ?: ""
                )
            )
        }
    }

    override suspend fun signUp(request: SignUpRequestDto): NetworkResult<AuthResponseDto> {
        return executeRequest(
            url = "$baseUrl/auth/signup",
            method = "POST",
            body = "{\"fullName\":\"${request.fullName}\",\"email\":\"${request.email}\",\"password\":\"${request.passwordHash}\"}"
        ) { body ->
            AuthResponseDto(
                token = SimpleJsonParser.parseString(body, "token") ?: "",
                refreshToken = SimpleJsonParser.parseString(body, "refreshToken") ?: "",
                user = UserDto(
                    id = SimpleJsonParser.parseString(body, "id") ?: "",
                    name = request.fullName,
                    email = request.email,
                    streakDays = 0,
                    membershipStatus = "Free Member"
                )
            )
        }
    }

    override suspend fun getUserProfile(authToken: String): NetworkResult<UserDto> {
        val authHeader = if (authToken.isNotBlank()) mapOf("Authorization" to "Bearer $authToken") else emptyMap()
        return executeRequest(
            url = "$baseUrl/user/profile",
            method = "GET",
            headers = authHeader
        ) { body ->
            UserDto(
                id = SimpleJsonParser.parseString(body, "id") ?: "",
                name = SimpleJsonParser.parseString(body, "name") ?: "",
                email = SimpleJsonParser.parseString(body, "email") ?: "",
                streakDays = SimpleJsonParser.parseInt(body, "streakDays") ?: 0,
                membershipStatus = SimpleJsonParser.parseString(body, "membershipStatus") ?: ""
            )
        }
    }

    override suspend fun fetchWorkoutHistory(authToken: String): NetworkResult<List<WorkoutLogDto>> {
        val authHeader = if (authToken.isNotBlank()) mapOf("Authorization" to "Bearer $authToken") else emptyMap()
        return executeRequest(
            url = "$baseUrl/workouts/history",
            method = "GET",
            headers = authHeader
        ) { emptyList() }
    }

    override suspend fun syncWorkoutLogs(
        authToken: String,
        request: SyncWorkoutRequestDto,
    ): NetworkResult<Boolean> {
        val authHeader = if (authToken.isNotBlank()) mapOf("Authorization" to "Bearer $authToken") else emptyMap()
        return executeRequest(
            url = "$baseUrl/workouts/sync",
            method = "POST",
            headers = authHeader,
            body = "{\"logsCount\":${request.workoutLogs.size}}"
        ) { true }
    }

    override suspend fun registerDevice(
        accessToken: String,
        request: DeviceRegisterRequestDto,
    ): NetworkResult<Boolean> {
        val authHeader = if (accessToken.isNotBlank()) mapOf("Authorization" to "Bearer $accessToken") else emptyMap()
        return executeRequest(
            url = "$baseUrl/devices/register",
            method = "POST",
            headers = authHeader,
            body = "{\"deviceId\":\"${request.deviceId}\",\"fcmToken\":\"${request.fcmToken}\",\"platform\":\"${request.platform}\",\"deviceName\":\"${request.deviceName}\",\"appVersion\":\"${request.appVersion}\",\"notificationEnabled\":${request.notificationEnabled}}"
        ) { true }
    }

    override suspend fun unregisterDevice(
        accessToken: String,
        deviceId: String,
    ): NetworkResult<Boolean> {
        val authHeader = if (accessToken.isNotBlank()) mapOf("Authorization" to "Bearer $accessToken") else emptyMap()
        return executeRequest(
            url = "$baseUrl/devices/$deviceId",
            method = "DELETE",
            headers = authHeader
        ) { true }
    }

    override suspend fun updateNotificationPreference(
        accessToken: String,
        deviceId: String,
        request: NotificationPreferenceRequestDto,
    ): NetworkResult<Boolean> {
        val authHeader = if (accessToken.isNotBlank()) mapOf("Authorization" to "Bearer $accessToken") else emptyMap()
        return executeRequest(
            url = "$baseUrl/devices/$deviceId/notifications",
            method = "PATCH",
            headers = authHeader,
            body = "{\"notificationEnabled\":${request.notificationEnabled}}"
        ) { true }
    }

    override suspend fun fetchNotificationHistory(
        accessToken: String,
        page: Int,
        size: Int,
        unreadOnly: Boolean,
    ): NetworkResult<NotificationHistoryResponseDto> {
        val authHeader = if (accessToken.isNotBlank()) mapOf("Authorization" to "Bearer $accessToken") else emptyMap()
        return executeRequest(
            url = "$baseUrl/notifications?page=$page&size=$size&unreadOnly=$unreadOnly",
            method = "GET",
            headers = authHeader
        ) { body ->
            NotificationHistoryResponseDto(
                content = emptyList(),
                page = page,
                size = size,
                totalElements = 0,
                totalPages = 0,
                last = true,
                unreadCount = SimpleJsonParser.parseInt(body, "unreadCount") ?: 0
            )
        }
    }

    override suspend fun fetchUnreadCount(accessToken: String): NetworkResult<UnreadCountResponseDto> {
        val authHeader = if (accessToken.isNotBlank()) mapOf("Authorization" to "Bearer $accessToken") else emptyMap()
        return executeRequest(
            url = "$baseUrl/notifications/unread-count",
            method = "GET",
            headers = authHeader
        ) { body ->
            UnreadCountResponseDto(
                unreadCount = SimpleJsonParser.parseInt(body, "unreadCount") ?: 0
            )
        }
    }

    override suspend fun markNotificationAsRead(
        accessToken: String,
        id: String,
    ): NetworkResult<Boolean> {
        val authHeader = if (accessToken.isNotBlank()) mapOf("Authorization" to "Bearer $accessToken") else emptyMap()
        return executeRequest(
            url = "$baseUrl/notifications/$id/read",
            method = "PATCH",
            headers = authHeader
        ) { true }
    }

    override suspend fun markAllNotificationsAsRead(accessToken: String): NetworkResult<Boolean> {
        val authHeader = if (accessToken.isNotBlank()) mapOf("Authorization" to "Bearer $accessToken") else emptyMap()
        return executeRequest(
            url = "$baseUrl/notifications/read-all",
            method = "PATCH",
            headers = authHeader
        ) { true }
    }

    private suspend fun <T> executeRequest(
        url: String,
        method: String = "GET",
        headers: Map<String, String> = emptyMap(),
        body: String? = null,
        parseData: (String) -> T,
    ): NetworkResult<T> = withContext(Dispatchers.Default) {
        val storage = tokenStorageManager
        val isAuthEndpoint = url.contains("/auth/loginRegister") ||
                url.contains("/auth/refresh-token") ||
                url.contains("/auth/signin") ||
                url.contains("/auth/signup") ||
                url.contains("/otp/")

        val currentToken = storage?.accessToken
        val authHeaders = if (!headers.containsKey("Authorization") && !currentToken.isNullOrBlank() && !isAuthEndpoint) {
            headers + ("Authorization" to "Bearer $currentToken")
        } else {
            headers
        }

        var result = sendHttpRequest(url, method, authHeaders, body, parseData)

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
                result = sendHttpRequest(url, method, retriedHeaders, body, parseData)
            }
        }

        result
    }

    private suspend fun <T> sendHttpRequest(
        url: String,
        method: String,
        headers: Map<String, String>,
        body: String?,
        parseData: (String) -> T,
    ): NetworkResult<T> {
        return try {
            val response = httpClient.execute(
                HttpRequest(
                    url = url,
                    method = method,
                    headers = mapOf(
                        "Content-Type" to "application/json",
                        "Accept" to "application/json"
                    ) + headers,
                    body = body
                )
            )

            if (response.statusCode in 200..299) {
                NetworkResult.Success(parseData(response.body))
            } else {
                val errorMsg = SimpleJsonParser.parseErrorMessage(
                    response.body,
                    "Server error (${response.statusCode})"
                )
                NetworkResult.Error(response.statusCode, errorMsg)
            }
        } catch (e: Throwable) {
            NetworkResult.Exception(e)
        }
    }
}
