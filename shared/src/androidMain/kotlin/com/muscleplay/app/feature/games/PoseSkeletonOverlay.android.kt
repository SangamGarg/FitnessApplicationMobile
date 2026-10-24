package com.muscleplay.app.feature.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.muscleplay.app.ml.PoseOverlayLandmark

private val poseConnections = listOf(
    // Shoulders and arms
    11 to 12, 11 to 13, 13 to 15, 12 to 14, 14 to 16,

    // Torso
    11 to 23, 12 to 24, 23 to 24,

    // Left leg
    23 to 25, 25 to 27, 27 to 29, 29 to 31, 27 to 31,

    // Right leg
    24 to 26, 26 to 28, 28 to 30, 30 to 32, 28 to 32
)

@Composable
fun PoseSkeletonOverlay(
    landmarks: List<PoseOverlayLandmark>,
    modifier: Modifier = Modifier,
    mirrorHorizontally: Boolean = true,
    rotateLandmarks: Boolean = false,
    minimumVisibility: Float = 0.5f,
    skeletonColor: Color = Color(0xFF7CF5B4),
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        if (landmarks.isEmpty()) return@Canvas

        fun point(index: Int): Offset? {
            val landmark = landmarks.getOrNull(index) ?: return null

            if (landmark.visibility < minimumVisibility) {
                return null
            }

            var x = landmark.x
            var y = landmark.y

            // Keep the rotation setting that fixed your preview.
            if (rotateLandmarks) {
                val oldX = x
                x = y
                y = 1f - oldX
            }

            if (mirrorHorizontally) {
                x = 1f - x
            }

            return Offset(
                x = x * size.width, y = y * size.height
            )
        }

        // Draw bones.
        poseConnections.forEach { (startIndex, endIndex) ->
            val start = point(startIndex) ?: return@forEach
            val end = point(endIndex) ?: return@forEach

            drawLine(
                color = skeletonColor,
                start = start,
                end = end,
                strokeWidth = 8f,
                cap = StrokeCap.Round
            )
        }

        // Draw joints.
        landmarks.indices.forEach { index ->
            val center = point(index) ?: return@forEach

            drawCircle(
                color = Color.White, radius = 9f, center = center
            )

            drawCircle(
                color = skeletonColor, radius = 5f, center = center
            )
        }
    }
}
