package com.muscleplay.app.feature.profile.support

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muscleplay.app.core.network.NetworkResult
import com.muscleplay.app.core.network.device.DeviceInstallationManager
import com.muscleplay.app.core.network.dto.DeviceDiagnosticInfo
import com.muscleplay.app.core.network.dto.EmailFromUserRequestDto
import com.muscleplay.app.core.network.dto.SupportAttachment
import com.muscleplay.app.core.network.dto.SupportIssueCategoryDto
import com.muscleplay.app.core.network.dto.SupportValidationDto
import com.muscleplay.app.feature.profile.support.data.EmailSupportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EmailFormUiState(
    val isLoadingHome: Boolean = true,
    val homeTitle: String = "Email Support",
    val homeSubtitle: String = "Dedicated Ticket Desk 📩",
    val homeDescription: String = "Replies within ~2 business hours. Attach logs or screenshots for faster resolution.",
    val categories: List<SupportIssueCategoryDto> = emptyList(),
    val selectedCategoryKey: String = "",
    val selectedCategoryLabel: String = "",
    val subject: String = "",
    val messageBody: String = "",
    val attachments: List<SupportAttachment> = emptyList(),
    val validation: SupportValidationDto = SupportValidationDto(),
    val isSubmitting: Boolean = false,
    val ticketSentSuccess: Boolean = false,
    val successMessage: String = "",
    val errorMessage: String? = null,
    val userEmail: String = "support@muscleplay.com",
    val deviceModel: String = "Android Device",
    val osVersion: String = "Android 15",
    val appVersion: String = "1.0.0",
    val manufacturer: String = "Android",
    val isDiagnosticExpanded: Boolean = false,
) {
    val totalAttachmentSizeBytes: Long
        get() = attachments.sumOf { it.sizeBytes }

    val isSubjectValid: Boolean
        get() = subject.trim().length in validation.subjectMinLength..validation.subjectMaxLength

    val isMessageValid: Boolean
        get() = messageBody.trim().length in validation.messageMinLength..validation.messageMaxLength

    val isCategoryValid: Boolean
        get() = selectedCategoryKey.isNotBlank()

    val isAttachmentsValid: Boolean
        get() = attachments.size <= validation.maxAttachments && totalAttachmentSizeBytes <= validation.maxTotalAttachmentSizeBytes

    val isFormValid: Boolean
        get() = isSubjectValid && isMessageValid && isCategoryValid && isAttachmentsValid && !isSubmitting
}

