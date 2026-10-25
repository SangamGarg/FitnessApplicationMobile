package com.muscleplay.app.ml.liveness

enum class LivenessStatus {
    NOT_STARTED,
    CHALLENGE_REQUIRED,
    CHALLENGE_IN_PROGRESS,
    VERIFIED,
    PAUSED,
    FAILED
}

enum class LivenessChallengeType {
    RAISE_LEFT_HAND,
    RAISE_RIGHT_HAND,
    HOLD_NEUTRAL
}

data class LivenessChallenge(
    val type: LivenessChallengeType,
    val instruction: String,
)

data class LivenessState(
    val status: LivenessStatus = LivenessStatus.NOT_STARTED,
    val challenge: LivenessChallenge? = null,
    val instruction: String = "Prepare to verify your movement",
    val canCountRepetitions: Boolean = false,
    val attemptsRemaining: Int = 3,
    val reason: String? = null,
)

data class LivenessConfig(
    val minimumVisibility: Float = 0.6f,
    val minimumFrameCoverage: Float = 0.15f,
    val maximumFrameCoverage: Float = 0.95f,
    val requiredStableFrames: Int = 8,
    val minimumFrameIntervalMs: Long = 50L,
    val challengeTimeoutMs: Long = 20_000L,
    val maxAttempts: Int = 3,
) {
    init {
        require(minimumVisibility in 0f..1f)
        require(minimumFrameCoverage in 0f..1f)
        require(maximumFrameCoverage in minimumFrameCoverage..1f)
        require(requiredStableFrames > 0)
        require(minimumFrameIntervalMs >= 0L)
        require(challengeTimeoutMs > 0L)
        require(maxAttempts > 0)
    }
}

enum class LivenessJoint {
    LEFT_SHOULDER,
    RIGHT_SHOULDER,
    LEFT_WRIST,
    RIGHT_WRIST,
    LEFT_HIP,
    RIGHT_HIP
}

data class LivenessLandmark(
    val x: Float,
    val y: Float,
    val visibility: Float,
)

data class LivenessPose(
    val landmarks: Map<LivenessJoint, LivenessLandmark>,
    val timestampMs: Long,
    val frameCoverage: Float? = null,
) {
    fun joint(
        joint: LivenessJoint,
        minimumVisibility: Float = 0.5f,
    ): LivenessLandmark? {
        return landmarks[joint]?.takeIf {
            it.visibility >= minimumVisibility
        }
    }
}
