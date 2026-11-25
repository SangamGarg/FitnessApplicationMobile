package com.muscleplay.app.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class GamificationHomeSummaryResponseDto(
    val title: String? = null,
    val subTitle: String? = null,
    val description: String? = null,
    val totalAvailableGames: Int = 0,
    val games: List<GameCatalogueResponseDto> = emptyList(),
    val totalXp: Long = 0L,
    val level: Int = 1,
    val coins: Long = 0L,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastActiveDate: String? = null,
    val xpToNextLevel: Long = 1000L,
)

@Serializable
data class GameCatalogueResponseDto(
    val gameType: String = "",
    val imageUrl: String? = null,
    val title: String = "",
    val subTitle: String? = null,
    val description: String? = null,
    val level: String? = null,
    val totalRepsRequired: Int? = null,
    val durationValue: Int = 0,
    val durationUnit: String? = null,
    val durationLabel: String? = null,
    val paid: Boolean = false,
    val entryFeeCoins: Long = 0L,
    val rewardCoins: Long = 0L,
    val rewardXp: Long = 0L,
    val isNew: Boolean = false,
)

@Serializable
data class GameDetailResponseDto(
    val game: GameCatalogueResponseDto? = null,
    val gifUrl: String? = null,
    val videoUrl: String? = null,
    val rules: List<String> = emptyList(),
    val tips: List<String> = emptyList(),
    val timesPlayed: Long = 0L,
    val bestScore: Long? = null,
)

@Serializable
data class GameStartRequestDto(
    val gameType: String,
    val clientRequestId: String,
)

@Serializable
data class GameSessionResponseDto(
    val sessionId: String = "",
    val gameType: String = "",
    val status: String = "IN_PROGRESS",
    val startedAt: String? = null,
    val completedAt: String? = null,
    val repsDetected: Int? = null,
    val holdSeconds: Int? = null,
    val durationSeconds: Int? = null,
    val score: Long? = null,
    val trackerVersion: String? = null,
    val modelConfidence: Double? = null,
)

@Serializable
data class GameCompleteRequestDto(
    val repsDetected: Int,
    val durationSeconds: Int,
    val trackerVersion: String = "1.0.0",
    val modelConfidence: Double = 1.0,
    val holdSeconds: Int? = null,
)

@Serializable
data class RewardHistoryItemDto(
    val id: Long = 0L,
    val rewardSource: String? = null,
    val xpAwarded: Long = 0L,
    val coinsAwarded: Long = 0L,
    val createdAt: String? = null,
    val description: String? = null,
)

@Serializable
data class RewardHistoryPageResponse(
    val content: List<RewardHistoryItemDto> = emptyList(),
    val page: GalleryPageMetadata = GalleryPageMetadata(),
)

@Serializable
data class GameHistoryPageResponse(
    val content: List<GameSessionResponseDto> = emptyList(),
    val page: GalleryPageMetadata = GalleryPageMetadata(),
)
