package com.muscleplay.app.core.network.api

import com.muscleplay.app.core.network.NetworkResult
import com.muscleplay.app.core.network.dto.FeedbackRequestDto
import com.muscleplay.app.core.network.dto.FeedbackResponseDto
import com.muscleplay.app.core.network.dto.FollowUsLinkDto
import com.muscleplay.app.core.network.dto.ProfileHomeResponse
import com.muscleplay.app.core.network.dto.ProgressGalleryPageResponse
import com.muscleplay.app.core.network.dto.ProgressGalleryResponseDto
import com.muscleplay.app.core.network.dto.SupportAttachment

interface ProfileApiService {
    suspend fun submitFeedback(accessToken: String, request: FeedbackRequestDto): NetworkResult<FeedbackResponseDto>
    suspend fun fetchFollowUsLinks(accessToken: String): NetworkResult<List<FollowUsLinkDto>>
    suspend fun fetchProfileHome(accessToken: String): NetworkResult<ProfileHomeResponse>
    suspend fun fetchProgressGallery(accessToken: String, page: Int = 0, size: Int = 20): NetworkResult<ProgressGalleryPageResponse>
    suspend fun fetchGalleryImageDetails(accessToken: String, imageId: Long): NetworkResult<ProgressGalleryResponseDto>
    suspend fun uploadProgressGalleryImage(
        accessToken: String,
        attachment: SupportAttachment,
        caption: String? = null,
    ): NetworkResult<ProgressGalleryResponseDto>
    suspend fun deleteProgressGalleryImage(accessToken: String, imageId: Long): NetworkResult<Boolean>
}
