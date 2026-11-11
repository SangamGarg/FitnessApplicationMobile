package com.muscleplay.app.feature.profile.support

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muscleplay.app.core.network.NetworkResult
import com.muscleplay.app.core.network.device.DeviceInstallationManager
import com.muscleplay.app.core.network.dto.CreateSupportTicketRequestDTO
import com.muscleplay.app.core.network.dto.DeviceDiagnosticInfo
import com.muscleplay.app.core.network.dto.SupportAttachment
import com.muscleplay.app.core.network.dto.SupportIssueCategoryDto
import com.muscleplay.app.core.network.dto.SupportValidationDto
import com.muscleplay.app.feature.profile.support.data.TicketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RaiseTicketUiState(
    val isLoadingHome: Boolean = true,
    val homeTitle: String = "Raise Support Ticket",
    val homeSubtitle: String = "Describe Your Issue",
    val homeDescription: String = "Select a category and attach media so our team can resolve your issue quickly.",
    val categories: List<SupportIssueCategoryDto> = emptyList(),
    val selectedCategoryKey: String = "",
    val selectedCategoryLabel: String = "",
    val subject: String = "",
    val description: String = "",
    val attachments: List<SupportAttachment> = emptyList(),
    val validation: SupportValidationDto = SupportValidationDto(),
    val isSubmitting: Boolean = false,
    val ticketCreatedSuccess: Boolean = false,
    val createdTicketNumber: String = "",
    val errorMessage: String? = null,
    val deviceModel: String = "Android Device",
    val osVersion: String = "Android 15",
    val appVersion: String = "1.0.0",
    val manufacturer: String = "Android",
) {
    val totalAttachmentSizeBytes: Long
        get() = attachments.sumOf { it.sizeBytes }

    val isSubjectValid: Boolean
        get() = subject.trim().length in validation.subjectMinLength..validation.subjectMaxLength

    val isDescriptionValid: Boolean
        get() = description.trim().length in validation.messageMinLength..validation.messageMaxLength

    val isCategoryValid: Boolean
        get() = selectedCategoryKey.isNotBlank()

    val isAttachmentsValid: Boolean
        get() = attachments.size <= validation.maxAttachments && totalAttachmentSizeBytes <= validation.maxTotalAttachmentSizeBytes

    val isFormValid: Boolean
        get() = isSubjectValid && isDescriptionValid && isCategoryValid && isAttachmentsValid && !isSubmitting
}

class RaiseTicketViewModel(
    private val ticketRepository: TicketRepository? = null,
    deviceInstallationManager: DeviceInstallationManager = DeviceInstallationManager(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(RaiseTicketUiState())
    val uiState: StateFlow<RaiseTicketUiState> = _uiState.asStateFlow()

    init {
        val deviceInfo = deviceInstallationManager.getDeviceInfo()
        _uiState.value = _uiState.value.copy(
            deviceModel = deviceInfo.deviceName,
            appVersion = deviceInfo.appVersion ?: "1.0.0"
        )
        loadTicketsHome()
    }

    fun loadTicketsHome() {
        val repo = ticketRepository ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingHome = true, errorMessage = null)
            when (val result = repo.fetchTicketsHome()) {
                is NetworkResult.Success -> {
                    val data = result.data
                    val categories = data.categories
                    val defaultCat = categories.firstOrNull()
                    val validation = data.validation ?: SupportValidationDto()

                    _uiState.value = _uiState.value.copy(
                        isLoadingHome = false,
                        homeTitle = data.title ?: "Raise Support Ticket",
                        homeSubtitle = data.subtitle ?: "Describe Your Issue",
                        homeDescription = data.description
                            ?: "Select a category and attach media so our team can resolve your issue quickly.",
                        categories = categories,
                        selectedCategoryKey = defaultCat?.key ?: "ACCOUNT",
                        selectedCategoryLabel = defaultCat?.label ?: defaultCat?.key ?: "Account & Login",
                        validation = validation
                    )
                }

                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingHome = false,
                        errorMessage = "Unable to load ticket configuration. Please try again."
                    )
                }

                is NetworkResult.Exception -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingHome = false,
                        errorMessage = "Unable to load ticket configuration. Please check your connection and try again."
                    )
                }
            }
        }
    }

    fun selectCategory(categoryKey: String) {
        val cat = _uiState.value.categories.firstOrNull { it.key == categoryKey }
        val label = cat?.label ?: categoryKey
        _uiState.value = _uiState.value.copy(
            selectedCategoryKey = categoryKey,
            selectedCategoryLabel = label
        )
    }

    fun updateSubject(subject: String) {
        val maxLen = _uiState.value.validation.subjectMaxLength
        val clamped = if (subject.length > maxLen) subject.take(maxLen) else subject
        _uiState.value = _uiState.value.copy(subject = clamped, errorMessage = null)
    }

    fun updateDescription(description: String) {
        val maxLen = _uiState.value.validation.messageMaxLength
        val clamped = if (description.length > maxLen) description.take(maxLen) else description
        _uiState.value = _uiState.value.copy(description = clamped, errorMessage = null)
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

    fun submitTicket() {
        val state = _uiState.value
        if (!state.isFormValid || state.isSubmitting) return

        val repo = ticketRepository
        if (repo == null) {
            _uiState.value = state.copy(
                ticketCreatedSuccess = true,
                createdTicketNumber = "#TKT-${(10000..99999).random()}"
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

            val requestDto = CreateSupportTicketRequestDTO(
                category = state.selectedCategoryKey,
                subject = state.subject.trim(),
                description = state.description.trim(),
                diagnosticInfo = diagnostics,
                attachmentUrls = emptyList()
            )

            when (val result = repo.createTicket(requestDto, state.attachments)) {
                is NetworkResult.Success -> {
                    val ticketNum = result.data.ticketNumber.ifBlank { "#TKT-${(10000..99999).random()}" }
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        ticketCreatedSuccess = true,
                        createdTicketNumber = ticketNum,
                        subject = "",
                        description = "",
                        attachments = emptyList()
                    )
                }

                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        errorMessage = "Unable to submit your support ticket right now. Please check your internet connection and try again."
                    )
                }

                is NetworkResult.Exception -> {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        errorMessage = "Unable to submit your support ticket right now. Please check your internet connection and try again."
                    )
                }
            }
        }
    }

    fun dismissSuccessDialog() {
        _uiState.value = _uiState.value.copy(ticketCreatedSuccess = false)
    }
}
