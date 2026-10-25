package com.muscleplay.app.feature.games.squat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.muscleplay.app.feature.games.camera.ExerciseCameraConfigs
import com.muscleplay.app.feature.games.camera.ExerciseCameraScreen

@Composable
actual fun SquatCameraScreen(onBack: () -> Unit) {
    ExerciseCameraScreen(
        detector = remember { SquatExerciseDetector() },
        config = ExerciseCameraConfigs.Squat,
        onBack = onBack
    )
}
