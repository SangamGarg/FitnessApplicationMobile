package com.muscleplay.app.core.network

import com.muscleplay.app.core.network.api.AuthApiService
import com.muscleplay.app.core.network.api.WorkoutApiService
import com.muscleplay.app.core.network.dto.*

class ApiClient(
    val baseUrl: String = "https://api.muscleplay.com/v1",
    private val httpClient: PlatformHttpClient = PlatformHttpClient()
) : AuthApiService, WorkoutApiService {

    override suspend fun signIn(request: SignInRequestDto): NetworkResult<AuthResponseDto> {
        return executeRequest(
            url = "$baseUrl/auth/signin",
            method = "POST",
            body = "{\"email\":\"${request.email}\",\"password\":\"${request.passwordHash}\"}"
        ) { body ->
            AuthResponseDto(
                token = "sample_jwt_token_xyz",
                refreshToken = "sample_refresh_token_abc",
                user = UserDto(
                    id = "usr_101",
                    name = "Alex Fitness",
                    email = request.email,
                    streakDays = 12,
                    membershipStatus = "Pro Member"
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
                token = "sample_jwt_token_xyz",
                refreshToken = "sample_refresh_token_abc",
                user = UserDto(
                    id = "usr_102",
                    name = request.fullName,
                    email = request.email,
                    streakDays = 1,
                    membershipStatus = "Free Member"
                )
            )
        }
    }

    override suspend fun getUserProfile(authToken: String): NetworkResult<UserDto> {
        return executeRequest(
            url = "$baseUrl/user/profile",
            method = "GET",
            headers = mapOf("Authorization" to "Bearer $authToken")
        ) { body ->
            UserDto(
                id = "usr_101",
                name = "Alex Fitness",
                email = "alex.fitness@muscleplay.com",
                streakDays = 12,
                membershipStatus = "Pro Member"
            )
        }
    }

    override suspend fun fetchWorkoutHistory(authToken: String): NetworkResult<List<WorkoutLogDto>> {
        return executeRequest(
            url = "$baseUrl/workouts/history",
            method = "GET",
            headers = mapOf("Authorization" to "Bearer $authToken")
        ) { body ->
            listOf(
                WorkoutLogDto("w1", "Lower Body Blitz", "Squat", 20, 180, "2025-02-20T10:00:00Z"),
                WorkoutLogDto("w2", "Upper Body Master", "Push-up", 15, 140, "2025-02-19T18:30:00Z")
            )
        }
    }

    override suspend fun syncWorkoutLogs(
        authToken: String,
        request: SyncWorkoutRequestDto
    ): NetworkResult<Boolean> {
        return executeRequest(
            url = "$baseUrl/workouts/sync",
            method = "POST",
            headers = mapOf("Authorization" to "Bearer $authToken"),
            body = "{\"logsCount\":${request.workoutLogs.size}}"
        ) { true }
    }

    private suspend fun <T> executeRequest(
        url: String,
        method: String = "GET",
        headers: Map<String, String> = emptyMap(),
        body: String? = null,
        parseData: (String) -> T
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
                NetworkResult.Error(response.statusCode, response.body)
            }
        } catch (e: Throwable) {
            NetworkResult.Exception(e)
        }
    }
}
