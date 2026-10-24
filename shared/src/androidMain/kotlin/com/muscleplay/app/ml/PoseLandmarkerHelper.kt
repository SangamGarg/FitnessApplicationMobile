package com.muscleplay.app.ml

import android.content.Context
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import com.muscleplay.app.feature.games.BodyLandmark
import com.muscleplay.app.feature.games.LegPose
import com.muscleplay.app.feature.games.PoseFrame

data class PoseOverlayLandmark(
    val x: Float,
    val y: Float,
    val visibility: Float,
)

class PoseLandmarkerHelper(
    context: Context,
    private val onPoseDetected: (PoseFrame?) -> Unit,
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

                // Preserve existing squat-counter functionality.
                onPoseDetected(result.toPoseFrame())
                // Extract all pose landmarks for the skeleton overlay
                val overlayLandmarks = result.landmarks()
                    .firstOrNull()
                    ?.map { landmark ->
                        PoseOverlayLandmark(
                            x = landmark.x(),
                            y = landmark.y(),
                            visibility = landmark.visibility().orElse(0f)
                        )
                    }
                // Send all 33 landmarks to the skeleton overlay.
                onLandmarksDetected(result.toOverlayLandmarks())
            }
            .setErrorListener { error ->
                onError(error.message ?: "Pose detection failed")
            }
            .build()

        landmarker = PoseLandmarker.createFromOptions(
            context,
            options
        )
    }

    fun detect(image: MPImage, timestampMs: Long) {
        landmarker.detectAsync(image, timestampMs)
    }

    fun close() {
        landmarker.close()
    }

    /**
     * Converts MediaPipe landmarks into the format used by the
     * shared squat repetition counter.
     */
    private fun PoseLandmarkerResult.toPoseFrame(): PoseFrame? {
        val landmarks = this.landmarks().firstOrNull()
            ?: return null

        fun landmark(index: Int): BodyLandmark? {
            val point = landmarks.getOrNull(index) ?: return null

            return BodyLandmark(
                x = point.x(),
                y = point.y(),
                visibility = point.visibility().orElse(0f)
            )
        }

        fun leg(
            hipIndex: Int,
            kneeIndex: Int,
            ankleIndex: Int,
        ): LegPose? {
            val hip = landmark(hipIndex) ?: return null
            val knee = landmark(kneeIndex) ?: return null
            val ankle = landmark(ankleIndex) ?: return null

            return LegPose(
                hip = hip,
                knee = knee,
                ankle = ankle
            )
        }

        return PoseFrame(
            leftLeg = leg(23, 25, 27),
            rightLeg = leg(24, 26, 28)
        )
    }

    /**
     * Converts the first detected pose into normalized coordinates
     * for drawing the full skeleton.
     */
    private fun PoseLandmarkerResult.toOverlayLandmarks():
            List<PoseOverlayLandmark>? {

        val landmarks = this.landmarks().firstOrNull()
            ?: return null

        return landmarks.map { point ->
            PoseOverlayLandmark(
                x = point.x(),
                y = point.y(),
                visibility = point.visibility().orElse(0f)
            )
        }
    }
}
