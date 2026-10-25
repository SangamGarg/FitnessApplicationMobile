
package com.muscleplay.app.feature.games.squat

import com.muscleplay.app.feature.games.core.ExerciseDetector
import com.muscleplay.app.feature.games.core.ExercisePhase
import com.muscleplay.app.feature.games.core.ExerciseState
import com.muscleplay.app.feature.games.core.ExerciseType
import com.muscleplay.app.ml.BodyJoint
import com.muscleplay.app.ml.JointLandmark
import com.muscleplay.app.ml.PoseSample

class SquatExerciseDetector(
    private val counter: SquatRepCounter = SquatRepCounter()
) : ExerciseDetector {

    override val type: ExerciseType = ExerciseType.SQUAT

    override fun processPose(pose: PoseSample): ExerciseState {
        val frame = pose.toPoseFrame()

        val updated = if (frame == null) {
            counter.state.copy(
                kneeAngleDegrees = null,
                isPoseReliable = false
            )
        } else {
            counter.processFrame(frame)
        }

        return updated.toExerciseState()
    }

    override fun onPoseLost(): ExerciseState {
        return counter.state.copy(
            kneeAngleDegrees = null,
            isPoseReliable = false
        ).toExerciseState()
    }

    override fun reset(): ExerciseState {
        counter.reset()
        return counter.state.toExerciseState()
    }

    private fun PoseSample.toPoseFrame(): PoseFrame? {
        val leftLeg = leg(
            BodyJoint.LEFT_HIP,
            BodyJoint.LEFT_KNEE,
            BodyJoint.LEFT_ANKLE
        )

        val rightLeg = leg(
            BodyJoint.RIGHT_HIP,
            BodyJoint.RIGHT_KNEE,
            BodyJoint.RIGHT_ANKLE
        )

        if (leftLeg == null && rightLeg == null) return null

        return PoseFrame(
            leftLeg = leftLeg,
            rightLeg = rightLeg
        )
    }

    private fun PoseSample.leg(
        hipJoint: BodyJoint,
        kneeJoint: BodyJoint,
        ankleJoint: BodyJoint
    ): LegPose? {
        val hip = joint(hipJoint)?.toBodyLandmark() ?: return null
        val knee = joint(kneeJoint)?.toBodyLandmark() ?: return null
        val ankle = joint(ankleJoint)?.toBodyLandmark() ?: return null

        return LegPose(
            hip = hip,
            knee = knee,
            ankle = ankle
        )
    }

    private fun JointLandmark.toBodyLandmark() = BodyLandmark(
        x = x,
        y = y,
        visibility = visibility
    )

    private fun SquatState.toExerciseState() = ExerciseState(
        repetitions = repetitions,
        phase = when (phase) {
            SquatPhase.WAITING_FOR_STANDING ->
                ExercisePhase.WAITING_FOR_START

            SquatPhase.STANDING ->
                ExercisePhase.READY

            SquatPhase.SQUATTING ->
                ExercisePhase.ACTIVE
        },
        isPoseReliable = isPoseReliable,
        primaryAngleDegrees = kneeAngleDegrees,
        feedback = when {
            !isPoseReliable -> "Move your legs into view"
            phase == SquatPhase.SQUATTING ->
                "Squat detected — stand up to complete the rep"

            phase == SquatPhase.WAITING_FOR_STANDING ->
                "Stand upright to begin"

            else -> "Pose detected — get ready to squat"
        }
    )
}
