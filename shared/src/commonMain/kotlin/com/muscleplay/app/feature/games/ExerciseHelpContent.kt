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
        title = "Squat Guide", sections = listOf(
            ExerciseHelpSection(
                heading = "1. Starting Position", instructions = listOf(
                    "Stand with your feet approximately shoulder-width apart.",
                    "Keep your toes slightly outward if comfortable.",
                    "Keep your chest lifted and back in a comfortable neutral position.",
                    "Make sure your hips, knees and ankles are visible to the camera."
                )
            ), ExerciseHelpSection(
                heading = "2. How to Perform a Squat", instructions = listOf(
                    "Begin by standing upright.",
                    "Push your hips backward and bend your knees.",
                    "Keep your heels on the floor and knees aligned with your toes.",
                    "Lower only as far as you can comfortably maintain control.",
                    "Push through your feet to return to standing.",
                    "Stand upright to complete the repetition."
                )
            ), ExerciseHelpSection(
                heading = "3. Common Mistakes", instructions = listOf(
                    "Avoid letting your knees collapse inward.",
                    "Avoid lifting your heels off the floor.",
                    "Do not rush or bounce at the bottom.",
                    "Do not force a depth that causes pain."
                )
            ), ExerciseHelpSection(
                heading = "4. Skeleton Color Guide", instructions = listOf(
                    "Green: The pose is reliable and the tracker recognizes the standing phase.",
                    "Yellow: Waiting for the starting position or tracking an active squat.",
                    "Red: The tracker considers the pose unreliable.",
                    "You can toggle the skeleton from the exercise options panel. Rep counting continues when it is hidden."
                )
            ), ExerciseHelpSection(
                heading = "5. Liveness Verification", instructions = listOf(
                    "Complete the on-screen verification instructions before repetition counting begins.",
                    "Keep your body visible and follow the current instruction shown on the screen.",
                    "Move naturally and deliberately so the camera can track your pose.",
                    "If verification pauses, first move back into the camera frame and make sure your required landmarks are visible.",
                    "If verification fails or pauses, raise your right hand above your right shoulder and hold it there for about 2-3 seconds to restart verification.",
                    "You can also tap the Revalidate button when it is displayed.",
                    "Repetitions are counted only after verification succeeds and repetition counting is enabled."
                )
            ), ExerciseHelpSection(
                heading = "6. Camera Tips", instructions = listOf(
                    "Keep your full body, including your feet, in the camera frame.",
                    "Use enough lighting for pose detection.",
                    "Position your phone securely before exercising."
                )
            ), ExerciseHelpSection(
                heading = "7. Safety Tips", instructions = listOf(
                    "Start slowly and use a comfortable range of motion.",
                    "Stop if you experience pain, dizziness or unusual discomfort.",
                    "Consider guidance from a qualified fitness professional if you are new to squats or have an injury."
                )
            )
        )
    )

    val pushUp = ExerciseHelpContent(
        title = "Push-up Guide", sections = listOf(
            ExerciseHelpSection(
                heading = "1. Starting Position", instructions = listOf(
                    "Start in a high plank position with your hands slightly wider than shoulder-width apart.",
                    "Keep your shoulders, elbows and wrists visible to the camera.",
                    "Keep your body aligned from your head through your hips to your heels.",
                    "Position the camera at a side angle so your arm movement is easier to track.",
                    "Straighten your arms to establish the starting position."
                )
            ), ExerciseHelpSection(
                heading = "2. How to Perform a Push-up", instructions = listOf(
                    "Begin with your arms straight and your body aligned.",
                    "Bend your elbows to lower your chest toward the floor with control.",
                    "Keep your elbows at a comfortable angle relative to your torso.",
                    "Lower yourself only as far as you can comfortably maintain good form.",
                    "Push through your hands to straighten your arms again.",
                    "Return to the top position to complete one repetition."
                )
            ), ExerciseHelpSection(
                heading = "3. Common Mistakes", instructions = listOf(
                    "Avoid letting your hips sag or rise excessively.",
                    "Avoid flaring your elbows excessively outward.",
                    "Do not drop your body suddenly or bounce at the bottom.",
                    "Avoid shortening the movement just to increase your repetition count.",
                    "Do not force a range of motion that causes pain."
                )
            ), ExerciseHelpSection(
                heading = "4. Skeleton Color Guide", instructions = listOf(
                    "Green: The pose is reliable and the tracker recognizes the ready position.",
                    "Yellow: Waiting for the starting position or tracking an active push-up.",
                    "Red: The tracker considers the pose unreliable.",
                    "Keep your shoulder, elbow and wrist visible for more reliable arm-angle detection.",
                    "You can toggle the skeleton from the exercise options panel. Rep counting continues when it is hidden."
                )
            ), ExerciseHelpSection(
                heading = "5. Liveness Verification", instructions = listOf(
                    "Complete the on-screen verification instructions before repetition counting begins.",
                    "Keep your upper body and arms visible while following the instruction shown on the screen.",
                    "Move naturally and deliberately so the camera can track your pose.",
                    "If verification pauses, move back into the camera frame and make sure your shoulder, elbow and wrist are visible.",
                    "If verification fails or pauses, raise your right hand above your right shoulder and hold it there for about 2-3 seconds to restart verification.",
                    "You can also tap the Revalidate button when it is displayed.",
                    "Repetitions are counted only after verification succeeds and repetition counting is enabled."
                )
            ), ExerciseHelpSection(
                heading = "6. Camera Tips", instructions = listOf(
                    "Use a side-on camera position for clearer elbow-angle measurements.",
                    "Keep your upper body and arms inside the camera frame.",
                    "Use sufficient lighting for pose detection.",
                    "Place your phone securely before starting your workout.",
                    "If tracking is unreliable, adjust your position until your arm landmarks are visible."
                )
            ), ExerciseHelpSection(
                heading = "7. Safety Tips", instructions = listOf(
                    "Start with a variation that matches your current fitness level.",
                    "Perform each repetition slowly and with control.",
                    "Stop if you experience pain, dizziness or unusual discomfort.",
                    "Consider knee push-ups or wall push-ups if standard push-ups are too challenging.",
                    "Seek guidance from a qualified fitness professional if you are new to push-ups or have an injury."
                )
            )
        )
    )
}
