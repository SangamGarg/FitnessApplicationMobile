package com.muscleplay.app.feature.profile

import com.muscleplay.app.core.network.NetworkResult
import com.muscleplay.app.core.network.dto.FollowUsLinkDto
import com.muscleplay.app.core.network.dto.ProfileHomeResponse
import com.muscleplay.app.core.network.dto.ProgressGalleryResponseDto
import com.muscleplay.app.core.network.dto.SupportAttachment
import com.muscleplay.app.feature.profile.data.ProfileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ProfileHomeUiState {
    data object Loading : ProfileHomeUiState
    data class Success(val profileHome: ProfileHomeResponse) : ProfileHomeUiState
    data class Error(val message: String) : ProfileHomeUiState
}

sealed interface GalleryUiState {
    data object Loading : GalleryUiState
    data class Success(
        val items: List<ProgressGalleryResponseDto>,
        val currentPage: Int,
        val totalPages: Int,
        val hasMorePages: Boolean,
        val isLoadingMore: Boolean = false,
    ) : GalleryUiState
    data class Error(val message: String) : GalleryUiState
    data object Empty : GalleryUiState
}

sealed interface GalleryUploadState {
    data object Idle : GalleryUploadState
    data object Uploading : GalleryUploadState
    data class Success(val uploadedItem: ProgressGalleryResponseDto) : GalleryUploadState
    data class Error(val message: String) : GalleryUploadState
}

sealed interface GalleryDeleteState {
    data object Idle : GalleryDeleteState
    data object Deleting : GalleryDeleteState
    data class Success(val imageId: Long) : GalleryDeleteState
    data class Error(val message: String) : GalleryDeleteState
}

sealed interface FeedbackSubmitState {
    data object Idle : FeedbackSubmitState
    data object Submitting : FeedbackSubmitState
    data class Success(val message: String) : FeedbackSubmitState
    data class Error(val message: String) : FeedbackSubmitState
}

sealed interface FollowUsUiState {
    data object Loading : FollowUsUiState
    data class Success(val links: List<FollowUsLinkDto>) : FollowUsUiState
    data class Error(val message: String) : FollowUsUiState
    data object Empty : FollowUsUiState
}

