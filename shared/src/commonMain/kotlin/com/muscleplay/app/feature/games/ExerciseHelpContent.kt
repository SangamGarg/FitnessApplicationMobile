package com.muscleplay.app.feature.games

data class ExerciseHelpSection(
    val heading: String,
    val instructions: List<String>,
)

data class ExerciseHelpContent(
    val title: String,
    val sections: List<ExerciseHelpSection>,
)

object ExerciseHelpContents {

    val squat = ExerciseHelpContent(
        title = "Squat Guide",
        sections = listOf(
            ExerciseHelpSection(
                heading = "1. Starting Position",
                instructions = listOf(
                    "Stand with your feet approximately shoulder-width apart.",
                    "Keep your toes slightly outward if comfortable.",
                    "Keep your chest lifted and back in a comfortable neutral position.",
                    "Make sure your hips, knees and ankles are visible to the camera."
                )
            ),
            ExerciseHelpSection(
                heading = "2. How to Perform a Squat",
                instructions = listOf(
                    "Begin by standing upright.",
                    "Push your hips backward and bend your knees.",
                    "Keep your heels on the floor and knees aligned with your toes.",
                    "Lower only as far as you can comfortably maintain control.",
                    "Push through your feet to return to standing.",
                    "Stand upright to complete the repetition."
                )
            ),
            ExerciseHelpSection(
                heading = "3. Common Mistakes",
                instructions = listOf(
                    "Avoid letting your knees collapse inward.",
                    "Avoid lifting your heels off the floor.",
                    "Do not rush or bounce at the bottom.",
                    "Do not force a depth that causes pain."
                )
            ),
            ExerciseHelpSection(
                heading = "4. Skeleton Color Guide",
                instructions = listOf(
                    "Green: The pose is reliable and the tracker recognizes the standing phase.",
                    "Yellow: Waiting for the starting position or tracking an active squat.",
                    "Red: The tracker considers the pose unreliable.",
                    "You can toggle the skeleton from the exercise options panel. Rep counting continues when it is hidden."
                )
            ),
            ExerciseHelpSection(
                heading = "5. Camera Tips",
                instructions = listOf(
                    "Keep your full body, including your feet, in the camera frame.",
                    "Use enough lighting for pose detection.",
                    "Position your phone securely before exercising."
                )
            ),
            ExerciseHelpSection(
                heading = "6. Safety Tips",
                instructions = listOf(
                    "Start slowly and use a comfortable range of motion.",
                    "Stop if you experience pain, dizziness or unusual discomfort.",
                    "Consider guidance from a qualified fitness professional if you are new to squats or have an injury."
                )
            )
        )
    )
}
