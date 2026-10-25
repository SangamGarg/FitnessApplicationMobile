
package com.muscleplay.app.feature.games.core

import com.muscleplay.app.ml.PoseSample

enum class ExerciseType {
    SQUAT,
    PUSH_UP,
    JUMPING_JACK,
    LUNGE
}

enum class ExercisePhase {
    WAITING_FOR_START,
    READY,
    ACTIVE,
    COMPLETED,
    UNKNOWN
}

data class ExerciseState(
    val repetitions: Int = 0,
    val phase: ExercisePhase = ExercisePhase.WAITING_FOR_START,
    val isPoseReliable: Boolean = false,
    val primaryAngleDegrees: Float? = null,
    val feedback: String = "Move into the camera frame"
)

interface ExerciseDetector {
    val type: ExerciseType

    fun processPose(pose: PoseSample): ExerciseState

    fun onPoseLost(): ExerciseState

    fun reset(): ExerciseState
}
