package com.example.calc

import kotlin.math.*

data class QiblaResult(
    val qiblaBearingDegrees: Float, // 0..360° from True North
    val distanceKm: Double,
    val isAligned: Boolean // True if user device heading is within ±3 degrees of Qibla
)

object QiblaCalculator {
    // Kaaba Coordinates in Makkah, Saudi Arabia
    const val KAABA_LATITUDE = 21.4225
    const val KAABA_LONGITUDE = 39.8262

    fun calculateQibla(
        userLatitude: Double,
        userLongitude: Double,
        currentDeviceHeading: Float = 0f
    ): QiblaResult {
        val phiUser = Math.toRadians(userLatitude)
        val lambdaUser = Math.toRadians(userLongitude)
        val phiKaaba = Math.toRadians(KAABA_LATITUDE)
        val lambdaKaaba = Math.toRadians(KAABA_LONGITUDE)

        val deltaLambda = lambdaKaaba - lambdaUser

        val y = sin(deltaLambda)
        val x = cos(phiUser) * tan(phiKaaba) - sin(phiUser) * cos(deltaLambda)

        var qiblaRad = atan2(y, x)
        var qiblaDeg = Math.toDegrees(qiblaRad).toFloat()
        if (qiblaDeg < 0) {
            qiblaDeg += 360f
        }

        // Distance using Haversine formula
        val dLat = phiKaaba - phiUser
        val dLon = deltaLambda
        val a = sin(dLat / 2).pow(2) + cos(phiUser) * cos(phiKaaba) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val earthRadiusKm = 6371.0
        val distance = earthRadiusKm * c

        // Check alignment
        val headingDiff = abs(qiblaDeg - (currentDeviceHeading % 360f))
        val minDiff = min(headingDiff, 360f - headingDiff)
        val isAligned = minDiff <= 3.5f

        return QiblaResult(
            qiblaBearingDegrees = qiblaDeg,
            distanceKm = distance,
            isAligned = isAligned
        )
    }
}
