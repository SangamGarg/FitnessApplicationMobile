
package com.muscleplay.app.feature.games.pushup

import com.muscleplay.app.feature.games.core.ExerciseDetector
import com.muscleplay.app.feature.games.core.ExercisePhase
import com.muscleplay.app.feature.games.core.ExerciseState
import com.muscleplay.app.feature.games.core.ExerciseType
import com.muscleplay.app.ml.BodyJoint
import com.muscleplay.app.ml.JointLandmark
import com.muscleplay.app.ml.PoseSample
import kotlin.math.acos
import kotlin.math.sqrt

class PushUpExerciseDetector(
    private val minimumVisibility: Float = 0.5f,
    topAngleThreshold: Float = 150f,
    bottomAngleThreshold: Float = 95f,
    stableFramesRequired: Int = 3
) : ExerciseDetector {

    override val type: ExerciseType = ExerciseType.PUSH_UP

    private val repCounter = PushUpRepCounter(
        topAngleThreshold = topAngleThreshold,
        bottomAngleThreshold = bottomAngleThreshold,
        stableFramesRequired = stableFramesRequired
    )

    override fun processPose(pose: PoseSample): ExerciseState {
        val angle = findBestArmAngle(pose)

        if (angle == null) {
            return onPoseLost()
        }

        return mapState(repCounter.processAngle(angle))
    }

    override fun onPoseLost(): ExerciseState {
        return mapState(repCounter.onPoseLost())
    }

    override fun reset(): ExerciseState {
        return mapState(repCounter.reset())
    }

    private fun mapState(
        counterState: PushUpRepState
    ): ExerciseState {
        val exercisePhase = when (counterState.phase) {
            PushUpRepPhase.WAITING_FOR_TOP ->
                ExercisePhase.WAITING_FOR_START

            PushUpRepPhase.TOP ->
                ExercisePhase.READY

            PushUpRepPhase.BOTTOM ->
                ExercisePhase.ACTIVE
        }

        val feedback = when {
            !counterState.isPoseReliable ->
                "Move into a side-on position with your arms visible"

            counterState.phase == PushUpRepPhase.WAITING_FOR_TOP ->
                "Straighten your arms to begin"

            counterState.phase == PushUpRepPhase.TOP ->
                "Lower your body with control"

            else ->
                "Push back up to complete the repetition"
        }

        return ExerciseState(
            repetitions = counterState.repetitions,
            phase = exercisePhase,
            isPoseReliable = counterState.isPoseReliable,
            primaryAngleDegrees = counterState.elbowAngleDegrees,
            feedback = feedback
        )
    }

    private fun findBestArmAngle(pose: PoseSample): Float? {
        val leftShoulder =
            pose.joint(BodyJoint.LEFT_SHOULDER, minimumVisibility)
        val leftElbow =
            pose.joint(BodyJoint.LEFT_ELBOW, minimumVisibility)
        val leftWrist =
            pose.joint(BodyJoint.LEFT_WRIST, minimumVisibility)

        val rightShoulder =
            pose.joint(BodyJoint.RIGHT_SHOULDER, minimumVisibility)
        val rightElbow =
            pose.joint(BodyJoint.RIGHT_ELBOW, minimumVisibility)
        val rightWrist =
            pose.joint(BodyJoint.RIGHT_WRIST, minimumVisibility)

        val leftAngle = calculateArmAngle(
            leftShoulder,
            leftElbow,
            leftWrist
        )

        val rightAngle = calculateArmAngle(
            rightShoulder,
            rightElbow,
            rightWrist
        )

        val leftVisibility = armVisibility(
            leftShoulder,
            leftElbow,
            leftWrist
        )

        val rightVisibility = armVisibility(
            rightShoulder,
            rightElbow,
            rightWrist
        )

        return when {
            leftAngle == null -> rightAngle
            rightAngle == null -> leftAngle
            leftVisibility >= rightVisibility -> leftAngle
            else -> rightAngle
        }
    }

    private fun armVisibility(
        shoulder: JointLandmark?,
        elbow: JointLandmark?,
        wrist: JointLandmark?
    ): Float {
        if (shoulder == null || elbow == null || wrist == null) {
            return 0f
        }

        return shoulder.visibility +
                elbow.visibility +
                wrist.visibility
    }

    private fun calculateArmAngle(
        shoulder: JointLandmark?,
        elbow: JointLandmark?,
        wrist: JointLandmark?
    ): Float? {
        if (shoulder == null || elbow == null || wrist == null) {
            return null
        }

        val ax = shoulder.x - elbow.x
        val ay = shoulder.y - elbow.y
        val bx = wrist.x - elbow.x
        val by = wrist.y - elbow.y

        val magnitudeA = sqrt(ax * ax + ay * ay)
        val magnitudeB = sqrt(bx * bx + by * by)

        if (magnitudeA < 0.0001f || magnitudeB < 0.0001f) {
            return null
        }

        val cosine = (
                (ax * bx + ay * by) /
                        (magnitudeA * magnitudeB)
                ).coerceIn(-1f, 1f)

        return (
                acos(cosine.toDouble()) * 180.0 / kotlin.math.PI
                ).toFloat()
    }
}
