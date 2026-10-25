package com.muscleplay.app.feature.games.squat

import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.sqrt

/**
 * Platform-independent body landmark.
 *
 * x and y are normalized image coordinates.
 * visibility is expected to be between 0.0 and 1.0.
 */
data class BodyLandmark(
    val x: Float,
    val y: Float,
    val visibility: Float,
)

data class LegPose(
    val hip: BodyLandmark,
    val knee: BodyLandmark,
    val ankle: BodyLandmark,
)

data class PoseFrame(
    val leftLeg: LegPose?,
    val rightLeg: LegPose?,
)

enum class SquatPhase {
    WAITING_FOR_STANDING,
    STANDING,
    SQUATTING
}

data class SquatState(
    val repetitions: Int = 0,
    val phase: SquatPhase = SquatPhase.WAITING_FOR_STANDING,
    val kneeAngleDegrees: Float? = null,
    val isPoseReliable: Boolean = false,
)


class SquatRepCounter(
    private val minimumVisibility: Float = 0.6f,
    private val standingAngle: Float = 145f,
    private val squatAngle: Float = 90f,
    private val angleWindowSize: Int = 3,
    private val bottomFramesRequired: Int = 3,
    private val standingFramesRequired: Int = 2,
    private val cooldownFrames: Int = 6,
) {
    var state: SquatState = SquatState()
        private set

    private val angleWindow = mutableListOf<Float>()
    private var bottomFrameCount = 0
    private var standingFrameCount = 0
    private var cooldownRemaining = 0


    fun processFrame(frame: PoseFrame): SquatState {
        val rawAngle = calculateKneeAngle(frame)

        if (rawAngle == null) {
            angleWindow.clear()
            bottomFrameCount = 0
            standingFrameCount = 0

            state = state.copy(
                kneeAngleDegrees = null,
                isPoseReliable = false
            )
            return state
        }

        // Keep a rolling window of recent valid angles.
        angleWindow.add(rawAngle)

        if (angleWindow.size > angleWindowSize) {
            angleWindow.removeAt(0)
        }

        // Moving average reduces frame-to-frame angle noise.
        val filteredAngle = angleWindow.average().toFloat()

//        println(
//            "SquatDebug: " +
//                    "angle=$filteredAngle, " +
//                    "phase=${state.phase}, " +
//                    "reps=${state.repetitions}, " +
//                    "reliable=true, " +
//                    "bottomFrames=$bottomFrameCount, " +
//                    "standingFrames=$standingFrameCount, " +
//                    "cooldown=$cooldownRemaining"
//        )

        if (cooldownRemaining > 0) {
            cooldownRemaining--
        }

        var repetitions = state.repetitions
        var phase = state.phase

        when (state.phase) {
            SquatPhase.WAITING_FOR_STANDING -> {
                if (filteredAngle >= standingAngle) {
                    standingFrameCount++
                } else {
                    standingFrameCount = 0
                }

                if (standingFrameCount >= standingFramesRequired) {
                    phase = SquatPhase.STANDING
                    standingFrameCount = 0
                }
            }

            SquatPhase.STANDING -> {
                standingFrameCount = 0

                if (cooldownRemaining == 0 &&
                    filteredAngle <= squatAngle
                ) {
                    bottomFrameCount++
                } else {
                    bottomFrameCount = 0
                }

                if (bottomFrameCount >= bottomFramesRequired) {
                    phase = SquatPhase.SQUATTING
                    bottomFrameCount = 0
                    standingFrameCount = 0
                }
            }

            SquatPhase.SQUATTING -> {
                if (filteredAngle >= standingAngle) {
                    standingFrameCount++
                } else {
                    standingFrameCount = 0
                }

                // Count only after a stable return to standing.
                if (standingFrameCount >= standingFramesRequired) {
                    repetitions++
                    phase = SquatPhase.STANDING

                    standingFrameCount = 0
                    bottomFrameCount = 0
                    cooldownRemaining = cooldownFrames
                }
            }
        }

        state = SquatState(
            repetitions = repetitions,
            phase = phase,
            kneeAngleDegrees = filteredAngle,
            isPoseReliable = true
        )

        return state
    }


    fun reset() {
        state = SquatState()
        angleWindow.clear()
        bottomFrameCount = 0
        standingFrameCount = 0
        cooldownRemaining = 0
    }

    private fun calculateKneeAngle(frame: PoseFrame): Float? {
        val leftAngle = frame.leftLeg
            ?.takeIf(::isReliable)
            ?.let(::kneeAngle)

        val rightAngle = frame.rightLeg
            ?.takeIf(::isReliable)
            ?.let(::kneeAngle)

        return when {
            leftAngle != null && rightAngle != null ->
                (leftAngle + rightAngle) / 2f

            leftAngle != null -> leftAngle
            rightAngle != null -> rightAngle
            else -> null
        }
    }

    private fun isReliable(leg: LegPose): Boolean {
        return listOf(leg.hip, leg.knee, leg.ankle).all {
            it.visibility >= minimumVisibility
        }
    }

    private fun kneeAngle(leg: LegPose): Float {
        val hip = leg.hip
        val knee = leg.knee
        val ankle = leg.ankle

        val ax = hip.x - knee.x
        val ay = hip.y - knee.y
        val bx = ankle.x - knee.x
        val by = ankle.y - knee.y

        val dot = ax * bx + ay * by
        val magnitudeA = sqrt(ax * ax + ay * ay)
        val magnitudeB = sqrt(bx * bx + by * by)

        if (magnitudeA < 0.0001f || magnitudeB < 0.0001f) {
            return 0f
        }

        val cosine = (dot / (magnitudeA * magnitudeB))
            .coerceIn(-1f, 1f)

        return (acos(cosine.toDouble()) * 180.0 / PI).toFloat()
    }
}
