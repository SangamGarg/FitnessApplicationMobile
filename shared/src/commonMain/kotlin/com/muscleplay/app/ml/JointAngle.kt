package com.muscleplay.app.ml

import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.sqrt

object JointAngle {

    /**
     * Returns the angle ABC in degrees, where B is the joint.
     * Returns null if the vectors are too small to calculate safely.
     */
    fun degrees(
        a: JointLandmark,
        b: JointLandmark,
        c: JointLandmark,
    ): Double? {
        val abX = (a.x - b.x).toDouble()
        val abY = (a.y - b.y).toDouble()
        val abZ = (a.z - b.z).toDouble()

        val cbX = (c.x - b.x).toDouble()
        val cbY = (c.y - b.y).toDouble()
        val cbZ = (c.z - b.z).toDouble()

        val magnitudeAB = sqrt(
            abX * abX + abY * abY + abZ * abZ
        )
        val magnitudeCB = sqrt(
            cbX * cbX + cbY * cbY + cbZ * cbZ
        )

        if (magnitudeAB < 1e-6 || magnitudeCB < 1e-6) {
            return null
        }

        val dot = abX * cbX + abY * cbY + abZ * cbZ
        val cosine = (dot / (magnitudeAB * magnitudeCB))
            .coerceIn(-1.0, 1.0)

        return acos(cosine) * 180.0 / PI
    }
}