class ProfileViewModel(
    private val repository: ProfileRepository,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main + Job()),
) {
    private val _profileHomeState = MutableStateFlow<ProfileHomeUiState>(ProfileHomeUiState.Loading)
    val profileHomeState: StateFlow<ProfileHomeUiState> = _profileHomeState.asStateFlow()

    private val _galleryState = MutableStateFlow<GalleryUiState>(GalleryUiState.Loading)
    val galleryState: StateFlow<GalleryUiState> = _galleryState.asStateFlow()

    private val _uploadState = MutableStateFlow<GalleryUploadState>(GalleryUploadState.Idle)
    val uploadState: StateFlow<GalleryUploadState> = _uploadState.asStateFlow()

    private val _deleteState = MutableStateFlow<GalleryDeleteState>(GalleryDeleteState.Idle)
    val deleteState: StateFlow<GalleryDeleteState> = _deleteState.asStateFlow()

    private val _feedbackState = MutableStateFlow<FeedbackSubmitState>(FeedbackSubmitState.Idle)
    val feedbackState: StateFlow<FeedbackSubmitState> = _feedbackState.asStateFlow()

    private val _followUsState = MutableStateFlow<FollowUsUiState>(FollowUsUiState.Loading)
    val followUsState: StateFlow<FollowUsUiState> = _followUsState.asStateFlow()

    init {
        loadProfileHome()
    }

    fun loadProfileHome(forceRefresh: Boolean = false) {
        if (!forceRefresh && _profileHomeState.value is ProfileHomeUiState.Success) {
            return
        }

        coroutineScope.launch {
            _profileHomeState.value = ProfileHomeUiState.Loading
            when (val result = repository.fetchProfileHome(forceRefresh)) {
                is NetworkResult.Success -> {
                    _profileHomeState.value = ProfileHomeUiState.Success(result.data)
                }

                is NetworkResult.Error -> {
                    _profileHomeState.value = ProfileHomeUiState.Error("Unable to load profile. Please try again.")
                }

                is NetworkResult.Exception -> {
                    _profileHomeState.value = ProfileHomeUiState.Error("Unable to load profile. Please try again.")
                }
            }
        }
    }

    fun loadProgressGallery(forceRefresh: Boolean = false) {
        val current = _galleryState.value
        if (!forceRefresh && current is GalleryUiState.Success && current.items.isNotEmpty()) {
            return
        }

        coroutineScope.launch {
            _galleryState.value = GalleryUiState.Loading
            when (val result = repository.fetchProgressGallery(page = 0, size = 20)) {
                is NetworkResult.Success -> {
                    val pageData = result.data
                    val items = pageData.content
                    val totalPages = pageData.page.totalPages
                    val currentPage = pageData.page.number
                    val hasMore = currentPage < totalPages - 1

                    if (items.isEmpty()) {
                        _galleryState.value = GalleryUiState.Empty
                    } else {
                        _galleryState.value = GalleryUiState.Success(
                            items = items,
                            currentPage = currentPage,
                            totalPages = totalPages,
                            hasMorePages = hasMore,
                            isLoadingMore = false
                        )
                    }
                }

                is NetworkResult.Error -> {
                    _galleryState.value = GalleryUiState.Error("Unable to load gallery. Please try again.")
                }

                is NetworkResult.Exception -> {
                    _galleryState.value = GalleryUiState.Error("Unable to load gallery. Please try again.")
                }
            }
        }
    }

    fun loadNextGalleryPage() {
        val current = _galleryState.value as? GalleryUiState.Success ?: return
        if (!current.hasMorePages || current.isLoadingMore) return

        coroutineScope.launch {
            _galleryState.value = current.copy(isLoadingMore = true)
            val nextPage = current.currentPage + 1
            when (val result = repository.fetchProgressGallery(page = nextPage, size = 20)) {
                is NetworkResult.Success -> {
                    val pageData = result.data
                    val newItems = (current.items + pageData.content).distinctBy { it.id }
                    val totalPages = pageData.page.totalPages
                    val currentPage = pageData.page.number
                    val hasMore = currentPage < totalPages - 1

                    _galleryState.value = GalleryUiState.Success(
                        items = newItems,
                        currentPage = currentPage,
                        totalPages = totalPages,
                        hasMorePages = hasMore,
                        isLoadingMore = false
                    )
                }

                else -> {
                    _galleryState.value = current.copy(isLoadingMore = false)
                }
            }
        }
    }

    fun uploadGalleryImage(attachment: SupportAttachment, caption: String? = null) {
        if (_uploadState.value is GalleryUploadState.Uploading) return

        coroutineScope.launch {
            _uploadState.value = GalleryUploadState.Uploading
            when (val result = repository.uploadProgressGalleryImage(attachment, caption)) {
                is NetworkResult.Success -> {
                    _uploadState.value = GalleryUploadState.Success(result.data)
                    // Refresh profile home cache to update 5-item preview
                    loadProfileHome(forceRefresh = true)
                }

                is NetworkResult.Error -> {
                    _uploadState.value = GalleryUploadState.Error("Unable to upload image. Please try again.")
                }

                is NetworkResult.Exception -> {
                    _uploadState.value = GalleryUploadState.Error("Unable to upload image. Please try again.")
                }
            }
        }
    }

    fun resetUploadState() {
        _uploadState.value = GalleryUploadState.Idle
    }

    fun deleteGalleryImage(imageId: Long) {
        if (_deleteState.value is GalleryDeleteState.Deleting) return

        coroutineScope.launch {
            _deleteState.value = GalleryDeleteState.Deleting
            when (val result = repository.deleteProgressGalleryImage(imageId)) {
                is NetworkResult.Success -> {
                    _deleteState.value = GalleryDeleteState.Success(imageId)
                    // Update current gallery state
                    (galleryState.value as? GalleryUiState.Success)?.let { state ->
                        val updatedList = state.items.filter { it.id != imageId }
                        _galleryState.value = if (updatedList.isEmpty()) {
                            GalleryUiState.Empty
                        } else {
                            state.copy(items = updatedList)
                        }
                    }
                    // Refresh profile home cache to update 5-item preview
                    loadProfileHome(forceRefresh = true)
                }

                is NetworkResult.Error -> {
                    _deleteState.value = GalleryDeleteState.Error("Unable to delete image. Please try again.")
                }

                is NetworkResult.Exception -> {
                    _deleteState.value = GalleryDeleteState.Error("Unable to delete image. Please try again.")
                }
            }
        }
    }

    fun resetDeleteState() {
        _deleteState.value = GalleryDeleteState.Idle
    }

    fun submitFeedback(text: String) {
        if (text.isBlank() || _feedbackState.value is FeedbackSubmitState.Submitting) return

        coroutineScope.launch {
            _feedbackState.value = FeedbackSubmitState.Submitting
            when (val result = repository.submitFeedback(text.trim())) {
                is NetworkResult.Success -> {
                    val msg = result.data.message ?: "Thank you for your feedback!"
                    _feedbackState.value = FeedbackSubmitState.Success(msg)
                }

                is NetworkResult.Error -> {
                    _feedbackState.value = FeedbackSubmitState.Error(result.message)
                }

                is NetworkResult.Exception -> {
                    _feedbackState.value = FeedbackSubmitState.Error(
                        result.throwable.message ?: "Network error. Please try again."
                    )
                }
            }
        }
    }

    fun resetFeedbackState() {
        _feedbackState.value = FeedbackSubmitState.Idle
    }

    fun loadFollowUsLinks(forceRefresh: Boolean = false) {
        val currentState = _followUsState.value
        if (!forceRefresh && currentState is FollowUsUiState.Success && currentState.links.isNotEmpty()) {
            return
        }

        coroutineScope.launch {
            _followUsState.value = FollowUsUiState.Loading
            when (val result = repository.fetchFollowUsLinks()) {
                is NetworkResult.Success -> {
                    val links = result.data
                    if (links.isEmpty()) {
                        _followUsState.value = FollowUsUiState.Empty
                    } else {
                        _followUsState.value = FollowUsUiState.Success(links)
                    }
                }

                is NetworkResult.Error -> {
                    _followUsState.value =
                        FollowUsUiState.Error("Unable to load links. Please try again.")
                }

                is NetworkResult.Exception -> {
                    _followUsState.value = FollowUsUiState.Error(
                        result.throwable.message ?: "Unable to load links. Please try again."
                    )
                }
            }
        }
    }
}
