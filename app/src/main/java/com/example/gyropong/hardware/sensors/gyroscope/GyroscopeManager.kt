// Este archivo continene la clase para el manejo del giroscopio.
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

    // Flujos para cada eje
    private val _xRotation = MutableStateFlow(0f) // pitch (arriba/abajo)
    val xRotation: StateFlow<Float> = _xRotation

    private val _yRotation = MutableStateFlow(0f) // yaw (izquierda/derecha)
    val yRotation: StateFlow<Float> = _yRotation

    private val _zRotation = MutableStateFlow(0f) // roll (rotación en plano)
    val zRotation: StateFlow<Float> = _zRotation

    private val gyroscopeListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            // Los valores corresponden a la velocidad angular (rad/s) en cada eje
            _xRotation.value = event.values[0] // Eje X
            _yRotation.value = event.values[1] // Eje Y
            _zRotation.value = event.values[2] // Eje Z
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