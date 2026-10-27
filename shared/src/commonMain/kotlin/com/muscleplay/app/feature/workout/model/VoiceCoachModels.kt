package com.muscleplay.app.feature.workout.model

enum class CoachPersona(
    val displayName: String,
    val description: String,
    val iconEmoji: String
) {
    MOTIVATIONAL("Motivational Coach", "Positive & encouraging guidance", "💪"),
    CALM("Calm Trainer", "Mindful & steady tempo cues", "🧘‍♂️"),
    HYPE_BEAST("Hype Beast", "High energy & intense motivation", "🔥")
}

enum class TempoPhase(
    val label: String,
    val instruction: String,
    val durationSec: Int
) {
    ECCENTRIC("Lowering", "2s Steady Down", 2),
    HOLD("Pause", "1s Hold at Bottom", 1),
    CONCENTRIC("Explode Up", "1s Power Up", 1)
}

data class VoiceCoachSettings(
    val isVoiceEnabled: Boolean = true,
    val persona: CoachPersona = CoachPersona.MOTIVATIONAL,
    val isMetronomeEnabled: Boolean = true,
    val isHapticPulseEnabled: Boolean = true
)

object VoiceCueGenerator {

    fun getRepAnnouncement(repCount: Int, goalReps: Int, persona: CoachPersona): String {
        val remaining = (goalReps - repCount).coerceAtLeast(0)
        return when (persona) {
            CoachPersona.MOTIVATIONAL -> when {
                repCount == 1 -> "Rep 1! Great start!"
                repCount == goalReps / 2 -> "Halfway there! Keep your form solid!"
                remaining == 2 -> "2 reps left! Finish strong!"
                repCount >= goalReps -> "Set complete! Excellent work!"
                else -> "Rep $repCount!"
            }
            CoachPersona.CALM -> when {
                repCount == 1 -> "Rep 1. Control your breath."
                repCount == goalReps / 2 -> "Halfway. Smooth, steady motion."
                remaining == 2 -> "Two reps remaining. Focus."
                repCount >= goalReps -> "Set complete. Rest and recover."
                else -> "Rep $repCount."
            }
            CoachPersona.HYPE_BEAST -> when {
                repCount == 1 -> "REP 1! LET'S GO!"
                repCount == goalReps / 2 -> "HALFWAY! DON'T STOP NOW!"
                remaining == 2 -> "2 MORE REPS! LIGHT WEIGHT BABY!"
                repCount >= goalReps -> "BOOM! SET DESTROYED!"
                else -> "REP $repCount!"
            }
        }
    }

    fun getFormFeedbackCue(feedback: String, persona: CoachPersona): String {
        return when (persona) {
            CoachPersona.MOTIVATIONAL -> "Tip: $feedback. You're doing great!"
            CoachPersona.CALM -> "Adjustment: $feedback. Keep it smooth."
            CoachPersona.HYPE_BEAST -> "LISTEN UP! $feedback! PUSH HARDER!"
        }
    }
}
