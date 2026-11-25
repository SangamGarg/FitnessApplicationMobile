package com.muscleplay.app.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class FeedbackRequestDto(
    val feedback: String,
)

@Serializable
data class FeedbackResponseDto(
    val status: String? = null,
    val statusCode: Int? = null,
    val message: String? = null,
    val timestamp: String? = null,
)

@Serializable
data class FollowUsLinkDto(
    val id: Long? = null,
    val title: String? = null,
    val subtitle: String? = null,
    val iconKey: String? = null,
    val url: String? = null,
)

@Serializable
data class XpProgressDto(
    val currentLevel: Int? = 1,
    val currentXp: Long? = 0L,
    val currentLevelRequiredXp: Long? = 0L,
    val nextLevel: Int? = 2,
    val nextLevelRequiredXp: Long? = 1000L,
    val xpToNextLevel: Long? = 1000L,
    val progressPercentage: Double? = 0.0,
)

@Serializable
data class BadgeResponseDto(
    val id: Long = 0L,
    val name: String = "",
    val description: String? = null,
    val imageUrl: String? = null,
    val category: String? = null,
    val unlocked: Boolean = false,
    val unlockedAt: String? = null,
)

@Serializable
data class ProgressGalleryResponseDto(
    val id: Long = 0L,
    val imageUrl: String = "",
    val caption: String? = null,
    val uploadedAt: String? = null,
    val category: String? = null,
)

@Serializable
data class ProfileHomeResponse(
    val name: String? = null,
    val email: String? = null,
    val profileImageUrl: String? = null,
    val totalCoins: Long? = 0L,
    val currentStreak: Int? = 0,
    val longestStreak: Int? = 0,
    val lastActiveDate: String? = null,
    val xp: XpProgressDto? = null,
    val badges: List<BadgeResponseDto> = emptyList(),
    val progressGallery: List<ProgressGalleryResponseDto> = emptyList(),
)

@Serializable
data class GalleryPageMetadata(
    val size: Int = 20,
    val number: Int = 0,
    val totalElements: Long = 0L,
    val totalPages: Int = 0,
)

@Serializable
data class ProgressGalleryPageResponse(
    val content: List<ProgressGalleryResponseDto> = emptyList(),
    val page: GalleryPageMetadata = GalleryPageMetadata(),
)
