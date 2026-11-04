package com.muscleplay.app.feature.onboarding.model

import kotlin.math.roundToInt

enum class Gender(val title: String, val icon: String) {
    MALE("Male", "♂"),
    FEMALE("Female", "♀");

    val label: String get() = title
}

typealias UserGender = Gender

enum class DevicePlatform {
    ANDROID,
    IOS
}

enum class FitnessGoal(
    val title: String,
    val description: String,
    val emoji: String,
    val calorieAdjustment: Int,
    val proteinPct: Double,
    val carbsPct: Double,
    val fatPct: Double
) {
    MAINTAIN_WEIGHT("Maintain Weight", "Keep your current body weight & build healthy habits", "⚖️", 0, 0.30, 0.40, 0.30),
    MILD_WEIGHT_LOSS("Mild Weight Loss", "Slow & steady fat loss (~0.25 kg/week)", "📉", -250, 0.35, 0.35, 0.30),
    WEIGHT_LOSS("Weight Loss", "Standard fat loss (~0.5 kg/week)", "🔥", -500, 0.40, 0.30, 0.30),
    EXTREME_WEIGHT_LOSS("Extreme Weight Loss", "Aggressive fat loss (~1 kg/week)", "⚡", -1000, 0.40, 0.35, 0.25),
    MILD_WEIGHT_GAIN("Mild Weight Gain", "Lean muscle gain with minimal fat (~0.25 kg/week)", "📈", 250, 0.30, 0.50, 0.20),
    WEIGHT_GAIN("Weight Gain", "Steady muscle & strength gain (~0.5 kg/week)", "💪", 500, 0.30, 0.50, 0.20),
    EXTREME_WEIGHT_GAIN("Extreme Weight Gain", "Rapid bulk & mass building (~1 kg/week)", "🏋️‍♂️", 1000, 0.25, 0.55, 0.20)
}

typealias UserFitnessGoal = FitnessGoal

enum class MobileAccountStatus {
    ACTIVE,
    SUSPENDED,
    DEACTIVATED
}

enum class ActivityLevel(
    val title: String,
    val description: String,
    val multiplier: Double
) {
    LEVEL_1("Sedentary", "Little or no exercise; mostly sitting throughout the day.", 1.2),
    LEVEL_2("Lightly Active", "Light exercise or physical activity 1–3 days per week.", 1.375),
    LEVEL_3("Moderately Active", "Moderate exercise or physical activity 3–5 days per week.", 1.55),
    LEVEL_4("Very Active", "Intense exercise or physical activity 6–7 days per week.", 1.725),
    LEVEL_5("Extremely Active", "Very intense daily exercise or physically demanding work, sometimes with additional training.", 1.9)
}

typealias UserActivityLevel = ActivityLevel

enum class UserExperienceLevel(val title: String, val badge: String) {
    BEGINNER("Beginner (< 6 months)", "New Athlete"),
    INTERMEDIATE("Intermediate (1-3 years)", "Regular Lifter"),
    ADVANCED("Advanced (> 3 years)", "Experienced Titan")
}

data class UserOnboardingProfile(
    val fullName: String = "",
    val gender: Gender = Gender.MALE,
    val fitnessGoal: FitnessGoal = FitnessGoal.WEIGHT_LOSS,
    val heightCm: Double = 175.0,
    val weightKg: Double = 70.0,
    val targetWeightKg: Double = 65.0,
    val age: Int = 25,
    val activityLevel: ActivityLevel = ActivityLevel.LEVEL_3,
    val experienceLevel: UserExperienceLevel = UserExperienceLevel.INTERMEDIATE,
    val profileImageUri: String? = null,
    val isImperialUnits: Boolean = false
) {
    val bmrCalories: Int
        get() {
            val base = (10 * weightKg) + (6.25 * heightCm) - (5 * age)
            val genderOffset = if (gender == Gender.FEMALE) -161 else 5
            return (base + genderOffset).roundToInt().coerceAtLeast(1200)
        }

    val tdeeCalories: Int
        get() = (bmrCalories * activityLevel.multiplier).roundToInt()

    val targetDailyCalories: Int
        get() = (tdeeCalories + fitnessGoal.calorieAdjustment).coerceAtLeast(1200)

    val targetProteinGrams: Int
        get() = ((targetDailyCalories * fitnessGoal.proteinPct) / 4.0).roundToInt()

    val targetCarbsGrams: Int
        get() = ((targetDailyCalories * fitnessGoal.carbsPct) / 4.0).roundToInt()

    val targetFatGrams: Int
        get() = ((targetDailyCalories * fitnessGoal.fatPct) / 9.0).roundToInt()
}
