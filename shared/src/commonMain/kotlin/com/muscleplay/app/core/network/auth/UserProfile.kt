package com.muscleplay.app.core.network.auth

data class UserProfile(
    val userId: String? = null,
    val email: String? = null,
    val name: String? = null,
    val profileImageUrl: String? = null,
    val gender: String? = null,
    val age: Int? = null,
    val heightCm: Double? = null,
    val weightKg: Double? = null,
    val activityLevel: String? = null,
    val goal: String? = null,
    val profileCompleted: Boolean = false,
)
