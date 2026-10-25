package com.muscleplay.app.ml.liveness

/**
 * Pure Kotlin evaluator. It reduces replay of a fixed clip by requiring a randomly
 * selected action, but pose-only checks cannot prove that a person is live.
 */
class LivenessChallengeEvaluator(
    private val config: LivenessConfig = LivenessConfig(),
    private val chooseChallenge: (List<LivenessChallengeType>) -> LivenessChallengeType = { options ->
        options.random()
    },
) {
    var state: LivenessState = LivenessState(attemptsRemaining = config.maxAttempts)
        private set

    private var challengeStartedAtMs: Long? = null
    private var lastFrameTimestampMs: Long? = null
    private var stableFrames = 0
    private var neutralStableFrames = 0
    private val usedChallenges = mutableSetOf<LivenessChallengeType>()

    fun start(nowMs: Long) {
        require(nowMs >= 0L)
        usedChallenges.clear()
        challengeStartedAtMs = null
        lastFrameTimestampMs = null
        stableFrames = 0
        neutralStableFrames = 0
        state = LivenessState(attemptsRemaining = config.maxAttempts)
        issueNextChallenge(nowMs)
    }

    /** Alias intended for an in-screen retry button. Resets all verification progress. */
    fun restart(nowMs: Long): LivenessState {
        start(nowMs)
        return state
    }

    fun process(pose: LivenessPose?, nowMs: Long): LivenessState {
        if (state.status !in setOf(
                LivenessStatus.CHALLENGE_REQUIRED,
                LivenessStatus.CHALLENGE_IN_PROGRESS
            )
        ) return state

        val started = challengeStartedAtMs ?: nowMs
        if (nowMs - started > config.challengeTimeoutMs) {
            return failAttempt("Challenge timed out", nowMs)
        }

        if (pose == null) {
            stableFrames = 0
            neutralStableFrames = 0
            state = state.copy(
                status = LivenessStatus.CHALLENGE_IN_PROGRESS,
                canCountRepetitions = false,
                reason = "No pose detected"
            )
            return state
        }

        val previousTimestamp = lastFrameTimestampMs
        if (previousTimestamp != null && pose.timestampMs <= previousTimestamp) {
            state = state.copy(reason = "Waiting for a new camera frame")
            return state
        }
        if (previousTimestamp != null &&
            pose.timestampMs - previousTimestamp < config.minimumFrameIntervalMs
        ) {
            return state
        }
        lastFrameTimestampMs = pose.timestampMs

        if (!isPoseUsable(pose)) {
            stableFrames = 0
            neutralStableFrames = 0
            state = state.copy(
                status = LivenessStatus.CHALLENGE_IN_PROGRESS,
                canCountRepetitions = false,
                reason = "Move farther into view and keep your shoulders, wrists and hips visible"
            )
            return state
        }

        val challenge = state.challenge ?: return state
        val passed = when (challenge.type) {
            LivenessChallengeType.RAISE_LEFT_HAND -> isHandRaised(pose, left = true)
            LivenessChallengeType.RAISE_RIGHT_HAND -> isHandRaised(pose, left = false)
            LivenessChallengeType.HOLD_NEUTRAL -> isNeutralPose(pose)
        }

        if (passed) stableFrames++ else stableFrames = 0
        state = state.copy(
            status = LivenessStatus.CHALLENGE_IN_PROGRESS,
            canCountRepetitions = false,
            reason = if (passed) "Hold the position" else null
        )

        if (stableFrames >= config.requiredStableFrames) {
            usedChallenges += challenge.type
            stableFrames = 0
            if (usedChallenges.size >= 2) {
                state = LivenessState(
                    status = LivenessStatus.VERIFIED,
                    instruction = "Verification complete. Start your exercise.",
                    canCountRepetitions = true,
                    attemptsRemaining = config.maxAttempts
                )
            } else {
                issueNextChallenge(nowMs)
            }
        }
        return state
    }

    /**
     * Call when camera tracking is interrupted. A lost pose during verification resets
     * consecutive-frame progress, but does not consume an attempt by itself. A terminal
     * FAILED or PAUSED state remains terminal until the user taps Revalidate.
     */
    fun onPoseLost(): LivenessState {
        stableFrames = 0
        neutralStableFrames = 0

        when (state.status) {
            LivenessStatus.VERIFIED -> {
                state = LivenessState(
                    status = LivenessStatus.PAUSED,
                    instruction = "Tracking interrupted. Verify again to resume.",
                    canCountRepetitions = false,
                    attemptsRemaining = config.maxAttempts,
                    reason = "Pose tracking was lost"
                )
            }

            LivenessStatus.CHALLENGE_REQUIRED,
            LivenessStatus.CHALLENGE_IN_PROGRESS,
                -> {
                state = state.copy(
                    status = LivenessStatus.CHALLENGE_IN_PROGRESS,
                    canCountRepetitions = false,
                    reason = "Pose tracking was lost"
                )
            }

            // Do not turn FAILED into CHALLENGE_IN_PROGRESS when another null frame arrives.
            LivenessStatus.NOT_STARTED,
            LivenessStatus.PAUSED,
            LivenessStatus.FAILED,
                -> Unit
        }
        return state
    }

    private fun issueNextChallenge(nowMs: Long) {
        val available = LivenessChallengeType.values().filterNot { it in usedChallenges }
        if (available.isEmpty()) {
            state = LivenessState(
                status = LivenessStatus.VERIFIED,
                instruction = "Verification complete. Start your exercise.",
                canCountRepetitions = true,
                attemptsRemaining = config.maxAttempts
            )
            return
        }

        val selected = chooseChallenge(available)
        challengeStartedAtMs = nowMs
        lastFrameTimestampMs = null
        stableFrames = 0
        neutralStableFrames = 0

        val instruction = when (selected) {
            LivenessChallengeType.RAISE_LEFT_HAND -> "Raise your LEFT hand and hold it up"
            LivenessChallengeType.RAISE_RIGHT_HAND -> "Raise your RIGHT hand and hold it up"
            LivenessChallengeType.HOLD_NEUTRAL -> "Stand upright and hold still"
        }

        state = LivenessState(
            status = LivenessStatus.CHALLENGE_REQUIRED,
            challenge = LivenessChallenge(selected, instruction),
            instruction = instruction,
            canCountRepetitions = false,
            attemptsRemaining = state.attemptsRemaining.takeIf { it > 0 } ?: config.maxAttempts
        )
    }

    private fun failAttempt(reason: String, nowMs: Long): LivenessState {
        val remaining = state.attemptsRemaining - 1
        stableFrames = 0
        neutralStableFrames = 0

        if (remaining <= 0) {
            state = LivenessState(
                status = LivenessStatus.FAILED,
                instruction = "Verification failed. Tap Revalidate to try again.",
                canCountRepetitions = false,
                attemptsRemaining = 0,
                reason = reason
            )
        } else {
            usedChallenges.clear()
            state = LivenessState(
                status = LivenessStatus.CHALLENGE_REQUIRED,
                attemptsRemaining = remaining,
                canCountRepetitions = false,
                reason = reason
            )
            issueNextChallenge(nowMs)
            state = state.copy(attemptsRemaining = remaining, reason = reason)
        }
        return state
    }

    private fun isPoseUsable(pose: LivenessPose): Boolean {
        val required = listOf(
            LivenessJoint.LEFT_SHOULDER,
            LivenessJoint.RIGHT_SHOULDER,
            LivenessJoint.LEFT_WRIST,
            LivenessJoint.RIGHT_WRIST,
            LivenessJoint.LEFT_HIP,
            LivenessJoint.RIGHT_HIP
        )
        if (required.any { pose.joint(it, config.minimumVisibility) == null }) return false

        val coverage = pose.frameCoverage
        if (coverage != null &&
            (coverage < config.minimumFrameCoverage || coverage > config.maximumFrameCoverage)
        ) return false

        return true
    }

    private fun isHandRaised(pose: LivenessPose, left: Boolean): Boolean {
        val wrist = pose.joint(
            if (left) LivenessJoint.LEFT_WRIST else LivenessJoint.RIGHT_WRIST,
            config.minimumVisibility
        ) ?: return false
        val shoulder = pose.joint(
            if (left) LivenessJoint.LEFT_SHOULDER else LivenessJoint.RIGHT_SHOULDER,
            config.minimumVisibility
        ) ?: return false

        // Normalized image y grows downward, so a smaller y is higher in the frame.
        return wrist.y < shoulder.y - 0.04f
    }

    private fun isNeutralPose(pose: LivenessPose): Boolean {
        val leftShoulder =
            pose.joint(LivenessJoint.LEFT_SHOULDER, config.minimumVisibility) ?: return false
        val rightShoulder =
            pose.joint(LivenessJoint.RIGHT_SHOULDER, config.minimumVisibility) ?: return false
        val leftHip = pose.joint(LivenessJoint.LEFT_HIP, config.minimumVisibility) ?: return false
        val rightHip = pose.joint(LivenessJoint.RIGHT_HIP, config.minimumVisibility) ?: return false
        val leftWrist =
            pose.joint(LivenessJoint.LEFT_WRIST, config.minimumVisibility) ?: return false
        val rightWrist =
            pose.joint(LivenessJoint.RIGHT_WRIST, config.minimumVisibility) ?: return false

        val shouldersY = (leftShoulder.y + rightShoulder.y) / 2f
        val hipsY = (leftHip.y + rightHip.y) / 2f
        val wristsY = (leftWrist.y + rightWrist.y) / 2f
        return hipsY > shouldersY && wristsY > shouldersY
    }
}
