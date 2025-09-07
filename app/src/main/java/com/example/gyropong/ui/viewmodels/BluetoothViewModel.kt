package com.example.gyropong.ui.viewmodels

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import com.example.gyropong.hardware.bluetooth.BluetoothHelper
import com.example.gyropong.hardware.bluetooth.BluetoothDiscovery
import com.example.gyropong.hardware.bluetooth.BluetoothConnection

class BluetoothViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "BluetoothVM"
        private val APP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private const val APP_HEADER = "[PONG_APP]"
        private const val NICK_HEADER = "[NICKNAME]"
    }

    private val context = application.applicationContext
    private val adapter: BluetoothAdapter? = BluetoothHelper.getBluetoothAdapter(context)

    private var discovery: BluetoothDiscovery? = null
    private var connection: BluetoothConnection? = null

    private val _devices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val devices: StateFlow<List<BluetoothDevice>> = _devices

    private val _deviceNicknames = MutableStateFlow<Map<String, String>>(emptyMap())
    val deviceNicknames: StateFlow<Map<String, String>> = _deviceNicknames

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val _receivedData = MutableStateFlow<ByteArray?>(null)
    val receivedData: StateFlow<ByteArray?> = _receivedData

    private val _opponentNickname = MutableStateFlow<String?>(null)
    val opponentNickname: StateFlow<String?> = _opponentNickname

    private var myNickname: String? = null

    init {
        if (adapter != null) {
            discovery = BluetoothDiscovery(context, adapter)
            connection = BluetoothConnection(context, APP_UUID).apply {
                onDataReceived = { data ->
                    _receivedData.value = data
                    val message = String(data, Charsets.UTF_8)
                    Log.d(TAG, "Datos recibidos en VM: $message")

                    if (message.startsWith("$NICK_HEADER$APP_HEADER")) {
                        val nickname = message
                            .removePrefix(NICK_HEADER)
                            .removePrefix(APP_HEADER)

                        getConnectedDeviceAddress()?.let { address ->
                            _deviceNicknames.value = _deviceNicknames.value + (address to nickname)
                        }

                        _opponentNickname.value = nickname
                        Log.d(TAG, "Nickname del rival recibido: $nickname")
                    }
                }

                onConnected = {
                    _isConnected.value = true
                    Log.d(TAG, "Conectado correctamente")
                    myNickname?.let { nickname ->
                        val data = "$NICK_HEADER$APP_HEADER$nickname".toByteArray(Charsets.UTF_8)
                        sendData(data)
                        Log.d(TAG, "Nickname enviado automáticamente: $nickname")
                    }
                }

                onDisconnected = {
                    _isConnected.value = false
                    _opponentNickname.value = null
                    Log.d(TAG, "Conexión cerrada")
                }
            }

            viewModelScope.launch {
                discovery?.devices?.collect { list ->
                    _devices.value = list
                }
            }
        }
    }

    /** ----------- DISCOVERY ----------- **/
    fun startDiscovery() = viewModelScope.launch {
        try { discovery?.startDiscovery() }
        catch (e: SecurityException) { Log.e(TAG, "Permiso faltante para iniciar discovery", e) }
    }

    fun stopDiscovery() {
        @SuppressLint("MissingPermission")
        discovery?.stopDiscovery()
    }

    /** ----------- CONNECTION ----------- **/
    fun connect(device: BluetoothDevice, nickname: String) {
        myNickname = nickname
        connection?.connectToDevice(device)
    }

    fun startServer(nickname: String) {
        myNickname = nickname
        connection?.startServer()
    }

    fun sendData(data: ByteArray) {
        connection?.sendBytes(data)
    }

    fun disconnect() {
        connection?.disconnect()
    }

    fun makeDiscoverable(duration: Int = 300) {
        connection?.makeDiscoverable(duration)
    }

    /** ----------- HELPERS ----------- **/
    fun isBluetoothEnabled(): Boolean = BluetoothHelper.isBluetoothEnabled(context)
    fun isBluetoothAvailable(): Boolean = BluetoothHelper.isBluetoothAvailable(context)

    /** ----------- CLEANUP ----------- **/
    override fun onCleared() {
        super.onCleared()
        stopDiscovery()
        disconnect()
    }
}


