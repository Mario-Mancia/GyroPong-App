package com.example.gyropong.hardware.sensors.accelerometer

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.sqrt

class AccelerometerManager(context: Context) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _isFastMovement = MutableStateFlow(false)
    val isFastMovement: StateFlow<Boolean> = _isFastMovement

    private val threshold = 15f // Valor para detectar movimiento brusco (ajustable)

    private val accelerometerListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val magnitude = sqrt(x * x + y * y + z * z)
            _isFastMovement.value = magnitude > threshold
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    fun start() {
        accelerometer?.also { sensor ->
            sensorManager.registerListener(
                accelerometerListener,
                sensor,
                SensorManager.SENSOR_DELAY_GAME
            )
        }
    }

    fun stop() {
        sensorManager.unregisterListener(accelerometerListener)
    }
}
