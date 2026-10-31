package com.muscleplay.app.core.network.dto

data class OtpRequestDto(
    val email: String
)

data class VerifyEmailOtpRequestDto(
    val email: String,
    val otp: String
)

data class ResetPasswordWithOtpRequestDto(
    val email: String,
    val verificationToken: String,
    val newPassword: String
)

data class OtpActionResponseDto(
    val status: String? = null,
    val statusCode: Int? = null,
    val message: String? = null,
    val timestamp: String? = null,
    val code: String? = null,
    val expiresInSeconds: Long? = null,
    val verificationToken: String? = null
)

data class UserDetailsDto(
    val name: String? = null,
    val email: String? = null,
    val firebaseCloudMessagingToken: List<String>? = null,
    val gender: String? = null,
    val age: Int? = null,
    val profileImageUrl: String? = null,
    val heightCm: Float? = null,
    val weightKg: Float? = null,
    val activityLevel: String? = null,
    val goal: String? = null,
    val bmi: Float? = null,
    val dailyCalories: Float? = null,
    val idealWeight: Float? = null,
    val bodyFat: Float? = null
)

data class UserRegisterResponseDto(
    val status: String? = null,
    val statusCode: Int? = null,
    val message: String? = null,
    val timestamp: String? = null,
    val tokenType: String? = "Bearer",
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val isNewUser: Boolean = false,
    val profileComplete: Boolean = false,
    val userDetailsDto: UserDetailsDto? = null
)

data class UserPatchRequestDto(
    val name: String? = null,
    val firebaseCloudMessagingToken: List<String>? = null,
    val gender: String? = null,
    val age: Int? = null,
    val profileImageUrl: String? = null,
    val heightCm: Float? = null,
    val weightKg: Float? = null,
    val activityLevel: String? = null,
    val goal: String? = null,
    val hipCm: Float? = null,
    val neckCm: Float? = null,
    val waistCm: Float? = null
)

data class UserDetailsResponseDto(
    val status: String? = null,
    val statusCode: Int? = null,
    val message: String? = null,
    val timestamp: String? = null,
    val userDetailsDto: UserDetailsDto? = null
)

data class RefreshTokenRequestDto(
    val refreshToken: String
)

data class RefreshTokenResponseDto(
    val status: String? = null,
    val statusCode: Int? = null,
    val message: String? = null,
    val timestamp: String? = null,
    val tokenType: String? = "Bearer",
    val accessToken: String? = null,
    val refreshToken: String? = null
)

data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val streakDays: Int,
    val membershipStatus: String
)

data class SignInRequestDto(
    val email: String,
    val passwordHash: String
)

data class SignUpRequestDto(
    val fullName: String,
    val email: String,
    val passwordHash: String
)

data class AuthResponseDto(
    val token: String,
    val refreshToken: String,
    val user: UserDto
)
