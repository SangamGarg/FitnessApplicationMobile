package com.muscleplay.app.feature.games.pushup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.muscleplay.app.feature.games.camera.ExerciseCameraConfigs
import com.muscleplay.app.feature.games.camera.ExerciseCameraScreen

@Composable
actual fun PushUpCameraScreen(onBack: () -> Unit) {
    ExerciseCameraScreen(
        detector = remember { PushUpExerciseDetector() },
        config = ExerciseCameraConfigs.PushUp,
        onBack = onBack
    )
}
