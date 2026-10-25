
package com.muscleplay.app.ml

enum class BodyJoint {
    NOSE,
    LEFT_SHOULDER,
    RIGHT_SHOULDER,
    LEFT_ELBOW,
    RIGHT_ELBOW,
    LEFT_WRIST,
    RIGHT_WRIST,
    LEFT_HIP,
    RIGHT_HIP,
    LEFT_KNEE,
    RIGHT_KNEE,
    LEFT_ANKLE,
    RIGHT_ANKLE,
    LEFT_HEEL,
    RIGHT_HEEL,
    LEFT_FOOT_INDEX,
    RIGHT_FOOT_INDEX
}

data class JointLandmark(
    val x: Float,
    val y: Float,
    val z: Float = 0f,
    val visibility: Float = 0f
)

data class PoseSample(
    val landmarks: Map<BodyJoint, JointLandmark>,
    val timestampMs: Long
) {
    fun joint(
        joint: BodyJoint,
        minimumVisibility: Float = 0.5f
    ): JointLandmark? {
        val point = landmarks[joint] ?: return null
        return point.takeIf { it.visibility >= minimumVisibility }
    }
}