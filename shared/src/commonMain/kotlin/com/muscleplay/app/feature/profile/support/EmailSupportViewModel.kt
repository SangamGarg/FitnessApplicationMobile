package com.muscleplay.app.feature.profile.support

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

val supportIssueCategories = listOf(
    "Billing & MP Coins",
    "AI Pose Tracking Issues",
    "Workout Logging Bug",
    "Account & Security",
    "Other General Support"
)

data class EmailFormUiState(
    val selectedCategory: String = supportIssueCategories[0],
    val subject: String = "Inquiry regarding ${supportIssueCategories[0]}",
    val messageBody: String = "Hello Support Team,\n\nI need assistance regarding ",
    val attachedFiles: List<String> = emptyList(),
    val isDiagnosticExpanded: Boolean = false,
    val isLoading: Boolean = false,
    val ticketSentSuccess: Boolean = false,
    val userEmail: String = "support@muscleplay.com",
    val deviceModel: String = "Android Device (KMP Cross-Platform)",
    val osVersion: String = "Android 14 (API 34)",
    val appVersion: String = "1.0.0 (Build 104)",
    val userId: String = "USR-98214-MP"
) {
    val isFormValid: Boolean
        get() = subject.isNotBlank() && messageBody.isNotBlank()
}

class EmailSupportViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(EmailFormUiState())
    val uiState: StateFlow<EmailFormUiState> = _uiState.asStateFlow()

    fun selectCategory(category: String) {
        val current = _uiState.value
        val newSubject = if (current.subject.startsWith("Inquiry regarding")) "Inquiry regarding $category" else current.subject
        _uiState.value = current.copy(
            selectedCategory = category,
            subject = newSubject
        )
    }

    fun updateSubject(subject: String) {
        _uiState.value = _uiState.value.copy(subject = subject)
    }

    fun updateMessageBody(body: String) {
        _uiState.value = _uiState.value.copy(messageBody = body)
    }

    fun addAttachment(fileUri: String) {
        val updated = _uiState.value.attachedFiles + fileUri
        _uiState.value = _uiState.value.copy(attachedFiles = updated)
    }

    fun removeAttachment(index: Int) {
        val updated = _uiState.value.attachedFiles.toMutableList().apply {
            if (index in indices) removeAt(index)
        }
        _uiState.value = _uiState.value.copy(attachedFiles = updated)
    }

    fun toggleDiagnosticExpanded() {
        _uiState.value = _uiState.value.copy(isDiagnosticExpanded = !_uiState.value.isDiagnosticExpanded)
    }

    fun submitTicket(onSuccess: () -> Unit = {}) {
        if (!_uiState.value.isFormValid) return
        _uiState.value = _uiState.value.copy(isLoading = true)

        _uiState.value = _uiState.value.copy(
            isLoading = false,
            ticketSentSuccess = true
        )
        onSuccess()
    }

    fun dismissSuccessDialog() {
        _uiState.value = _uiState.value.copy(ticketSentSuccess = false)
    }
}
