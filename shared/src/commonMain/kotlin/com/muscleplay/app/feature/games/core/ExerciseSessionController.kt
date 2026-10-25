
package com.muscleplay.app.feature.games.core

import com.muscleplay.app.ml.PoseSample

class ExerciseSessionController(
    private val detector: ExerciseDetector
) {
    var state: ExerciseState = ExerciseState()
        private set

    var isRunning: Boolean = false
        private set

    fun start() {
        isRunning = true
    }

    fun processPose(pose: PoseSample): ExerciseState {
        if (!isRunning) return state

        state = detector.processPose(pose)
        return state
    }

    fun pause() {
        isRunning = false
    }

    fun resume() {
        isRunning = true
    }

    fun reset() {
        state = detector.reset()
    }

    fun stop() {
        isRunning = false
        state = detector.reset()
    }
}
