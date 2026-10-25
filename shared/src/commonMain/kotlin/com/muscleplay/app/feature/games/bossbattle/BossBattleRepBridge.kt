
package com.muscleplay.app.feature.games.bossbattle

import com.muscleplay.app.feature.games.core.ExerciseState

/**
 * Converts cumulative detector repetitions into one-time boss damage events.
 */
class BossBattleRepBridge(
    private val engine: FitnessBossBattleEngine
) {
    private var previousRepetitions = 0

    fun start(currentRepetitions: Int = 0) {
        previousRepetitions = currentRepetitions.coerceAtLeast(0)
        engine.start()
    }

    fun onExerciseState(state: ExerciseState) {
        val currentRepetitions = state.repetitions.coerceAtLeast(0)

        if (currentRepetitions < previousRepetitions) {
            // The detector was reset; don't count old reps again.
            previousRepetitions = currentRepetitions
            return
        }

        val newRepetitions = currentRepetitions - previousRepetitions
        previousRepetitions = currentRepetitions

        repeat(newRepetitions) {
            engine.onValidRep()
        }
    }

    fun reset(currentRepetitions: Int = 0) {
        engine.reset()
        previousRepetitions = currentRepetitions.coerceAtLeast(0)
    }
}
