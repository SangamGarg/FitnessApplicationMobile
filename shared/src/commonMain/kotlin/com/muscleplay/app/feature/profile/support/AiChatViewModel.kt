package com.muscleplay.app.feature.profile.support

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class MessageSender {
    USER, BOT
}

data class ChatMessage(
    val id: String,
    val sender: MessageSender,
    val text: String,
    val timestamp: String,
    val actionText: String? = null,
    val actionRoute: String? = null
)

val defaultQuickPromptChips = listOf(
    "How do I cash out MP Coins?",
    "AI rep counter not working",
    "How to change my diet plan?",
    "What is the minimum withdrawal limit?"
)

data class AiChatUiState(
    val messages: List<ChatMessage> = initialChatMessages,
    val inputText: String = "",
    val isTyping: Boolean = false,
    val suggestionChips: List<String> = defaultQuickPromptChips,
    val attachmentUri: String? = null
)

val initialChatMessages = listOf(
    ChatMessage(
        id = "msg_1",
        sender = MessageSender.BOT,
        text = "Hello! 👋 I'm your MusclePlay AI Coach & Support Bot. Ask me anything about workouts, AI pose tracking, diet plans, or MP Coins cashout!",
        timestamp = "Just Now"
    )
)

class AiChatViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AiChatUiState())
    val uiState: StateFlow<AiChatUiState> = _uiState.asStateFlow()

    fun updateInputText(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun attachScreenshot(uri: String?) {
        _uiState.value = _uiState.value.copy(attachmentUri = uri)
    }

    fun sendMessage(userText: String = _uiState.value.inputText) {
        val trimmed = userText.trim()
        if (trimmed.isEmpty()) return

        val currentState = _uiState.value
        val userMsg = ChatMessage(
            id = "user_${(1000..9999).random()}",
            sender = MessageSender.USER,
            text = trimmed,
            timestamp = "Just Now"
        )

        val updatedList = currentState.messages + userMsg
        _uiState.value = currentState.copy(
            messages = updatedList,
            inputText = "",
            isTyping = true
        )

        // Generate automated AI response based on query keywords
        generateBotReply(trimmed, updatedList)
    }

    private fun generateBotReply(userQuery: String, currentMessages: List<ChatMessage>) {
        val queryLower = userQuery.lowercase()

        val (replyText, actionText, actionRoute) = when {
            "cash out" in queryLower || "withdraw" in queryLower || "coin" in queryLower -> Triple(
                "You can cash out your MP Coins in the MP Coins Hub! Minimum redemption threshold is 1,000 MP Coins (₹10.00). We support UPI, Bank Transfer, PayPal, and Stripe Connect.",
                "Go to MP Coins Hub",
                "profile_coin_redemption"
            )
            "rep" in queryLower || "tracking" in queryLower || "camera" in queryLower || "pose" in queryLower -> Triple(
                "For accurate AI Pose Tracking, ensure good lighting and position your phone 6-8 feet away so your full body is visible in the frame. If you still experience issues, check Camera Diagnostics.",
                "Run Camera Diagnostics",
                "profile_diagnostics"
            )
            "diet" in queryLower || "calorie" in queryLower || "plan" in queryLower -> Triple(
                "You can scan meal ingredients with our AI Camera Scanner or adjust your daily target calories in the Diet tab.",
                "Open Diet Tracker",
                "diet_home"
            )
            else -> Triple(
                "I've logged your request! If you need personal staff assistance, you can also file a detailed Email Support ticket.",
                "File Email Ticket",
                "profile_email_support"
            )
        }

        val botMsg = ChatMessage(
            id = "bot_${(1000..9999).random()}",
            sender = MessageSender.BOT,
            text = replyText,
            timestamp = "Just Now",
            actionText = actionText,
            actionRoute = actionRoute
        )

        _uiState.value = _uiState.value.copy(
            messages = currentMessages + botMsg,
            isTyping = false
        )
    }

    fun clearChat() {
        _uiState.value = _uiState.value.copy(
            messages = initialChatMessages,
            isTyping = false,
            inputText = ""
        )
    }
}
