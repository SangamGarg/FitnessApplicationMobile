package com.muscleplay.app.feature.games.pushup

import com.muscleplay.app.feature.games.core.ExercisePhase
import com.muscleplay.app.ml.BodyJoint
import com.muscleplay.app.ml.JointLandmark
import com.muscleplay.app.ml.PoseSample
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PushUpExerciseDetectorTest {

    private fun createDetector() = PushUpExerciseDetector(
        minimumVisibility = 0.5f,
        topAngleThreshold = 150f,
        bottomAngleThreshold = 95f,
        stableFramesRequired = 3,
    )

    /**
     * Creates a synthetic pose with the requested shoulder-elbow-wrist angle.
     * Both arms have the same angle and visibility.
     */
    private fun poseWithArmAngle(
        angleDegrees: Float,
        timestampMs: Long = 0L,
        visibility: Float = 0.99f,
    ): PoseSample {
        val radians = angleDegrees.toDouble() * PI / 180.0
        val wristX = cos(radians).toFloat()
        val wristY = sin(radians).toFloat()

        val landmarks = mapOf(
            BodyJoint.LEFT_SHOULDER to JointLandmark(
                x = 1f,
                y = 0f,
                visibility = visibility,
            ),
            BodyJoint.LEFT_ELBOW to JointLandmark(
                x = 0f,
                y = 0f,
                visibility = visibility,
            ),
            BodyJoint.LEFT_WRIST to JointLandmark(
                x = wristX,
                y = wristY,
                visibility = visibility,
            ),
            BodyJoint.RIGHT_SHOULDER to JointLandmark(
                x = 1f,
                y = 0f,
                visibility = visibility,
            ),
            BodyJoint.RIGHT_ELBOW to JointLandmark(
                x = 0f,
                y = 0f,
                visibility = visibility,
            ),
            BodyJoint.RIGHT_WRIST to JointLandmark(
                x = wristX,
                y = wristY,
                visibility = visibility,
            ),
        )

        return PoseSample(
            landmarks = landmarks,
            timestampMs = timestampMs,
        )
    }

    private fun sendFrames(
        detector: PushUpExerciseDetector,
        angle: Float,
        count: Int = 3,
    ) {
        repeat(count) { index ->
            detector.processPose(
                poseWithArmAngle(
                    angleDegrees = angle,
                    timestampMs = index.toLong(),
                )
            )
        }
    }

    @Test
    fun completePushUp_countsExactlyOneRepetition() {
        val detector = createDetector()

        // Establish the starting top position.
        sendFrames(detector, angle = 170f)

        assertEquals(0, detector.processPose(poseWithArmAngle(170f)).repetitions)

        // Lower to the bottom position.
        sendFrames(detector, angle = 80f)

        assertEquals(0, detector.processPose(poseWithArmAngle(80f)).repetitions)

        // Return to the top position and complete the repetition.
        sendFrames(detector, angle = 170f)

        val state = detector.processPose(poseWithArmAngle(170f))

        assertEquals(1, state.repetitions)
        assertEquals(ExercisePhase.READY, state.phase)
        assertTrue(state.isPoseReliable)
    }

    @Test
    fun partialMovement_doesNotCountRepetition() {
        val detector = createDetector()

        sendFrames(detector, angle = 170f)

        // This angle is neither low enough to register the bottom
        // nor a completed push-up.
        sendFrames(detector, angle = 120f)

        val state = detector.processPose(poseWithArmAngle(120f))

        assertEquals(0, state.repetitions)
        assertEquals(ExercisePhase.READY, state.phase)
    }

    @Test
    fun remainingAtTop_doesNotCountExtraRepetitions() {
        val detector = createDetector()

        sendFrames(detector, angle = 170f)
        sendFrames(detector, angle = 170f, count = 10)

        val state = detector.processPose(poseWithArmAngle(170f))

        assertEquals(0, state.repetitions)
        assertEquals(ExercisePhase.READY, state.phase)
    }

    @Test
    fun poseLoss_requiresStartingPositionAgain() {
        val detector = createDetector()

        sendFrames(detector, angle = 170f)
        sendFrames(detector, angle = 80f)

        val lostState = detector.onPoseLost()

        assertEquals(0, lostState.repetitions)
        assertFalse(lostState.isPoseReliable)

        // Bottom-position frames cannot complete a repetition
        // after tracking has been lost.
        sendFrames(detector, angle = 80f)

        assertEquals(
            0,
            detector.processPose(poseWithArmAngle(80f)).repetitions,
        )

        // Establish the top position again, then perform a full rep.
        sendFrames(detector, angle = 170f)
        sendFrames(detector, angle = 80f)
        sendFrames(detector, angle = 170f)

        val state = detector.processPose(poseWithArmAngle(170f))

        assertEquals(1, state.repetitions)
    }

    @Test
    fun reset_clearsRepetitionsAndRequiresStartingPositionAgain() {
        val detector = createDetector()

        sendFrames(detector, angle = 170f)
        sendFrames(detector, angle = 80f)
        sendFrames(detector, angle = 170f)

        assertEquals(
            1,
            detector.processPose(poseWithArmAngle(170f)).repetitions,
        )

        val resetState = detector.reset()

        assertEquals(0, resetState.repetitions)
        assertEquals(ExercisePhase.WAITING_FOR_START, resetState.phase)
        assertFalse(resetState.isPoseReliable)

        // The detector must establish the top position after reset.
        sendFrames(detector, angle = 170f)

        assertEquals(
            0,
            detector.processPose(poseWithArmAngle(170f)).repetitions,
        )
    }

    @Test
    fun invisibleArms_areNotAcceptedAsReliablePose() {
        val detector = createDetector()

        val state = detector.processPose(
            poseWithArmAngle(
                angleDegrees = 170f,
                visibility = 0.1f,
            )
        )

        assertFalse(state.isPoseReliable)
        assertEquals(0, state.repetitions)
    }
}