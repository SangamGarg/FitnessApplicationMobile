package com.muscleplay.app.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class SupportHomeResponseDto(
    val categories: List<SupportFaqCategoryDto> = emptyList(),
    val popularFaqs: List<SupportFaqDto> = emptyList(),
    val appVersion: String? = null,
    val appBuild: String? = null,
    val footer: String? = null,
)

@Serializable
data class SupportFaqCategoryDto(
    val id: Long = 0L,
    val categoryKey: String = "",
    val name: String = "",
    val description: String? = null,
    val iconKey: String? = null,
    val sortOrder: Int = 0,
)

@Serializable
data class SupportFaqDto(
    val id: Long = 0L,
    val categoryKey: String = "",
    val categoryName: String = "",
    val question: String = "",
    val answer: String = "",
    val popular: Boolean = false,
    val updatedAt: String? = null,
)

@Serializable
data class AppInfoContentResponseDto(
    val content: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val version: Long? = null,
    val updatedBy: String? = null,
)

@Serializable
data class SupportFaqPageDto(
    val content: List<SupportFaqDto> = emptyList(),
    val totalElements: Long = 0L,
    val totalPages: Int = 0,
    val last: Boolean = true,
    val first: Boolean = true,
    val number: Int = 0,
    val size: Int = 20,
    val numberOfElements: Int = 0,
    val empty: Boolean = false,
)
