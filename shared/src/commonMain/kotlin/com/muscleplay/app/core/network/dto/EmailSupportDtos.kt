package com.muscleplay.app.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class SupportEmailHomeResponseDto(
    val title: String? = null,
    val subtitle: String? = null,
    val description: String? = null,
    val categories: List<SupportIssueCategoryDto> = emptyList(),
    val validation: SupportValidationDto? = null,
    val diagnosticsEnabled: Boolean = true,
)

@Serializable
data class SupportIssueCategoryDto(
    val key: String = "",
    val label: String = "",
)

@Serializable
data class SupportValidationDto(
    val subjectMinLength: Int = 5,
    val subjectMaxLength: Int = 100,
    val messageMinLength: Int = 10,
    val messageMaxLength: Int = 2000,
    val maxAttachments: Int = 3,
    val maxAttachmentSizeBytes: Long = 5242880L,
    val maxTotalAttachmentSizeBytes: Long = 10485760L,
    val allowedMimeTypes: List<String> = listOf("image/jpeg", "image/png", "application/pdf"),
)

@Serializable
data class DeviceDiagnosticInfo(
    val appVersion: String = "1.0.0",
    val osVersion: String = "Android 15",
    val deviceModel: String = "Unknown",
    val manufacturer: String = "Unknown",
    val isBatteryOptimizationIgnored: Boolean? = true,
    val isGoogleFitLinked: Boolean? = false,
    val deviceType: String = "ANDROID",
)

@Serializable
data class EmailFromUserRequestDto(
    val category: String,
    val subject: String,
    val message: String,
    val diagnostics: DeviceDiagnosticInfo,
)

@Serializable
data class EmailFromUserResponseDto(
    val status: String? = null,
    val statusCode: Int? = null,
    val message: String? = null,
    val timestamp: String? = null,
)

data class SupportAttachment(
    val name: String,
    val sizeBytes: Long,
    val mimeType: String,
    val bytes: ByteArray? = null,
    val uriString: String? = null,
)
