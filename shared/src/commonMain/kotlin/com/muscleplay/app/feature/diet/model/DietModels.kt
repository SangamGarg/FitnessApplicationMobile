package com.muscleplay.app.feature.diet.model

enum class DietaryPreference(val label: String) {
    ALL("All"),
    VEG("Veg 🌿"),
    NON_VEG("Non-Veg 🍗"),
    VEGAN("Vegan 🌱"),
    EGGETARIAN("Eggetarian 🥚")
}

enum class FitnessGoal(val label: String) {
    MUSCLE_GAIN("Muscle Gain 💪"),
    WEIGHT_LOSS("Weight Loss 🔥"),
    MAINTENANCE("Maintenance ⚖️"),
    CLEAN_BULKING("Clean Bulking 🏋️")
}

enum class SyncStatus {
    SYNCED,
    PENDING_UPLOAD,
    FAILED
}

data class MacroInfo(
    val calories: Int,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatsGrams: Int
)

data class Recipe(
    val id: String,
    val title: String,
    val prepTimeMinutes: Int,
    val difficulty: String,
    val macros: MacroInfo,
    val dietType: DietaryPreference,
    val ingredients: List<String>,
    val instructions: List<String>,
    val imageUrl: String? = null
)

data class LoggedMealEntity(
    val id: String,
    val name: String,
    val calories: Int,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatsGrams: Int,
    val timestampIso: String,
    val photoUri: String? = null,
    val syncStatus: SyncStatus = SyncStatus.PENDING_UPLOAD
)
