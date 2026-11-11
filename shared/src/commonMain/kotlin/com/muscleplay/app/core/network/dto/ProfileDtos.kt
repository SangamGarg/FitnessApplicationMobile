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
