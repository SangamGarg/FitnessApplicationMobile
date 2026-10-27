package com.muscleplay.app.feature.workout.model

import androidx.compose.ui.graphics.Color

enum class MuscleGroup(
    val displayName: String,
    val isFrontView: Boolean
) {
    CHEST("Chest", true),
    SHOULDERS("Shoulders", true),
    BICEPS("Biceps", true),
    ABS("Abs / Core", true),
    QUADS("Quadriceps", true),
    BACK("Upper & Lower Back", false),
    TRICEPS("Triceps", false),
    GLUTES("Glutes", false),
    HAMSTRINGS("Hamstrings", false),
    CALVES("Calves", false)
}

enum class FatigueLevel(
    val label: String,
    val color: Color,
    val fatiguePercentage: Int
) {
    RECOVERED("100% Recovered", Color(0xFF4CAF50), 0),
    MODERATE("50% Moderate Strain", Color(0xFFFFD54F), 50),
    HIGH_STRAIN("High Strain 🔥", Color(0xFFFF5722), 90)
}

data class MuscleStatus(
    val muscleGroup: MuscleGroup,
    val fatigueLevel: FatigueLevel,
    val lastTrainedText: String
)

val defaultMuscleStatuses = mapOf(
    MuscleGroup.CHEST to MuscleStatus(MuscleGroup.CHEST, FatigueLevel.HIGH_STRAIN, "Trained Today"),
    MuscleGroup.TRICEPS to MuscleStatus(MuscleGroup.TRICEPS, FatigueLevel.HIGH_STRAIN, "Trained Today"),
    MuscleGroup.SHOULDERS to MuscleStatus(MuscleGroup.SHOULDERS, FatigueLevel.MODERATE, "Trained Yesterday"),
    MuscleGroup.ABS to MuscleStatus(MuscleGroup.ABS, FatigueLevel.MODERATE, "Trained Yesterday"),
    MuscleGroup.QUADS to MuscleStatus(MuscleGroup.QUADS, FatigueLevel.RECOVERED, "Trained 3 days ago"),
    MuscleGroup.HAMSTRINGS to MuscleStatus(MuscleGroup.HAMSTRINGS, FatigueLevel.RECOVERED, "Trained 3 days ago"),
    MuscleGroup.BACK to MuscleStatus(MuscleGroup.BACK, FatigueLevel.RECOVERED, "Trained 4 days ago"),
    MuscleGroup.BICEPS to MuscleStatus(MuscleGroup.BICEPS, FatigueLevel.RECOVERED, "Trained 4 days ago"),
    MuscleGroup.GLUTES to MuscleStatus(MuscleGroup.GLUTES, FatigueLevel.RECOVERED, "Trained 5 days ago"),
    MuscleGroup.CALVES to MuscleStatus(MuscleGroup.CALVES, FatigueLevel.RECOVERED, "Trained 5 days ago")
)
