
package com.muscleplay.app.ml

import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

object AndroidPoseAdapter {

    fun toPoseSample(
        result: PoseLandmarkerResult,
        timestampMs: Long
    ): PoseSample? {
        val points = result.landmarks().firstOrNull() ?: return null

        val indices = arrayOf(
            BodyJoint.NOSE to 0,
            BodyJoint.LEFT_SHOULDER to 11,
            BodyJoint.RIGHT_SHOULDER to 12,
            BodyJoint.LEFT_ELBOW to 13,
            BodyJoint.RIGHT_ELBOW to 14,
            BodyJoint.LEFT_WRIST to 15,
            BodyJoint.RIGHT_WRIST to 16,
            BodyJoint.LEFT_HIP to 23,
            BodyJoint.RIGHT_HIP to 24,
            BodyJoint.LEFT_KNEE to 25,
            BodyJoint.RIGHT_KNEE to 26,
            BodyJoint.LEFT_ANKLE to 27,
            BodyJoint.RIGHT_ANKLE to 28,
            BodyJoint.LEFT_HEEL to 29,
            BodyJoint.RIGHT_HEEL to 30,
            BodyJoint.LEFT_FOOT_INDEX to 31,
            BodyJoint.RIGHT_FOOT_INDEX to 32
        )

        val mapped = buildMap<BodyJoint, JointLandmark> {
            for ((joint, index) in indices) {
                val point = points.getOrNull(index) ?: continue

                put(
                    joint,
                    JointLandmark(
                        x = point.x(),
                        y = point.y(),
                        z = point.z(),
                        visibility = point.visibility().orElse(0f)
                    )
                )
            }
        }

        return PoseSample(
            landmarks = mapped,
            timestampMs = timestampMs
        )
    }
}