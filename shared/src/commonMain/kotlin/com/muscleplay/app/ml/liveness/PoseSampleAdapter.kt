
package com.muscleplay.app.ml.liveness

import com.muscleplay.app.ml.BodyJoint
import com.muscleplay.app.ml.PoseSample

/**
 * Converts the existing shared PoseSample model into the
 * generic liveness pose model.
 *
 * Only the six joints required by the current liveness
 * evaluator are mapped.
 */
fun PoseSample.toLivenessPose(
    frameCoverage: Float? = null
): LivenessPose {

    val mapping: Map<LivenessJoint, BodyJoint> = mapOf(
        LivenessJoint.LEFT_SHOULDER to BodyJoint.LEFT_SHOULDER,
        LivenessJoint.RIGHT_SHOULDER to BodyJoint.RIGHT_SHOULDER,
        LivenessJoint.LEFT_WRIST to BodyJoint.LEFT_WRIST,
        LivenessJoint.RIGHT_WRIST to BodyJoint.RIGHT_WRIST,
        LivenessJoint.LEFT_HIP to BodyJoint.LEFT_HIP,
        LivenessJoint.RIGHT_HIP to BodyJoint.RIGHT_HIP
    )

    val converted: Map<LivenessJoint, LivenessLandmark> =
        mapping.mapNotNull { (targetJoint, sourceJoint) ->
            val landmark = joint(
                sourceJoint,
                minimumVisibility = 0f
            ) ?: return@mapNotNull null

            targetJoint to LivenessLandmark(
                x = landmark.x,
                y = landmark.y,
                visibility = landmark.visibility
            )
        }.toMap()

    return LivenessPose(
        landmarks = converted,
        timestampMs = timestampMs,
        frameCoverage = frameCoverage
    )
}
