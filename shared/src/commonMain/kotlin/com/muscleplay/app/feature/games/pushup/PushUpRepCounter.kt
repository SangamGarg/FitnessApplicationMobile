
package com.muscleplay.app.feature.games.pushup

enum class PushUpRepPhase {
    WAITING_FOR_TOP,
    TOP,
    BOTTOM
}

data class PushUpRepState(
    val repetitions: Int = 0,
    val phase: PushUpRepPhase = PushUpRepPhase.WAITING_FOR_TOP,
    val elbowAngleDegrees: Float? = null,
    val isPoseReliable: Boolean = false
)

class PushUpRepCounter(
    private val topAngleThreshold: Float = 150f,
    private val bottomAngleThreshold: Float = 95f,
    private val stableFramesRequired: Int = 3
) {
    private var repetitions = 0
    private var phase = PushUpRepPhase.WAITING_FOR_TOP
    private var stableFrames = 0
    private var lastAngle: Float? = null

    var state: PushUpRepState = buildState(
        angle = null,
        reliable = false
    )
        private set

    init {
        require(topAngleThreshold > bottomAngleThreshold) {
            "Top angle threshold must be greater than bottom angle threshold."
        }
        require(bottomAngleThreshold >= 0f) {
            "Bottom angle threshold cannot be negative."
        }
        require(topAngleThreshold <= 180f) {
            "Top angle threshold cannot exceed 180 degrees."
        }
        require(stableFramesRequired > 0) {
            "Stable frames required must be greater than zero."
        }
    }

    fun processAngle(angleDegrees: Float): PushUpRepState {
        require(angleDegrees.isFinite() && angleDegrees in 0f..180f) {
            "Elbow angle must be finite and between 0 and 180 degrees."
        }

        lastAngle = angleDegrees

        when (phase) {
            PushUpRepPhase.WAITING_FOR_TOP -> {
                if (angleDegrees >= topAngleThreshold) {
                    stableFrames++

                    if (stableFrames >= stableFramesRequired) {
                        phase = PushUpRepPhase.TOP
                        stableFrames = 0
                    }
                } else {
                    stableFrames = 0
                }
            }

            PushUpRepPhase.TOP -> {
                if (angleDegrees <= bottomAngleThreshold) {
                    stableFrames++

                    if (stableFrames >= stableFramesRequired) {
                        phase = PushUpRepPhase.BOTTOM
                        stableFrames = 0
                    }
                } else {
                    stableFrames = 0
                }
            }

            PushUpRepPhase.BOTTOM -> {
                if (angleDegrees >= topAngleThreshold) {
                    stableFrames++

                    if (stableFrames >= stableFramesRequired) {
                        repetitions++
                        phase = PushUpRepPhase.TOP
                        stableFrames = 0
                    }
                } else {
                    stableFrames = 0
                }
            }
        }

        state = buildState(
            angle = angleDegrees,
            reliable = true
        )
        return state
    }

    /**
     * Keeps completed repetitions but requires the user to
     * establish the top position again after pose loss.
     */
    fun onPoseLost(): PushUpRepState {
        phase = PushUpRepPhase.WAITING_FOR_TOP
        stableFrames = 0
        lastAngle = null

        state = buildState(
            angle = null,
            reliable = false
        )
        return state
    }

    fun reset(): PushUpRepState {
        repetitions = 0
        phase = PushUpRepPhase.WAITING_FOR_TOP
        stableFrames = 0
        lastAngle = null

        state = buildState(
            angle = null,
            reliable = false
        )
        return state
    }

    private fun buildState(
        angle: Float?,
        reliable: Boolean
    ): PushUpRepState {
        return PushUpRepState(
            repetitions = repetitions,
            phase = phase,
            elbowAngleDegrees = angle,
            isPoseReliable = reliable
        )
    }
}
