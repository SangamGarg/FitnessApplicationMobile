package com.muscleplay.app.core.network.auth

import com.muscleplay.app.core.network.dto.DashboardResponseDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface UserSessionRepository {
    val userProfile: StateFlow<UserProfile?>
    fun updateProfile(profile: UserProfile)
    fun updateProfileFromSummary(summary: DashboardResponseDto.UserSummary)
    fun clearSession()
}

class UserSessionRepositoryImpl : UserSessionRepository {
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    override val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    override fun updateProfile(profile: UserProfile) {
        _userProfile.value = profile
    }

    override fun updateProfileFromSummary(summary: DashboardResponseDto.UserSummary) {
        val current = _userProfile.value
        val updated = UserProfile(
            userId = summary.userId ?: current?.userId,
            email = summary.email ?: current?.email,
            name = summary.name ?: current?.name,
            profileImageUrl = summary.profileImageUrl ?: current?.profileImageUrl,
            gender = summary.gender ?: current?.gender,
            age = summary.age ?: current?.age,
            heightCm = summary.heightCm ?: current?.heightCm,
            weightKg = summary.weightKg ?: current?.weightKg,
            activityLevel = summary.activityLevel ?: current?.activityLevel,
            goal = summary.goal ?: current?.goal,
            profileCompleted = summary.profileCompleted
        )
        _userProfile.value = updated
    }

    override fun clearSession() {
        _userProfile.value = null
    }
}
