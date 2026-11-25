package com.muscleplay.app.core.network.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class AnalyticsHomeResponseDto(
    val title: String? = null,
    val subtitle: String? = null,
    val description: String? = null,
    val healthSnapshot: HealthSnapshotDto? = null,
    val weightAnalytics: WeightAnalyticsDto? = null,
    val measureOthers: MeasureOthersDto? = null,
    val popularCalculators: List<PopularCalculatorDto> = emptyList(),
)

@Serializable
data class HealthSnapshotDto(
    val title: String? = null,
    val subtitle: String? = null,
    val metrics: List<HealthMetricDto> = emptyList(),
    val lastUpdatedAt: String? = null,
)

@Serializable
data class HealthMetricDto(
    val key: String = "",
    val title: String = "",
    val value: String = "",
    val unit: String? = null,
    val status: String? = null,
    val iconKey: String? = null,
    val displayOrder: Int? = 0,
)

@Serializable
data class PopularCalculatorDto(
    val key: String = "",
    val name: String = "",
    val category: String? = null,
    val description: String? = null,
    val unit: String? = null,
    val iconKey: String? = null,
    val requiresSavedData: Boolean = false,
)

@Serializable
data class MeasureOthersDto(
    val title: String? = null,
    val description: String? = null,
    val buttonText: String? = null,
    val action: String? = null,
)

@Serializable
data class WeightAnalyticsDto(
    val currentWeightKg: Double? = null,
    val targetWeightKg: Double? = null,
    val weightChangeKg: Double? = null,
    val startingWeightKg: Double? = null,
    val goalName: String? = null,
    val progressPercent: Int? = 0,
)

@Serializable
data class CalculatorHomeResponseDto(
    val title: String? = null,
    val subtitle: String? = null,
    val description: String? = null,
    val calculatorCategories: List<CalculatorCategoryDto> = emptyList(),
    val popularCalculators: List<PopularCalculatorDto> = emptyList(),
    val recentlyUsedCalculators: List<PopularCalculatorDto> = emptyList(),
)

@Serializable
data class CalculatorCategoryDto(
    val category: String = "",
    val calculators: List<PopularCalculatorDto> = emptyList(),
)

@Serializable
data class SmartCalculationRequest(
    val inputs: Map<String, JsonElement>,
)
