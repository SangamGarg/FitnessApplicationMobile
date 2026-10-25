package com.muscleplay.app.ml.liveness

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LivenessChallengeEvaluatorTest {
    private fun pose(timestamp: Long, raisedLeft: Boolean = false, raisedRight: Boolean = false) =
        LivenessPose(
            landmarks = mapOf(
                LivenessJoint.LEFT_SHOULDER to LivenessLandmark(0.4f, 0.3f, 0.99f),
                LivenessJoint.RIGHT_SHOULDER to LivenessLandmark(0.6f, 0.3f, 0.99f),
                LivenessJoint.LEFT_WRIST to LivenessLandmark(
                    0.3f,
                    if (raisedLeft) 0.1f else 0.55f,
                    0.99f
                ),
                LivenessJoint.RIGHT_WRIST to LivenessLandmark(
                    0.7f,
                    if (raisedRight) 0.1f else 0.55f,
                    0.99f
                ),
                LivenessJoint.LEFT_HIP to LivenessLandmark(0.43f, 0.6f, 0.99f),
                LivenessJoint.RIGHT_HIP to LivenessLandmark(0.57f, 0.6f, 0.99f),
            ),
            timestampMs = timestamp,
            frameCoverage = 0.4f
        )

    @Test
    fun `counter remains blocked until two challenges pass`() {
        val sequence = listOf(
            LivenessChallengeType.RAISE_LEFT_HAND,
            LivenessChallengeType.RAISE_RIGHT_HAND
        )
        var index = 0
        val evaluator = LivenessChallengeEvaluator(
            config = LivenessConfig(
                requiredStableFrames = 2,
                minimumFrameIntervalMs = 0L
            ),
            chooseChallenge = { sequence[index++] }
        )
        evaluator.start(0)
        assertFalse(evaluator.state.canCountRepetitions)

        evaluator.process(pose(1, raisedLeft = true), 1)
        evaluator.process(pose(2, raisedLeft = true), 2)
        assertFalse(evaluator.state.canCountRepetitions)

        evaluator.process(pose(3, raisedRight = true), 3)
        evaluator.process(pose(4, raisedRight = true), 4)
        assertTrue(evaluator.state.canCountRepetitions)
        assertEquals(LivenessStatus.VERIFIED, evaluator.state.status)
    }

    @Test
    fun `pose loss revokes verified state`() {
        val evaluator = LivenessChallengeEvaluator(
            config = LivenessConfig(requiredStableFrames = 1),
            chooseChallenge = { options -> options.first() }
        )
        evaluator.start(0)
        val first = evaluator.state.challenge!!.type
        val firstPose = when (first) {
            LivenessChallengeType.RAISE_LEFT_HAND -> pose(1, raisedLeft = true)
            LivenessChallengeType.RAISE_RIGHT_HAND -> pose(1, raisedRight = true)
            LivenessChallengeType.HOLD_NEUTRAL -> pose(1)
        }
        evaluator.process(firstPose, 1)
        val second = evaluator.state.challenge?.type
        if (second != null) {
            val secondPose = when (second) {
                LivenessChallengeType.RAISE_LEFT_HAND -> pose(2, raisedLeft = true)
                LivenessChallengeType.RAISE_RIGHT_HAND -> pose(2, raisedRight = true)
                LivenessChallengeType.HOLD_NEUTRAL -> pose(2)
            }
            evaluator.process(secondPose, 2)
        }
        if (evaluator.state.status == LivenessStatus.VERIFIED) {
            assertFalse(evaluator.onPoseLost().canCountRepetitions)
        }
    }

    @Test
    fun `rep gate blocks until verified`() {
        val gate = RepCountingGate()
        assertFalse(gate.mayProcessExercisePose())
        gate.update(LivenessState(status = LivenessStatus.VERIFIED, canCountRepetitions = true))
        assertTrue(gate.mayProcessExercisePose())
        gate.update(LivenessState(status = LivenessStatus.PAUSED, canCountRepetitions = false))
        assertFalse(gate.mayProcessExercisePose())
    }
}
