// Este archivo contiene el manejo de descubrimiento de los dispositivos.
package com.example.gyropong.hardware.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BluetoothDiscovery(
    private val context: Context,
    private val adapter: BluetoothAdapter
) {

    companion object {
        private const val TAG = "BluetoothDiscovery"
    }

    private val _devices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val devices: StateFlow<List<BluetoothDevice>> = _devices

    private var isReceiverRegistered = false

    // Localizador de dispositivos.
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.let {
                        if (!_devices.value.contains(it)) {
                            _devices.value = _devices.value + it
                            val name = if (
                                ActivityCompat.checkSelfPermission(
                                    this@BluetoothDiscovery.context,
                                    Manifest.permission.BLUETOOTH_CONNECT
                                ) == PackageManager.PERMISSION_GRANTED
                            ) it.name ?: "Sin nombre" else "Nombre no disponible"
                            Log.d(TAG, "Dispositivo encontrado: $name / ${it.address}")
                        }
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.d(TAG, "Descubrimiento finalizado")
                }
            }
        }
    }

    // Incio de descubrimiento de dispositivos.
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun startDiscovery() {
        _devices.value = emptyList()
        if (!isReceiverRegistered) {
            val filter = IntentFilter(BluetoothDevice.ACTION_FOUND).apply {
                addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            }
            context.registerReceiver(receiver, filter)
            isReceiverRegistered = true
        }
        Log.d(TAG, "Iniciando descubrimiento de dispositivos...")
        adapter.startDiscovery()
    }

    // Detener el descubrimiento de dispositivos.
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun stopDiscovery() {
        if (adapter.isDiscovering) {
            adapter.cancelDiscovery()
            Log.d(TAG, "Deteniendo descubrimiento de dispositivos")
        }
        if (isReceiverRegistered) {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: IllegalArgumentException) { }
            isReceiverRegistered = false
        }
    }
}