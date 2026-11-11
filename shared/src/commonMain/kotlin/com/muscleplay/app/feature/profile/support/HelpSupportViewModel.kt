package com.muscleplay.app.feature.profile.support

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muscleplay.app.core.network.NetworkResult
import com.muscleplay.app.core.network.dto.SupportFaqDto
import com.muscleplay.app.core.network.dto.SupportHomeResponseDto
import com.muscleplay.app.feature.profile.support.data.SupportRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FaqItem(
    val id: Int,
    val category: String,
    val question: String,
    val answer: String,
    val actionText: String? = null,
    val policyTarget: String? = null,
)

val defaultFaqs = listOf(
    FaqItem(
        id = 1,
        category = "AI Tracking",
        question = "How does AI pose tracking work?",
        answer = "MusclePlay uses real-time on-device camera pose detection (CameraX on Android / AVFoundation on iOS) to calculate joint angles for rep counting. No video data ever leaves your device.",
        actionText = "Learn about Privacy & AI Rules",
        policyTarget = "Fair Play & Anti-Cheat Rules"
    ),
    FaqItem(
        id = 2,
        category = "Privacy",
        question = "Why is camera permission required?",
        answer = "Camera permissions are strictly used locally on your device for real-time rep counting and form evaluation. MusclePlay does not store or upload video recordings to any cloud server.",
        actionText = "View Privacy Policy",
        policyTarget = "Privacy Policy"
    ),
    FaqItem(
        id = 3,
        category = "MP Coins",
        question = "How do I cash out my MP Coins?",
        answer = "You can redeem MP Coins once your balance reaches the 1,000 MP threshold. Select your preferred payment channel (UPI, Bank Transfer, PayPal, or Stripe) and authorize cashout using Biometrics.",
        actionText = "View MP Coins & Cashout Policy",
        policyTarget = "MP Coins & Cashout Policy"
    ),
    FaqItem(
        id = 4,
        category = "Account",
        question = "How do I frozen streaks or freeze protection?",
        answer = "Streaks update daily upon completing workouts. Pro members receive 2 free monthly Streak Freezes to protect their progress during rest or travel days."
    ),
    FaqItem(
        id = 5,
        category = "Security",
        question = "Are my progress photos and data secure?",
        answer = "Yes! All progress gallery photos are encrypted and stored locally in app-private storage. Account credentials are state-of-the-art encrypted via Credential Manager.",
        actionText = "Read Terms of Service",
        policyTarget = "Terms of Service"
    )
)

sealed interface SupportHomeUiState {
    data object Loading : SupportHomeUiState
    data class Success(val homeData: SupportHomeResponseDto) : SupportHomeUiState
    data class Error(val message: String) : SupportHomeUiState
}

sealed interface PolicyDialogUiState {
    data object Idle : PolicyDialogUiState
    data class Loading(val title: String) : PolicyDialogUiState
    data class Success(val title: String, val content: String) : PolicyDialogUiState
    data class Error(val title: String, val message: String) : PolicyDialogUiState
}

sealed interface FaqSearchUiState {
    data object Idle : FaqSearchUiState
    data object Loading : FaqSearchUiState
    data class Success(val faqs: List<SupportFaqDto>, val isLast: Boolean) : FaqSearchUiState
    data class Error(val message: String) : FaqSearchUiState
}

data class HelpSupportUiState(
    val searchQuery: String = "",
    val expandedFaqId: Long? = null,
    val selectedCategoryKey: String? = null,
    val faqHelpfulVotes: Map<Long, Boolean?> = emptyMap(),
    val homeState: SupportHomeUiState = SupportHomeUiState.Loading,
    val dialogState: PolicyDialogUiState = PolicyDialogUiState.Idle,
    val searchFaqState: FaqSearchUiState = FaqSearchUiState.Idle,
)