class EmailSupportViewModel(
    private val emailSupportRepository: EmailSupportRepository? = null,
    deviceInstallationManager: DeviceInstallationManager = DeviceInstallationManager(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmailFormUiState())
    val uiState: StateFlow<EmailFormUiState> = _uiState.asStateFlow()

    //TODO SEND REAL DEVICE INFO IN THE EMAIL
    init {
        val deviceInfo = deviceInstallationManager.getDeviceInfo()
        _uiState.value = _uiState.value.copy(
            deviceModel = deviceInfo.deviceName,
            appVersion = deviceInfo.appVersion?:"v1.0.0",
            osVersion = "Android OS",
            manufacturer = "Build.MANUFACTURER",
        )
        loadHomeData()
    }

    fun loadHomeData() {
        val repo = emailSupportRepository ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingHome = true, errorMessage = null)
            when (val result = repo.fetchEmailSupportHome()) {
                is NetworkResult.Success -> {
                    val data = result.data
                    val categories = data.categories
                    val defaultCat = categories.firstOrNull()
                    val validation = data.validation ?: SupportValidationDto()

                    _uiState.value = _uiState.value.copy(
                        isLoadingHome = false,
                        homeTitle = data.title ?: "Email Support",
                        homeSubtitle = data.subtitle ?: "Dedicated Ticket Desk 📩",
                        homeDescription = data.description
                            ?: "Replies within ~12 business hours. Attach logs or screenshots for faster resolution.",
                        categories = categories,
                        selectedCategoryKey = defaultCat?.key ?: "ACCOUNT",
                        selectedCategoryLabel = defaultCat?.label ?: defaultCat?.key ?: "Account & Login",
                        subject = defaultCat?.let { "Inquiry regarding ${it.label}" } ?: "Inquiry regarding Support",
                        messageBody = defaultCat?.let { "Hello Support Team,\n\nI need assistance regarding ${it.label}: " }
                            ?: "Hello Support Team,\n\n",
                        validation = validation
                    )
                }

                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingHome = false,
                        errorMessage = "Unable to load support configuration. Please try again."
                    )
                }

                is NetworkResult.Exception -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingHome = false,
                        errorMessage = "Unable to load support configuration. Please try again."
                    )
                }
            }
        }
    }

    fun selectCategory(categoryKey: String) {
        val cat = _uiState.value.categories.firstOrNull { it.key == categoryKey }
        val label = cat?.label ?: categoryKey
        val currentSubject = _uiState.value.subject
        val newSubject = if (currentSubject.isBlank() || currentSubject.startsWith("Inquiry regarding")) {
            "Inquiry regarding $label"
        } else currentSubject

        _uiState.value = _uiState.value.copy(
            selectedCategoryKey = categoryKey,
            selectedCategoryLabel = label,
            subject = newSubject
        )
    }

    fun updateSubject(subject: String) {
        val maxLen = _uiState.value.validation.subjectMaxLength
        val clamped = if (subject.length > maxLen) subject.take(maxLen) else subject
        _uiState.value = _uiState.value.copy(subject = clamped, errorMessage = null)
    }

    fun updateMessageBody(body: String) {
        val maxLen = _uiState.value.validation.messageMaxLength
        val clamped = if (body.length > maxLen) body.take(maxLen) else body
        _uiState.value = _uiState.value.copy(messageBody = clamped, errorMessage = null)
    }

    fun addAttachments(newAttachments: List<SupportAttachment>) {
        val current = _uiState.value.attachments
        val maxAllowed = _uiState.value.validation.maxAttachments
        val maxSingleSize = _uiState.value.validation.maxAttachmentSizeBytes
        val maxTotalSize = _uiState.value.validation.maxTotalAttachmentSizeBytes
        val allowedTypes = _uiState.value.validation.allowedMimeTypes

        val validNew = newAttachments.filter { att ->
            val isSizeOk = att.sizeBytes <= maxSingleSize
            val isMimeOk = allowedTypes.isEmpty() || allowedTypes.any { type ->
                type.equals(att.mimeType, ignoreCase = true) || type.contains("*")
            }
            isSizeOk && isMimeOk
        }

        val combined = (current + validNew).take(maxAllowed)
        if (combined.sumOf { it.sizeBytes } > maxTotalSize) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Total attachments size exceeds limit (${maxTotalSize / (1024 * 1024)}MB)."
            )
            return
        }

        _uiState.value = _uiState.value.copy(attachments = combined, errorMessage = null)
    }

    fun removeAttachment(index: Int) {
        val updated = _uiState.value.attachments.toMutableList().apply {
            if (index in indices) removeAt(index)
        }
        _uiState.value = _uiState.value.copy(attachments = updated, errorMessage = null)
    }

    fun toggleDiagnosticExpanded() {
        _uiState.value = _uiState.value.copy(isDiagnosticExpanded = !_uiState.value.isDiagnosticExpanded)
    }

    fun submitTicket() {
        val state = _uiState.value
        if (!state.isFormValid || state.isSubmitting) return

        val repo = emailSupportRepository
        if (repo == null) {
            _uiState.value = state.copy(
                ticketSentSuccess = true,
                successMessage = "Your support request has been submitted successfully."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, errorMessage = null)

            val diagnostics = DeviceDiagnosticInfo(
                appVersion = state.appVersion,
                osVersion = state.osVersion,
                deviceModel = state.deviceModel,
                manufacturer = state.manufacturer,
                isBatteryOptimizationIgnored = true,
                isGoogleFitLinked = false,
                deviceType = "ANDROID"
            )

            val requestDto = EmailFromUserRequestDto(
                category = state.selectedCategoryKey,
                subject = state.subject.trim(),
                message = state.messageBody.trim(),
                diagnostics = diagnostics
            )

            when (val result = repo.sendSupportEmail(requestDto, state.attachments)) {
                is NetworkResult.Success -> {
                    val message = result.data.message ?: "Your support request has been submitted successfully."
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        ticketSentSuccess = true,
                        successMessage = message,
                        subject = "",
                        messageBody = "",
                        attachments = emptyList()
                    )
                }

                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        errorMessage = "Unable to submit your support request right now. Please check your internet connection and try again."
                    )
                }

                is NetworkResult.Exception -> {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        errorMessage = "Unable to submit your support request right now. Please check your internet connection and try again."
                    )
                }
            }
        }
    }

    fun dismissSuccessDialog() {
        _uiState.value = _uiState.value.copy(ticketSentSuccess = false)
    }

    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
