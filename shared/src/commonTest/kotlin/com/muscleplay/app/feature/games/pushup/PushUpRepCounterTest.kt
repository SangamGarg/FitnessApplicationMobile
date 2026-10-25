
package com.muscleplay.app.feature.games.pushup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class PushUpRepCounterTest {

    @Test
    fun countsOneRepAfterTopBottomTopSequence() {
        val counter = PushUpRepCounter()

        repeat(3) { counter.processAngle(165f) }
        assertEquals(PushUpRepPhase.TOP, counter.state.phase)

        repeat(3) { counter.processAngle(85f) }
        assertEquals(PushUpRepPhase.BOTTOM, counter.state.phase)
        assertEquals(0, counter.state.repetitions)

        repeat(3) { counter.processAngle(165f) }
        assertEquals(1, counter.state.repetitions)
        assertEquals(PushUpRepPhase.TOP, counter.state.phase)
    }

    @Test
    fun doesNotCountIncompleteRep() {
        val counter = PushUpRepCounter()

        repeat(3) { counter.processAngle(165f) }
        repeat(3) { counter.processAngle(85f) }

        assertEquals(0, counter.state.repetitions)
    }

    @Test
    fun requiresThreeStableFramesAtEachPosition() {
        val counter = PushUpRepCounter()

        counter.processAngle(165f)
        counter.processAngle(165f)

        assertEquals(
            PushUpRepPhase.WAITING_FOR_TOP,
            counter.state.phase
        )

        counter.processAngle(165f)

        assertEquals(
            PushUpRepPhase.TOP,
            counter.state.phase
        )
    }

    @Test
    fun interruptedMovementDoesNotAdvancePhase() {
        val counter = PushUpRepCounter()

        repeat(3) { counter.processAngle(165f) }

        counter.processAngle(85f)
        counter.processAngle(85f)
        counter.processAngle(120f)

        assertEquals(PushUpRepPhase.TOP, counter.state.phase)

        repeat(3) { counter.processAngle(85f) }

        assertEquals(PushUpRepPhase.BOTTOM, counter.state.phase)
    }

    @Test
    fun poseLossPreservesRepsAndRequiresTopPositionAgain() {
        val counter = PushUpRepCounter()

        repeat(3) { counter.processAngle(165f) }
        repeat(3) { counter.processAngle(85f) }
        repeat(3) { counter.processAngle(165f) }

        assertEquals(1, counter.state.repetitions)

        val lostState = counter.onPoseLost()

        assertEquals(1, lostState.repetitions)
        assertEquals(
            PushUpRepPhase.WAITING_FOR_TOP,
            lostState.phase
        )
        assertFalse(lostState.isPoseReliable)
        assertNull(lostState.elbowAngleDegrees)

        repeat(3) { counter.processAngle(85f) }

        assertEquals(1, counter.state.repetitions)
        assertEquals(
            PushUpRepPhase.WAITING_FOR_TOP,
            counter.state.phase
        )

        repeat(3) { counter.processAngle(165f) }

        assertEquals(PushUpRepPhase.TOP, counter.state.phase)
        assertEquals(1, counter.state.repetitions)
    }

    @Test
    fun resetClearsRepetitionsAndState() {
        val counter = PushUpRepCounter()

        repeat(3) { counter.processAngle(165f) }
        repeat(3) { counter.processAngle(85f) }
        repeat(3) { counter.processAngle(165f) }

        val state = counter.reset()

        assertEquals(0, state.repetitions)
        assertEquals(
            PushUpRepPhase.WAITING_FOR_TOP,
            state.phase
        )
        assertFalse(state.isPoseReliable)
        assertNull(state.elbowAngleDegrees)
    }

    @Test
    fun rejectsInvalidAngles() {
        val counter = PushUpRepCounter()

        assertFailsWith<IllegalArgumentException> {
            counter.processAngle(-1f)
        }

        assertFailsWith<IllegalArgumentException> {
            counter.processAngle(181f)
        }

        assertFailsWith<IllegalArgumentException> {
            counter.processAngle(Float.NaN)
        }

        assertEquals(0, counter.state.repetitions)
    }
}