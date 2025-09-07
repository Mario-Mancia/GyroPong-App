package com.example.gyropong.hardware.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context

object BluetoothHelper {

    fun getBluetoothAdapter(context: Context): BluetoothAdapter? {
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        return bluetoothManager.adapter
    }

    fun isBluetoothAvailable(context: Context): Boolean {
        return getBluetoothAdapter(context) != null
    }

    fun isBluetoothEnabled(context: Context): Boolean {
        return getBluetoothAdapter(context)?.isEnabled == true
    }
}