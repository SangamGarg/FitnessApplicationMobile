package com.muscleplay.app.feature.profile.data

import com.muscleplay.app.core.network.ApiClient
import com.muscleplay.app.core.network.NetworkResult
import com.muscleplay.app.core.network.auth.TokenStorageManager
import com.muscleplay.app.core.network.dto.FeedbackRequestDto
import com.muscleplay.app.core.network.dto.FeedbackResponseDto
import com.muscleplay.app.core.network.dto.FollowUsLinkDto

interface ProfileRepository {
    suspend fun submitFeedback(feedback: String): NetworkResult<FeedbackResponseDto>
    suspend fun fetchFollowUsLinks(): NetworkResult<List<FollowUsLinkDto>>
}

class ProfileRepositoryImpl(
    private val apiClient: ApiClient,
    private val tokenStorageManager: TokenStorageManager,
) : ProfileRepository {
    override suspend fun submitFeedback(feedback: String): NetworkResult<FeedbackResponseDto> {
        val accessToken = tokenStorageManager.accessToken ?: ""
        return apiClient.submitFeedback(accessToken, FeedbackRequestDto(feedback = feedback))
    }

    override suspend fun fetchFollowUsLinks(): NetworkResult<List<FollowUsLinkDto>> {
        val accessToken = tokenStorageManager.accessToken ?: ""
        return apiClient.fetchFollowUsLinks(accessToken)
    }
}
