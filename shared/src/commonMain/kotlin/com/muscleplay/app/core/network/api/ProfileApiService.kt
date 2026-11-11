package com.muscleplay.app.core.network.api

import com.muscleplay.app.core.network.NetworkResult
import com.muscleplay.app.core.network.dto.FeedbackRequestDto
import com.muscleplay.app.core.network.dto.FeedbackResponseDto
import com.muscleplay.app.core.network.dto.FollowUsLinkDto

interface ProfileApiService {
    suspend fun submitFeedback(accessToken: String, request: FeedbackRequestDto): NetworkResult<FeedbackResponseDto>
    suspend fun fetchFollowUsLinks(accessToken: String): NetworkResult<List<FollowUsLinkDto>>
}
