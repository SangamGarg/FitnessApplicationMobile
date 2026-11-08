package com.muscleplay.app.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class OtpRequestDto(
    val email: String,
)

@Serializable
data class VerifyEmailOtpRequestDto(
    val email: String,
    val otp: String,
)

@Serializable
data class ResetPasswordWithOtpRequestDto(
    val email: String,
    val verificationToken: String,
    val newPassword: String,
)

@Serializable
data class OtpActionResponseDto(
    val status: String? = null,
    val statusCode: Int? = null,
    val message: String? = null,
    val timestamp: String? = null,
    val expiresInSeconds: Long? = null,
    val verificationToken: String? = null,
)

@Serializable
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
    val bodyFat: Float? = null,
)

@Serializable
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
    val userDetailsDto: UserDetailsDto? = null,
)

@Serializable
data class UserPatchRequestDto(
    val name: String? = null,
    val firebaseCloudMessagingToken: List<String>? = null,
    val gender: String? = null,
    val age: Int? = null,
    val heightCm: Float? = null,
    val weightKg: Float? = null,
    val activityLevel: String? = null,
    val goal: String? = null,
)

@Serializable
data class UserDetailsResponseDto(
    val status: String? = null,
    val statusCode: Int? = null,
    val message: String? = null,
    val userDetailsDto: UserDetailsDto? = null,
)

@Serializable
data class RefreshTokenRequestDto(
    val refreshToken: String,
)

@Serializable
data class RefreshTokenResponseDto(
    val status: String? = null,
    val statusCode: Int? = null,
    val message: String? = null,
    val tokenType: String? = "Bearer",
    val accessToken: String? = null,
    val refreshToken: String? = null,
)

@Serializable
data class SignInRequestDto(
    val email: String,
    val passwordHash: String,
)

@Serializable
data class SignUpRequestDto(
    val fullName: String,
    val email: String,
    val passwordHash: String,
)

@Serializable
data class UserDto(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val streakDays: Int = 0,
    val membershipStatus: String = "",
)

@Serializable
data class AuthResponseDto(
    val token: String = "",
    val refreshToken: String = "",
    val user: UserDto? = null,
)
