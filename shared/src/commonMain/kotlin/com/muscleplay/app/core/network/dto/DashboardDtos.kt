package com.muscleplay.app.core.network.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val jsonConfig = Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
}

@Serializable
data class DashboardResponseDto(
    val user: UserSummary? = null,
    val activity: ActivitySummaryResponseDto? = null,
    val nutrition: DailyNutritionSummaryResponse? = null,
    val workoutAnalytics: WorkoutAnalyticsOverviewResponse? = null,
    val wallet: WalletSummary? = null,
    val notifications: NotificationSummary? = null,
    val recommendedWorkout: WorkoutRecommendation? = null,
    val generatedAt: String? = null,
) {
    @Serializable
    data class UserSummary(
        val userId: String? = null,
        val email: String? = null,
        val name: String? = null,
        val profileImageUrl: String? = null,
        val gender: String? = null,
        val age: Int? = null,
        val heightCm: Double? = null,
        val weightKg: Double? = null,
        val activityLevel: String? = null,
        val goal: String? = null,
        val profileCompleted: Boolean = false,
    )

    @Serializable
    data class WalletSummary(
        val balanceCoins: Long = 0L,
    )

    @Serializable
    data class NotificationSummary(
        val unreadCount: Long = 0L,
    )

    @Serializable
    data class WorkoutRecommendation(
        val id: Long? = null,
        val planKey: String? = null,
        val name: String? = null,
        val description: String? = null,
        val goal: String? = null,
        val difficulty: String? = null,
        val estimatedSessionMinutes: Int? = null,
        val daysPerWeek: Int = 0,
    )
}

@Serializable
data class WorkoutAnalyticsOverviewResponse(
    val from: String? = null,
    val to: String? = null,
    val totalWorkouts: Long = 0L,
    val totalDurationSeconds: Long = 0L,
    val averageDurationSeconds: Long = 0L,
    val totalSets: Long = 0L,
    val totalReps: Long = 0L,
    val totalVolumeKg: Double? = null,
    val activeDays: Long = 0L,
)

@Serializable
data class DailyNutritionSummaryResponse(
    val date: String? = null,
    val target: NutritionGoalResponse? = null,
    val consumed: NutrientTotals? = null,
    val remaining: NutrientTotals? = null,
    val calorieProgressPercent: Double? = null,
    val meals: List<MealEntryResponse> = emptyList(),
) {
    @Serializable
    data class NutrientTotals(
        val calories: Double? = null,
        val protein: Double? = null,
        val carbohydrates: Double? = null,
        val fat: Double? = null,
        val fiber: Double? = null,
        val sugar: Double? = null,
    )
}

@Serializable
data class NutritionGoalResponse(
    val dailyCalories: Double? = null,
    val dailyProtein: Double? = null,
    val dailyCarbohydrates: Double? = null,
    val dailyFat: Double? = null,
)

@Serializable
data class MealEntryResponse(
    val id: String? = null,
    val foodId: String? = null,
    val foodName: String? = null,
    val mealType: String? = null,
    val mealDate: String? = null,
    val quantityGrams: Double? = null,
    val quantityUnit: String? = null,
    val calories: Double? = null,
    val protein: Double? = null,
    val carbohydrates: Double? = null,
    val fat: Double? = null,
    val fiber: Double? = null,
    val sugar: Double? = null,
    val notes: String? = null,
    val createdAt: String? = null,
    val clientMealId: String? = null,
    val photoUrl: String? = null,
    val syncStatus: String? = null,
)

@Serializable
data class ActivitySummaryResponseDto(
    val date: String? = null,
    val timeZone: String? = null,
    val activeMinutes: Int = 0,
    val caloriesBurned: Double? = null,
    val distanceMeters: Double? = null,
    val steps: Long = 0L,
    val completedActivities: Long = 0L,
    val dailyGoalMinutes: Int = 0,
    val goalProgressPercent: Int = 0,
)

object DashboardJsonParser {
    fun parseDashboardResponseDto(json: String): DashboardResponseDto {
        if (json.isBlank()) return DashboardResponseDto()
        return try {
            jsonConfig.decodeFromString<DashboardResponseDto>(json)
        } catch (e: Throwable) {
            DashboardResponseDto()
        }
    }
}
