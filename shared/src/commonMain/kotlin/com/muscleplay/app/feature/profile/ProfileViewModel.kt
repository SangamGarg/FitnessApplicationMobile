package com.muscleplay.app.feature.profile

import com.muscleplay.app.core.network.NetworkResult
import com.muscleplay.app.core.network.dto.FollowUsLinkDto
import com.muscleplay.app.feature.profile.data.ProfileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main + Job())
) {
    private val _feedbackState = MutableStateFlow<FeedbackSubmitState>(FeedbackSubmitState.Idle)
    val feedbackState: StateFlow<FeedbackSubmitState> = _feedbackState.asStateFlow()

    private val _followUsState = MutableStateFlow<FollowUsUiState>(FollowUsUiState.Loading)
    val followUsState: StateFlow<FollowUsUiState> = _followUsState.asStateFlow()

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
                    _followUsState.value = FollowUsUiState.Error(result.message)
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
