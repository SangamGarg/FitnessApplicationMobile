
package com.muscleplay.app.feature.games.squat

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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

        // Bend the knees and move the hips downward.
        repeat(15) {
            counter.processFrame(
                frame(
                    angleDegrees = 80f,
                    hipDescent = 0.08f
                )
            )
        }

        // Return to the original standing position.
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

        // Descend, but never return to standing.
        repeat(15) {
            counter.processFrame(
                frame(
                    angleDegrees = 80f,
                    hipDescent = 0.08f
                )
            )
        }

        assertEquals(0, counter.state.repetitions)
    }

    @Test
    fun repeatedStandingFramesDoNotCreateExtraRepetitions() {
        repeat(15) {
            counter.processFrame(frame(angleDegrees = 170f))
        }

        repeat(15) {
            counter.processFrame(
                frame(
                    angleDegrees = 80f,
                    hipDescent = 0.08f
                )
            )
        }

        repeat(20) {
            counter.processFrame(frame(angleDegrees = 170f))
        }

        // Staying upright must not count another repetition.
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
            counter.processFrame(
                frame(
                    angleDegrees = 80f,
                    hipDescent = 0.08f
                )
            )
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
        hipDescent: Float = 0f
    ): PoseFrame {
        return PoseFrame(
            leftLeg = leg(angleDegrees, visibility, hipDescent),
            rightLeg = leg(angleDegrees, visibility, hipDescent)
        )
    }

    private fun leg(
        angleDegrees: Float,
        visibility: Float,
        hipDescent: Float
    ): LegPose {
        val radians = angleDegrees.toDouble() * PI / 180.0
        val kneeX = 0.5f
        val kneeY = 0.5f
        val segmentLength = 0.2

        // Shift the entire leg down to simulate hip descent
        // without changing the constructed knee angle.
        val hip = BodyLandmark(
            x = kneeX,
            y = kneeY - segmentLength.toFloat() + hipDescent,
            visibility = visibility
        )

        val knee = BodyLandmark(
            x = kneeX,
            y = kneeY + hipDescent,
            visibility = visibility
        )

        val ankle = BodyLandmark(
            x = kneeX + (sin(radians) * segmentLength).toFloat(),
            y = kneeY - (cos(radians) * segmentLength).toFloat()
                    + hipDescent,
            visibility = visibility
        )

        return LegPose(
            hip = hip,
            knee = knee,
            ankle = ankle
        )
    }
}