class HelpSupportViewModel(
    private val supportRepository: SupportRepository? = null,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HelpSupportUiState())
    val uiState: StateFlow<HelpSupportUiState> = _uiState.asStateFlow()

    private var searchDebounceJob: Job? = null

    init {
        loadSupportHome()
    }

    fun loadSupportHome() {
        val repo = supportRepository ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(homeState = SupportHomeUiState.Loading)
            when (val result = repo.fetchSupportHome()) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        homeState = SupportHomeUiState.Success(result.data)
                    )
                }

                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        homeState = SupportHomeUiState.Error(result.message)
                    )
                }

                is NetworkResult.Exception -> {
                    _uiState.value = _uiState.value.copy(
                        homeState = SupportHomeUiState.Error(
                            result.throwable.message ?: "Unable to load support home. Please try again."
                        )
                    )
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        searchDebounceJob?.cancel()

        if (query.isBlank() && _uiState.value.selectedCategoryKey == null) {
            _uiState.value = _uiState.value.copy(searchFaqState = FaqSearchUiState.Idle)
            return
        }

        searchDebounceJob = viewModelScope.launch {
            delay(350L)
            performFaqSearch(categoryKey = _uiState.value.selectedCategoryKey, query = query.trim())
        }
    }

    fun selectCategory(categoryKey: String?) {
        val newKey = if (_uiState.value.selectedCategoryKey == categoryKey) null else categoryKey
        _uiState.value = _uiState.value.copy(selectedCategoryKey = newKey)
        performFaqSearch(categoryKey = newKey, query = _uiState.value.searchQuery.trim())
    }

    fun performFaqSearch(
        categoryKey: String? = _uiState.value.selectedCategoryKey,
        query: String? = _uiState.value.searchQuery.trim(),
        page: Int = 0,
    ) {
        val repo = supportRepository ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(searchFaqState = FaqSearchUiState.Loading)
            when (val result = repo.searchFaqs(categoryKey = categoryKey, query = query, page = page)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        searchFaqState = FaqSearchUiState.Success(
                            faqs = result.data.content,
                            isLast = result.data.last
                        )
                    )
                }

                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        searchFaqState = FaqSearchUiState.Error(result.message)
                    )
                }

                is NetworkResult.Exception -> {
                    _uiState.value = _uiState.value.copy(
                        searchFaqState = FaqSearchUiState.Error(
                            result.throwable.message ?: "Unable to search FAQs."
                        )
                    )
                }
            }
        }
    }

    fun openPolicyDialog(title: String) {
        val repo = supportRepository
        if (repo == null) {
            _uiState.value = _uiState.value.copy(
                dialogState = PolicyDialogUiState.Success(
                    title = title,
                    content = getFallbackPolicyText(title)
                )
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(dialogState = PolicyDialogUiState.Loading(title))
            val result = when (title) {
                "Terms of Service", "Terms & Conditions" -> repo.fetchTermsConditions()
                "Privacy Policy" -> repo.fetchPrivacyPolicy()
                "About Us" -> repo.fetchAboutInfo()
                else -> repo.fetchAboutInfo()
            }

            when (result) {
                is NetworkResult.Success -> {
                    val contentText = result.data.content?.takeIf { it.isNotBlank() }
                        ?: getFallbackPolicyText(title)
                    _uiState.value = _uiState.value.copy(
                        dialogState = PolicyDialogUiState.Success(title, contentText)
                    )
                }

                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        dialogState = PolicyDialogUiState.Error(title, result.message)
                    )
                }

                is NetworkResult.Exception -> {
                    _uiState.value = _uiState.value.copy(
                        dialogState = PolicyDialogUiState.Error(
                            title,
                            result.throwable.message ?: "Unable to load document."
                        )
                    )
                }
            }
        }
    }

    fun closePolicyDialog() {
        _uiState.value = _uiState.value.copy(dialogState = PolicyDialogUiState.Idle)
    }

    fun toggleFaq(id: Long) {
        val current = _uiState.value.expandedFaqId
        _uiState.value = _uiState.value.copy(
            expandedFaqId = if (current == id) null else id
        )
    }

    fun voteHelpful(faqId: Long, isHelpful: Boolean) {
        val currentVotes = _uiState.value.faqHelpfulVotes.toMutableMap()
        currentVotes[faqId] = isHelpful
        _uiState.value = _uiState.value.copy(faqHelpfulVotes = currentVotes)
    }

    private fun getFallbackPolicyText(title: String): String {
        return when (title) {
            "MP Coins & Cashout Policy" -> "1. Minimum Redemption Threshold: Users can cash out once balance reaches 1,000 MP Coins (1,000 MP = ₹10.00).\n2. Payout Velocity: Maximum 1 redemption request per 24 hours.\n3. Processing Time: Payouts via UPI, Bank, PayPal, or Stripe require 1-2 business days.\n4. Anti-Fraud Audit: Coins generated via fake referral self-invites, emulators, or manipulated pose metrics are subject to freezing during compliance audit."
            "Fair Play & Anti-Cheat Rules" -> "1. Real-Time Rep Verification: MusclePlay utilizes on-device pose detection algorithms. Artificial video feeds or static image spoofing will invalidate workout records.\n2. Referral Integrity: Self-referrals or device ID spoofing will result in referral reward revocation and coin forfeiture."
            "Privacy Policy" -> "1. Camera Data: Camera access is strictly processed locally on your device hardware for pose tracking. No raw video feed is recorded or stored.\n2. Personal Data: Credentials and profile metrics are stored in app-private storage and encrypted in transit."
            "About Us" -> "MusclePlay is a cutting-edge fitness application utilizing artificial intelligence and multiplatform KMP technologies to make workouts engaging, rewarding, and accessible everywhere."
            else -> "MusclePlay $title outlines standard user responsibilities, terms of service, acceptable fitness app usage, and customer support standards."
        }
    }
}
