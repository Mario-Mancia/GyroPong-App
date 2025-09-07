package com.example.gyropong.hardware.sensors.gyroscope

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class GyroscopeManager(context: Context) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val gyroscope: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    private val _xRotation = MutableStateFlow(0f) // Rotación en el eje X (izquierda/derecha)
    val xRotation: StateFlow<Float> = _xRotation

    private val gyroscopeListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            // event.values[0] -> rotación alrededor del eje X
            _xRotation.value = event.values[0]
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    fun start() {
        gyroscope?.also { sensor ->
            sensorManager.registerListener(
                gyroscopeListener,
                sensor,
                SensorManager.SENSOR_DELAY_GAME
            )
        }
    }

    fun stop() {
        sensorManager.unregisterListener(gyroscopeListener)
    }
}