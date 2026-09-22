package com.example.wasselha.utils

import android.location.Location

object LocationUtils {

    // الإحداثيات الجغرافية لمركز قرية سمهود (قنا)
    const val SEMHOUD_LAT = 26.050631
    const val SEMHOUD_LNG = 32.124564

    // المسافة المسموحة القصوى بالـ (متر) = 10 كيلومتر
    const val MAX_ALLOWED_DISTANCE_METERS = 10000.0

    /**
     * دالة للتحقق مما إذا كان الإحداثي الممرر يقع داخل نطاق الـ 10 كم من سمهود
     */
    fun isInsideDeliveryZone(targetLat: Double, targetLng: Double): Boolean {
        val semhoudCenter = Location("Semhoud_Center").apply {
            latitude = SEMHOUD_LAT
            longitude = SEMHOUD_LNG
        }

        val targetLocation = Location("Target_Location").apply {
            latitude = targetLat
            longitude = targetLng
        }

        // حساب المسافة الفعلية بالأمتار بين النقطتين
        val distanceInMeters = semhoudCenter.distanceTo(targetLocation)

        // تعيد true إذا كانت المسافة أقل من أو تساوي 10 كم
        return distanceInMeters <= MAX_ALLOWED_DISTANCE_METERS
    }

    /**
     * دالة لحساب المسافة بالكيلومتر من مركز سمهود
     */
    fun getDistanceFromSemhoudKm(targetLat: Double, targetLng: Double): Double {
        val semhoudCenter = Location("Semhoud_Center").apply {
            latitude = SEMHOUD_LAT
            longitude = SEMHOUD_LNG
        }

        val targetLocation = Location("Target_Location").apply {
            latitude = targetLat
            longitude = targetLng
        }

        return semhoudCenter.distanceTo(targetLocation) / 1000.0
    }
}
