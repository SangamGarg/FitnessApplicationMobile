package com.muscleplay.app.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class SupportTicketResponseDTO(
    val ticketNumber: String = "",
    val userId: String? = null,
    val userEmail: String? = null,
    val category: String? = null,
    val subject: String = "",
    val description: String = "",
    val status: String = "OPEN",
    val priority: String = "MEDIUM",
    val diagnosticInfo: DeviceDiagnosticInfo? = null,
    val attachmentUrls: List<String> = emptyList(),
    val resolutionNotes: String? = null,
    val assignedToEmail: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

@Serializable
data class SupportTicketMessageResponseDTO(
    val id: Long = 0L,
    val actorEmail: String? = null,
    val message: String = "",
    val internalNote: Boolean = false,
    val createdAt: String? = null,
)

@Serializable
data class SupportTicketHomeResponseDto(
    val title: String? = null,
    val subtitle: String? = null,
    val description: String? = null,
    val categories: List<SupportIssueCategoryDto> = emptyList(),
    val validation: SupportValidationDto? = null,
)

@Serializable
data class CreateSupportTicketRequestDTO(
    val category: String,
    val subject: String,
    val description: String,
    val diagnosticInfo: DeviceDiagnosticInfo,
    val attachmentUrls: List<String> = emptyList(),
)
