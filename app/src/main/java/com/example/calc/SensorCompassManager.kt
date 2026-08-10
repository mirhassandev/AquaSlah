package com.example.calc

import android.content.Context
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.abs

class SensorCompassManager(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val rotationVectorSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val _headingFlow = MutableStateFlow(0f)
    val headingFlow: StateFlow<Float> = _headingFlow

    private val _hasSensors = MutableStateFlow(rotationVectorSensor != null || (accelerometer != null && magnetometer != null))
    val hasSensors: StateFlow<Boolean> = _hasSensors

    private var userLat: Double = 0.0
    private var userLon: Double = 0.0

    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientation = FloatArray(3)

    private var currentHeading = 0f

    fun setLocation(lat: Double, lon: Double) {
        userLat = lat
        userLon = lon
    }

    fun start() {
        if (rotationVectorSensor != null) {
            sensorManager?.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            accelerometer?.let {
                sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
            magnetometer?.let {
                sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
        }
    }

    fun stop() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        var magneticHeading = -1f

        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientation)
            var azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
            if (azimuth < 0) azimuth += 360f
            magneticHeading = azimuth
        } else {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                gravity[0] = gravity[0] * 0.9f + event.values[0] * 0.1f
                gravity[1] = gravity[1] * 0.9f + event.values[1] * 0.1f
                gravity[2] = gravity[2] * 0.9f + event.values[2] * 0.1f
            }

            if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                geomagnetic[0] = geomagnetic[0] * 0.9f + event.values[0] * 0.1f
                geomagnetic[1] = geomagnetic[1] * 0.9f + event.values[1] * 0.1f
                geomagnetic[2] = geomagnetic[2] * 0.9f + event.values[2] * 0.1f
            }

            if (SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)) {
                SensorManager.getOrientation(rotationMatrix, orientation)
                var azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
                if (azimuth < 0) azimuth += 360f
                magneticHeading = azimuth
            }
        }

        if (magneticHeading >= 0f) {
            val trueHeading = computeTrueHeading(magneticHeading)
            if (abs(trueHeading - currentHeading) > 0.4f) {
                currentHeading = trueHeading
                _headingFlow.value = trueHeading
            }
        }
    }

    private fun computeTrueHeading(magneticHeading: Float): Float {
        if (userLat == 0.0 && userLon == 0.0) return magneticHeading
        return try {
            val geoField = GeomagneticField(
                userLat.toFloat(),
                userLon.toFloat(),
                0f,
                System.currentTimeMillis()
            )
            (magneticHeading + geoField.declination + 360f) % 360f
        } catch (e: Exception) {
            magneticHeading
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
