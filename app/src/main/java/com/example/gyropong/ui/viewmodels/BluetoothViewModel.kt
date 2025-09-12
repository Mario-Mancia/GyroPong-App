/* Este archivo contiene la clase para manejar el intercambio de información entre ambos
* usuarios bluetooth, compatible con el juego de pingpong no utilizado y con el de piedra, papel
* o tijeras */

package com.example.gyropong.ui.viewmodels

import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import com.example.gyropong.hardware.bluetooth.BluetoothDiscovery
import com.example.gyropong.hardware.bluetooth.BluetoothConnection
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.update
import com.example.gyropong.ui.components.Ball

class BluetoothViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "BluetoothVM"
        private val APP_UUID: UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

        private const val APP_HEADER = "[PONG_APP]"
        private const val NICK_HEADER = "[NICKNAME]"
        private const val START_HEADER = "[START_GAME]"
        private const val BALL_HEADER = "[BALL]"
        private const val RPS_HEADER = "[RPS]"
    }

    private val context = application.applicationContext
    private val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var discovery: BluetoothDiscovery? = null
    private var connection: BluetoothConnection? = null

    private val _devices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val devices: StateFlow<List<BluetoothDevice>> = _devices

    private val _deviceNicknames = MutableStateFlow<Map<String, String>>(emptyMap())
    val deviceNicknames: StateFlow<Map<String, String>> = _deviceNicknames

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val _opponentNickname = MutableStateFlow<String?>(null)
    val opponentNickname: StateFlow<String?> = _opponentNickname

    private val _startSignalReceived = MutableStateFlow(false)
    val startSignalReceived: StateFlow<Boolean> = _startSignalReceived

    // Bola (Pantalla de juego sin utilizar)
    private val _incomingBall = MutableSharedFlow<Ball>(replay = 1)
    val incomingBall: SharedFlow<Ball> = _incomingBall

    // Archivos para el juego de Piedra papel o tijera.
    private val _incomingRps = MutableSharedFlow<String>(replay = 1)
    val incomingRps: SharedFlow<String> = _incomingRps

    var myNickname: String? = null
    var isHost: Boolean = false

    init {
        if (adapter != null) {
            discovery = BluetoothDiscovery(context, adapter)
            viewModelScope.launch {
                discovery?.devices?.collect { _devices.value = it }
            }

            connection = BluetoothConnection(context, APP_UUID).apply {
                onDataReceived = { handleReceivedData(it) }
                onConnected = { onConnected() }
                onDisconnected = { onDisconnected() }
            }
        } else Log.w(TAG, "Bluetooth no disponible")
    }

    private fun onConnected() {
        _isConnected.value = true
        Log.d(TAG, "Conectado correctamente")
        myNickname?.let {
            sendData("$NICK_HEADER$APP_HEADER$it".toByteArray(Charsets.UTF_8))
        }
    }

    private fun onDisconnected() {
        _isConnected.value = false
        _opponentNickname.value = null
        _startSignalReceived.value = false
        Log.d(TAG, "Desconectado")
    }

    private fun handleReceivedData(data: ByteArray) {
        val message = String(data, Charsets.UTF_8)
        Log.d(TAG, "Datos recibidos en VM: $message")

        when {
            message.startsWith("$NICK_HEADER$APP_HEADER") -> {
                val nick = message.removePrefix("$NICK_HEADER$APP_HEADER")
                _opponentNickname.value = nick
                val addr = connection?.getConnectedDeviceAddress() ?: "unknown"
                _deviceNicknames.update { it + (addr to nick) }
                if (isHost) sendStartSignal()
            }

            message.startsWith(START_HEADER) -> _startSignalReceived.value = true

            message.startsWith(BALL_HEADER) -> {
                val parts = message.removePrefix(BALL_HEADER).split(",")
                if (parts.size == 4) {
                    val ball = Ball(
                        x = parts[0].toFloat(),
                        y = parts[1].toFloat(),
                        vx = parts[2].toFloat(),
                        vy = parts[3].toFloat()
                    )
                    viewModelScope.launch { _incomingBall.emit(ball) }
                }
            }

            message.startsWith(RPS_HEADER) -> {
                val choice = message.removePrefix(RPS_HEADER)
                viewModelScope.launch { _incomingRps.emit(choice) }
            }
        }
    }

    // Funciones
    fun startServer(nickname: String) {
        myNickname = nickname; isHost = true; connection?.startServer()
    }

    fun connect(device: BluetoothDevice, nickname: String) {
        myNickname = nickname; isHost = false
        stopDiscovery(); connection?.connectToDevice(device)
    }

    fun sendData(data: ByteArray) {
        connection?.sendBytes(data)
    }

    fun sendStartSignal() {
        sendData(START_HEADER.toByteArray(Charsets.UTF_8))
        _startSignalReceived.value = true
    }

    fun disconnect() {
        connection?.disconnect()
    }

    fun startDiscovery() {
        try {
            if (!hasRequiredPermissions()) {
                Log.w(TAG, "Permisos necesarios no concedidos para discovery")
                return
            }
            discovery?.startDiscovery()
            Log.d(TAG, "Discovery iniciado correctamente")
        } catch (e: SecurityException) {
            Log.e(TAG, "Error iniciando discovery: ${e.message}", e)
        }
    }

    fun stopDiscovery() {
        try {
            if (!hasRequiredPermissions()) {
                Log.w(TAG, "Permisos necesarios no concedidos para detener discovery")
                return
            }
            discovery?.stopDiscovery()
            Log.d(TAG, "Discovery detenido correctamente")
        } catch (e: SecurityException) {
            Log.e(TAG, "Error deteniendo discovery: ${e.message}", e)
        }
    }

    fun makeDiscoverable(duration: Int = 300) {
        connection?.makeDiscoverable(duration)
    }

    fun sendBall(ball: Ball) {
        val message = "$BALL_HEADER${ball.x},${ball.y},${ball.vx},${ball.vy}"
        sendData(message.toByteArray(Charsets.UTF_8))
    }

    // Manejos para el nuevo juego de Piedra Papel o Tijera.
    fun sendRpsChoice(choice: String) {
        val message = "$RPS_HEADER$choice"
        sendData(message.toByteArray(Charsets.UTF_8))
    }

    private fun hasRequiredPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) == PackageManager.PERMISSION_GRANTED
        } else {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}
