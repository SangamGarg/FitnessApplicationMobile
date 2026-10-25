package com.muscleplay.app.core.network.dto

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
