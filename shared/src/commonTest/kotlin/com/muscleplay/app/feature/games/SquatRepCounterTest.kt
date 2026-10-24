package com.muscleplay.app.feature.games

import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.math.cos
import kotlin.math.sin

class SquatRepCounterTest {

    private val counter = SquatRepCounter()

    @Test
    fun stableStandingPoseIsReliable() {
        repeat(15) {
            counter.processFrame(frame(angleDegrees = 170f))
        }

        assertTrue(counter.state.isPoseReliable)
        assertEquals(0, counter.state.repetitions)
    }

    @Test
    fun completeSquatCountsExactlyOneRepetition() {
        // Establish the starting standing position.
        repeat(15) {
            counter.processFrame(frame(angleDegrees = 170f))
        }

        // Move into the squat.
        repeat(15) {
            counter.processFrame(frame(angleDegrees = 80f))
        }

        // Return to standing to complete the repetition.
        repeat(20) {
            counter.processFrame(frame(angleDegrees = 170f))
        }

        assertEquals(1, counter.state.repetitions)
    }

    @Test
    fun incompleteSquatDoesNotCountARepetition() {
        repeat(15) {
            counter.processFrame(frame(angleDegrees = 170f))
        }

        // The user lowers into the squat but never stands back up.
        repeat(15) {
            counter.processFrame(frame(angleDegrees = 80f))
        }

        assertEquals(0, counter.state.repetitions)
    }

    @Test
    fun repeatedStandingFramesDoNotCreateExtraRepetitions() {
        repeat(15) {
            counter.processFrame(frame(angleDegrees = 170f))
        }
        repeat(15) {
            counter.processFrame(frame(angleDegrees = 80f))
        }
        repeat(20) {
            counter.processFrame(frame(angleDegrees = 170f))
        }

        // Remaining upright must not count another repetition.
        repeat(30) {
            counter.processFrame(frame(angleDegrees = 170f))
        }

        assertEquals(1, counter.state.repetitions)
    }

    @Test
    fun lowVisibilityLandmarksAreNotReliable() {
        repeat(15) {
            counter.processFrame(
                frame(
                    angleDegrees = 170f,
                    visibility = 0.1f
                )
            )
        }

        assertFalse(counter.state.isPoseReliable)
        assertEquals(0, counter.state.repetitions)
    }

    @Test
    fun missingLegsAreNotReliable() {
        repeat(5) {
            counter.processFrame(
                PoseFrame(
                    leftLeg = null,
                    rightLeg = null
                )
            )
        }

        assertFalse(counter.state.isPoseReliable)
        assertEquals(0, counter.state.repetitions)
    }

    @Test
    fun resetClearsRepetitionCount() {
        repeat(15) {
            counter.processFrame(frame(angleDegrees = 170f))
        }
        repeat(15) {
            counter.processFrame(frame(angleDegrees = 80f))
        }
        repeat(20) {
            counter.processFrame(frame(angleDegrees = 170f))
        }

        assertEquals(1, counter.state.repetitions)

        counter.reset()

        assertEquals(0, counter.state.repetitions)
        assertEquals(
            SquatPhase.WAITING_FOR_STANDING,
            counter.state.phase
        )
    }

    private fun frame(
        angleDegrees: Float,
        visibility: Float = 0.95f,
    ): PoseFrame {
        return PoseFrame(
            leftLeg = leg(angleDegrees, visibility),
            rightLeg = leg(angleDegrees, visibility)
        )
    }

    private fun leg(
        angleDegrees: Float,
        visibility: Float,
    ): LegPose {
        val radians = angleDegrees.toDouble() * PI / 180.0
        val kneeX = 0.5f
        val kneeY = 0.5f
        val segmentLength = 0.2

        val hip = BodyLandmark(
            x = kneeX,
            y = kneeY - segmentLength.toFloat(),
            visibility = visibility
        )

        val knee = BodyLandmark(
            x = kneeX,
            y = kneeY,
            visibility = visibility
        )

        val ankle = BodyLandmark(
            x = kneeX + (sin(radians) * segmentLength).toFloat(),
            y = kneeY - (cos(radians) * segmentLength).toFloat(),
            visibility = visibility
        )

        return LegPose(
            hip = hip,
            knee = knee,
            ankle = ankle
        )
    }
}
