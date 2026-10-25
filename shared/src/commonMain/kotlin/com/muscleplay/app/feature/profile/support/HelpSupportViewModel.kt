package com.muscleplay.app.feature.profile.support

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class FaqItem(
    val id: Int,
    val category: String,
    val question: String,
    val answer: String,
    val actionText: String? = null,
    val policyTarget: String? = null
)

data class HelpSupportUiState(
    val searchQuery: String = "",
    val expandedFaqId: Int? = null,
    val faqHelpfulVotes: Map<Int, Boolean?> = emptyMap(),
    val selectedPolicyTitle: String? = null,
    val faqs: List<FaqItem> = defaultFaqs
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

class HelpSupportViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HelpSupportUiState())
    val uiState: StateFlow<HelpSupportUiState> = _uiState.asStateFlow()

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun toggleFaq(id: Int) {
        val current = _uiState.value.expandedFaqId
        _uiState.value = _uiState.value.copy(
            expandedFaqId = if (current == id) null else id
        )
    }

    fun voteHelpful(faqId: Int, isHelpful: Boolean) {
        val currentVotes = _uiState.value.faqHelpfulVotes.toMutableMap()
        currentVotes[faqId] = isHelpful
        _uiState.value = _uiState.value.copy(faqHelpfulVotes = currentVotes)
    }

    fun openPolicy(title: String) {
        _uiState.value = _uiState.value.copy(selectedPolicyTitle = title)
    }

    fun closePolicy() {
        _uiState.value = _uiState.value.copy(selectedPolicyTitle = null)
    }
}
