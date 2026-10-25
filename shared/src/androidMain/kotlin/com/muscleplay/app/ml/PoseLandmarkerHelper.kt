
package com.muscleplay.app.ml

import android.content.Context
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

data class PoseOverlayLandmark(
    val x: Float,
    val y: Float,
    val visibility: Float,
)

class PoseLandmarkerHelper(
    context: Context,
    private val onPoseDetected: (PoseSample?) -> Unit,
    private val onError: (String) -> Unit,
    private val onLandmarksDetected: (List<PoseOverlayLandmark>?) -> Unit = {},
) {
    private val landmarker: PoseLandmarker

    init {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("pose_landmarker_lite.task")
            .build()

        val options = PoseLandmarker.PoseLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setNumPoses(1)
            .setResultListener { result: PoseLandmarkerResult, _: MPImage ->
                val landmarks = result.landmarks().firstOrNull()

                if (landmarks == null) {
                    onPoseDetected(null)
                    onLandmarksDetected(null)
                    return@setResultListener
                }

                // Convert the MediaPipe result into the shared KMP format.
                onPoseDetected(
                    AndroidPoseAdapter.toPoseSample(
                        result = result,
                        timestampMs = result.timestampMs(),
                    )
                )

                // Keep overlay conversion independent of exercise processing.
                onLandmarksDetected(
                    landmarks.map { landmark ->
                        PoseOverlayLandmark(
                            x = landmark.x(),
                            y = landmark.y(),
                            visibility = landmark.visibility().orElse(0f),
                        )
                    }
                )
            }
            .setErrorListener { error ->
                onError(error.message ?: "Pose detection failed")
            }
            .build()

        landmarker = PoseLandmarker.createFromOptions(
            context.applicationContext,
            options,
        )
    }

    fun detect(image: MPImage, timestampMs: Long) {
        landmarker.detectAsync(image, timestampMs)
    }

    fun close() {
        landmarker.close()
    }
}
