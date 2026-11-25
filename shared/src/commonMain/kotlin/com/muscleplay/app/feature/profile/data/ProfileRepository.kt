package com.muscleplay.app.feature.profile.data

import com.muscleplay.app.core.network.ApiClient
import com.muscleplay.app.core.network.NetworkResult
import com.muscleplay.app.core.network.auth.TokenStorageManager
import com.muscleplay.app.core.network.dto.FeedbackRequestDto
import com.muscleplay.app.core.network.dto.FeedbackResponseDto
import com.muscleplay.app.core.network.dto.FollowUsLinkDto
import com.muscleplay.app.core.network.dto.ProfileHomeResponse
import com.muscleplay.app.core.network.dto.ProgressGalleryPageResponse
import com.muscleplay.app.core.network.dto.ProgressGalleryResponseDto
import com.muscleplay.app.core.network.dto.SupportAttachment

interface ProfileRepository {
    suspend fun submitFeedback(feedback: String): NetworkResult<FeedbackResponseDto>
    suspend fun fetchFollowUsLinks(): NetworkResult<List<FollowUsLinkDto>>
    suspend fun fetchProfileHome(forceRefresh: Boolean = false): NetworkResult<ProfileHomeResponse>
    suspend fun fetchProgressGallery(page: Int = 0, size: Int = 20): NetworkResult<ProgressGalleryPageResponse>
    suspend fun fetchGalleryImageDetails(imageId: Long): NetworkResult<ProgressGalleryResponseDto>
    suspend fun uploadProgressGalleryImage(
        attachment: SupportAttachment,
        caption: String? = null,
    ): NetworkResult<ProgressGalleryResponseDto>
    suspend fun deleteProgressGalleryImage(imageId: Long): NetworkResult<Boolean>
    fun clearCache()
}

class ProfileRepositoryImpl(
    private val apiClient: ApiClient,
    private val tokenStorageManager: TokenStorageManager,
) : ProfileRepository {

    private var cachedProfileHome: ProfileHomeResponse? = null

    override suspend fun submitFeedback(feedback: String): NetworkResult<FeedbackResponseDto> {
        val accessToken = tokenStorageManager.accessToken ?: ""
        return apiClient.submitFeedback(accessToken, FeedbackRequestDto(feedback = feedback))
    }

    override suspend fun fetchFollowUsLinks(): NetworkResult<List<FollowUsLinkDto>> {
        val accessToken = tokenStorageManager.accessToken ?: ""
        return apiClient.fetchFollowUsLinks(accessToken)
    }

    override suspend fun fetchProfileHome(forceRefresh: Boolean): NetworkResult<ProfileHomeResponse> {
        if (!forceRefresh && cachedProfileHome != null) {
            return NetworkResult.Success(cachedProfileHome!!)
        }
        val accessToken = tokenStorageManager.accessToken ?: ""
        return when (val result = apiClient.fetchProfileHome(accessToken)) {
            is NetworkResult.Success -> {
                cachedProfileHome = result.data
                result
            }

            else -> result
        }
    }

    override suspend fun fetchProgressGallery(page: Int, size: Int): NetworkResult<ProgressGalleryPageResponse> {
        val accessToken = tokenStorageManager.accessToken ?: ""
        return apiClient.fetchProgressGallery(accessToken, page, size)
    }

    override suspend fun fetchGalleryImageDetails(imageId: Long): NetworkResult<ProgressGalleryResponseDto> {
        val accessToken = tokenStorageManager.accessToken ?: ""
        return apiClient.fetchGalleryImageDetails(accessToken, imageId)
    }

    override suspend fun uploadProgressGalleryImage(
        attachment: SupportAttachment,
        caption: String?,
    ): NetworkResult<ProgressGalleryResponseDto> {
        val accessToken = tokenStorageManager.accessToken ?: ""
        return when (val result = apiClient.uploadProgressGalleryImage(accessToken, attachment, caption)) {
            is NetworkResult.Success -> {
                cachedProfileHome?.let { home ->
                    val updatedGallery = listOf(result.data) + home.progressGallery
                    cachedProfileHome = home.copy(progressGallery = updatedGallery)
                }
                result
            }

            else -> result
        }
    }

    override suspend fun deleteProgressGalleryImage(imageId: Long): NetworkResult<Boolean> {
        val accessToken = tokenStorageManager.accessToken ?: ""
        return when (val result = apiClient.deleteProgressGalleryImage(accessToken, imageId)) {
            is NetworkResult.Success -> {
                cachedProfileHome?.let { home ->
                    val updatedGallery = home.progressGallery.filter { it.id != imageId }
                    cachedProfileHome = home.copy(progressGallery = updatedGallery)
                }
                result
            }

            else -> result
        }
    }

    override fun clearCache() {
        cachedProfileHome = null
    }
}
