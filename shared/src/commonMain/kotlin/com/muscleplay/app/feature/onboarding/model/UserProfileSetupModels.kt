package com.muscleplay.app.feature.onboarding.model

import kotlin.math.roundToInt

enum class UserGender(val label: String, val icon: String) {
    MALE("Male", "♂"),
    FEMALE("Female", "♀"),
}

enum class UserFitnessGoal(
    val title: String,
    val description: String,
    val emoji: String,
    val calorieAdjustment: Int,
    val proteinPct: Double,
    val carbsPct: Double,
    val fatPct: Double
) {
    FAT_LOSS("Fat Loss & Lean Shred", "Burn fat while preserving lean muscle mass", "🔥", -500, 0.40, 0.30, 0.30),
    MUSCLE_GAIN("Muscle Gain & Hypertrophy", "Build muscle size and strength with caloric surplus", "💪", 300, 0.30, 0.50, 0.20),
    STRENGTH("Max Strength & Power", "Focus on 1RM power benchmarks and heavy lifting", "🏋️‍♂️", 200, 0.35, 0.45, 0.20),
    ENDURANCE("Endurance & General Health", "Maintain optimal energy for stamina and active lifestyle", "🧘", 0, 0.30, 0.40, 0.30)
}

enum class UserActivityLevel(val title: String, val description: String, val multiplier: Double) {
    SEDENTARY("Sedentary", "Desk job / minimal movement", 1.2),
    LIGHTLY_ACTIVE("Lightly Active", "1-2 workout days per week", 1.375),
    MODERATELY_ACTIVE("Moderately Active", "3-4 workout days per week", 1.55),
    VERY_ACTIVE("Very Active", "5-6 workout days per week", 1.725),
    ATHLETE("Athlete", "Heavy daily training or double sessions", 1.9)
}

enum class UserExperienceLevel(val title: String, val badge: String) {
    BEGINNER("Beginner (< 6 months)", "New Athlete"),
    INTERMEDIATE("Intermediate (1-3 years)", "Regular Lifter"),
    ADVANCED("Advanced (> 3 years)", "Experienced Titan")
}

data class UserOnboardingProfile(
    val fullName: String = "",
    val gender: UserGender = UserGender.MALE,
    val fitnessGoal: UserFitnessGoal = UserFitnessGoal.FAT_LOSS,
    val heightCm: Double = 175.0,
    val weightKg: Double = 70.0,
    val targetWeightKg: Double = 65.0,
    val age: Int = 25,
    val activityLevel: UserActivityLevel = UserActivityLevel.MODERATELY_ACTIVE,
    val experienceLevel: UserExperienceLevel = UserExperienceLevel.INTERMEDIATE,
    val profileImageUri: String? = null,
    val isImperialUnits: Boolean = false
) {
    val bmrCalories: Int
        get() {
            val base = (10 * weightKg) + (6.25 * heightCm) - (5 * age)
            val genderOffset = if (gender == UserGender.FEMALE) -161 else 5
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