/*
class BluetoothViewModel(
    application: Application
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "BluetoothVM"
        private val APP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private const val APP_HEADER = "[PONG_APP]"
        private const val NICK_HEADER = "[NICKNAME]"
    }

    private val context = application.applicationContext
    private val adapter: BluetoothAdapter? = BluetoothHelper.getBluetoothAdapter(context)

    private var discovery: BluetoothDiscovery? = null
    private var connection: BluetoothConnection? = null

    private val _devices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val devices: StateFlow<List<BluetoothDevice>> = _devices

    // Mapa de dirección -> nickname
    private val _deviceNicknames = MutableStateFlow<Map<String, String>>(emptyMap())
    val deviceNicknames: StateFlow<Map<String, String>> = _deviceNicknames

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val _receivedData = MutableStateFlow<ByteArray?>(null)
    val receivedData: StateFlow<ByteArray?> = _receivedData

    private val _opponentNickname = MutableStateFlow<String?>(null)
    val opponentNickname: StateFlow<String?> = _opponentNickname

    private var myNickname: String? = null

    init {
        if (adapter != null) {
            discovery = BluetoothDiscovery(context, adapter)
            connection = BluetoothConnection(context, APP_UUID).apply {
                onDataReceived = { data ->
                    _receivedData.value = data
                    val message = String(data, Charsets.UTF_8)
                    Log.d(TAG, "Datos recibidos en VM: $message")

                    // Si es un mensaje de nickname
                    if (message.startsWith("$NICK_HEADER$APP_HEADER")) {
                        val nickname = message
                            .removePrefix(NICK_HEADER)
                            .removePrefix(APP_HEADER)

                        connection?.getConnectedDeviceAddress()?.let { address ->
                            _deviceNicknames.value =
                                _deviceNicknames.value + (address to nickname)
                        }

                        _opponentNickname.value = nickname
                        Log.d(TAG, "Nickname del rival recibido: $nickname")
                    }
                }

                onConnected = {
                    _isConnected.value = true
                    Log.d(TAG, "Conectado correctamente")

                    // Enviar automáticamente el nickname tras conectarse
                    myNickname?.let { nickname ->
                        val data = "$NICK_HEADER$APP_HEADER$nickname".toByteArray(Charsets.UTF_8)
                        sendData(data)
                        Log.d(TAG, "Nickname enviado automáticamente: $nickname")
                    }
                }

                onDisconnected = {
                    _isConnected.value = false
                    _opponentNickname.value = null
                    Log.d(TAG, "Conexión cerrada")
                }
            }

            // Suscribirse al discovery
            viewModelScope.launch {
                discovery?.devices?.collect { list ->
                    _devices.value = list
                }
            }
        }
    }

    /** ----------- DISCOVERY ----------- **/
    fun startDiscovery() {
        viewModelScope.launch {
            try {
                discovery?.startDiscovery()
            } catch (e: SecurityException) {
                Log.e(TAG, "Permiso faltante para iniciar discovery", e)
            }
        }
    }

    fun stopDiscovery() {
        @SuppressLint("MissingPermission")
        discovery?.stopDiscovery()
    }

    /** ----------- CONNECTION ----------- **/
    fun connect(device: BluetoothDevice, nickname: String) {
        myNickname = nickname
        connection?.connectToDevice(device)
    }

    fun startServer(nickname: String) {
        myNickname = nickname
        connection?.startServer()
    }

    fun sendData(data: ByteArray) {
        connection?.sendBytes(data)
    }

    fun disconnect() {
        connection?.disconnect()
    }

    fun makeDiscoverable(duration: Int = 300) {
        connection?.makeDiscoverable(duration)
    }

    /** ----------- HELPERS ----------- **/
    fun isBluetoothEnabled(): Boolean = BluetoothHelper.isBluetoothEnabled(context)
    fun isBluetoothAvailable(): Boolean = BluetoothHelper.isBluetoothAvailable(context)
}
*/