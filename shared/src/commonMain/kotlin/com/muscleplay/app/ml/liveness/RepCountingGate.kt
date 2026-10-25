package com.muscleplay.app.ml.liveness

/** Small shared gate to ensure exercise detectors cannot count before liveness verification. */
class RepCountingGate {
    var isEnabled: Boolean = false
        private set

    fun update(state: LivenessState) {
        isEnabled = state.status == LivenessStatus.VERIFIED && state.canCountRepetitions
    }

    /** Call before forwarding a pose to any squat/push-up/jumping-jack detector. */
    fun mayProcessExercisePose(): Boolean = isEnabled

    fun block() {
        isEnabled = false
    }
}
