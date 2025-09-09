package com.example.gyropong.ui.viewmodels

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import com.example.gyropong.hardware.bluetooth.BluetoothHelper
import com.example.gyropong.hardware.bluetooth.BluetoothDiscovery
import com.example.gyropong.hardware.bluetooth.BluetoothConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update

class BluetoothViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "BluetoothVM"
        private val APP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private const val APP_HEADER = "[PONG_APP]"
        private const val NICK_HEADER = "[NICKNAME]"
        private const val START_HEADER = "[START_GAME]"
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

    private var myNickname: String? = null
    var isHost: Boolean = false

    init {
        if (adapter != null) {
            discovery = BluetoothDiscovery(context, adapter)
            viewModelScope.launch { discovery?.devices?.collect { _devices.value = it } }

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
        myNickname?.let { nickname ->
            sendData("$NICK_HEADER$APP_HEADER$nickname".toByteArray(Charsets.UTF_8))
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
        }
    }

    fun startServer(nickname: String) {
        myNickname = nickname
        isHost = true
        connection?.startServer()
    }

    fun connect(device: BluetoothDevice, nickname: String) {
        myNickname = nickname
        isHost = false
        stopDiscovery()
        connection?.connectToDevice(device)
    }

    fun sendData(data: ByteArray) { connection?.sendBytes(data) }

    fun sendStartSignal() {
        sendData(START_HEADER.toByteArray(Charsets.UTF_8))
        _startSignalReceived.value = true
    }

    fun disconnect() { connection?.disconnect() }

    fun startDiscovery() {
        try {
            if (!hasRequiredPermissions()) {
                Log.w(TAG, "Permisos necesarios no concedidos")
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
            if (!hasRequiredPermissions()) return
            discovery?.stopDiscovery()
            Log.d(TAG, "Discovery detenido correctamente")
        } catch (e: SecurityException) {
            Log.e(TAG, "Error deteniendo discovery: ${e.message}", e)
        }
    }

    fun makeDiscoverable(duration: Int = 300) { connection?.makeDiscoverable(duration) }

    fun getConnectedDeviceAddress(): String? = connection?.getConnectedDeviceAddress()

    // ------------------------ PERMISOS ------------------------
    private fun hasRequiredPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
    }
}


/*
class BluetoothViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "BluetoothVM"
        private val APP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private const val APP_HEADER = "[PONG_APP]"
        private const val NICK_HEADER = "[NICKNAME]"
        private const val START_HEADER = "[START_GAME]"
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

    private var myNickname: String? = null
    var isHost: Boolean = false

    init {
        if (adapter != null) {
            discovery = BluetoothDiscovery(context, adapter)
            viewModelScope.launch { discovery?.devices?.collect { _devices.value = it } }

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
        myNickname?.let { nickname ->
            sendData("$NICK_HEADER$APP_HEADER$nickname".toByteArray(Charsets.UTF_8))
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
        }
    }

    fun startServer(nickname: String) {
        myNickname = nickname
        isHost = true
        connection?.startServer()
    }

    fun connect(device: BluetoothDevice, nickname: String) {
        myNickname = nickname
        isHost = false
        stopDiscovery()
        connection?.connectToDevice(device)
    }

    fun sendData(data: ByteArray) { connection?.sendBytes(data) }

    fun sendStartSignal() {
        sendData(START_HEADER.toByteArray(Charsets.UTF_8))
        _startSignalReceived.value = true
    }

    fun disconnect() { connection?.disconnect() }

    fun startDiscovery() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    Log.w(TAG, "Permiso BLUETOOTH_SCAN no concedido")
                    return
                }
            } else {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Log.w(TAG, "Permiso ACCESS_FINE_LOCATION no concedido")
                    return
                }
            }
            discovery?.startDiscovery()
            Log.d(TAG, "Discovery iniciado correctamente")
        } catch (e: SecurityException) {
            Log.e(TAG, "Error iniciando discovery: ${e.message}", e)
        }
    }

    fun stopDiscovery() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) return
            } else {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return
            }
            discovery?.stopDiscovery()
            Log.d(TAG, "Discovery detenido correctamente")
        } catch (e: SecurityException) {
            Log.e(TAG, "Error deteniendo discovery: ${e.message}", e)
        }
    }

    fun makeDiscoverable(duration: Int = 300) { connection?.makeDiscoverable(duration) }
    fun getConnectedDeviceAddress(): String? = connection?.getConnectedDeviceAddress()
}
*/

/*
class BluetoothViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "BluetoothVM"
        private val APP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private const val APP_HEADER = "[PONG_APP]"
        private const val NICK_HEADER = "[NICKNAME]"
        private const val START_HEADER = "[START_GAME]"
    }

    private val context: Context = application.applicationContext
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

    private val _receivedData = MutableStateFlow<ByteArray?>(null)
    val receivedData: StateFlow<ByteArray?> = _receivedData

    private var myNickname: String? = null
    var isHost: Boolean = false
    private var toastShown = false

    init {
        if (adapter != null) {
            discovery = BluetoothDiscovery(context, adapter)
            connection = BluetoothConnection(context, APP_UUID).apply {
                onDataReceived = { data ->
                    val message = String(data, Charsets.UTF_8)
                    viewModelScope.launch(Dispatchers.Main) {
                        _receivedData.value = data
                        Log.d(TAG, "Datos recibidos en VM: $message")

                        when {
                            message.startsWith("$NICK_HEADER$APP_HEADER") -> {
                                val nickname = message.removePrefix("$NICK_HEADER$APP_HEADER")
                                _opponentNickname.value = nickname
                                getConnectedDeviceAddress()?.let { addr ->
                                    _deviceNicknames.value = _deviceNicknames.value + (addr to nickname)
                                }

                                if (isHost) {
                                    viewModelScope.launch {
                                        delay(120)
                                        sendStartSignal()
                                    }
                                }
                            }

                            message.startsWith(START_HEADER) -> {
                                _startSignalReceived.value = true
                                Log.d(TAG, "Señal de inicio de juego recibida")
                            }
                        }
                    }
                }

                onConnected = {
                    viewModelScope.launch(Dispatchers.Main) {
                        _isConnected.value = true
                        Log.d(TAG, "Conectado correctamente")
                        myNickname?.let { nickname ->
                            try { sendData("$NICK_HEADER$APP_HEADER$nickname".toByteArray(Charsets.UTF_8)) }
                            catch (t: Throwable) { Log.e(TAG, "Error enviando nickname: ${t.message}", t) }
                        }
                    }
                }

                onDisconnected = {
                    viewModelScope.launch(Dispatchers.Main) {
                        _isConnected.value = false
                        _opponentNickname.value = null
                        _startSignalReceived.value = false
                        _receivedData.value = null
                        Log.d(TAG, "Conexión cerrada")

                        if (!toastShown) {
                            toastShown = true
                            Handler(Looper.getMainLooper()).postDelayed({
                                Toast.makeText(context, "Conexión cerrada", Toast.LENGTH_SHORT).show()
                                toastShown = false
                            }, 50)
                        }
                    }
                }
            }

            viewModelScope.launch {
                discovery?.devices?.collect { list -> _devices.value = list }
            }
        } else {
            Log.w(TAG, "Bluetooth no disponible en este dispositivo")
        }
    }

    fun startDiscovery() = viewModelScope.launch {
        val adapter = adapter ?: return@launch

        // Android S+ requiere BLUETOOTH_SCAN
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Permiso BLUETOOTH_SCAN faltante")
                Toast.makeText(context, "Se requiere permiso BLUETOOTH_SCAN", Toast.LENGTH_SHORT).show()
                return@launch
            }
        } else {
            // Android < S: requiere ACCESS_FINE_LOCATION
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Permiso ACCESS_FINE_LOCATION faltante")
                Toast.makeText(context, "Se requiere permiso de ubicación", Toast.LENGTH_SHORT).show()
                return@launch
            }
        }

        try {
            discovery?.startDiscovery()
            Log.d(TAG, "Discovery iniciado correctamente")
        } catch (e: SecurityException) {
            Log.e(TAG, "Error iniciando discovery: ${e.message}", e)
        }
    }


    fun stopDiscovery() {
        try {
            @SuppressLint("MissingPermission")
            discovery?.stopDiscovery()
        } catch (e: SecurityException) {
            Log.e(TAG, "Error deteniendo discovery: ${e.message}", e)
        }
    }

    fun connect(device: BluetoothDevice, nickname: String) {
        myNickname = nickname
        isHost = false
        stopDiscovery()
        connection?.connectToDevice(device)
    }

    fun startServer(nickname: String) {
        myNickname = nickname
        isHost = true
        connection?.startServer()
    }

    fun sendData(data: ByteArray) { connection?.sendBytes(data) }

    fun sendStartSignal() {
        connection?.sendBytes(START_HEADER.toByteArray(Charsets.UTF_8))
        _startSignalReceived.value = true
        Log.d(TAG, "Señal de inicio enviada")
    }

    fun disconnect() { connection?.disconnect() }
    fun makeDiscoverable(duration: Int = 300) { connection?.makeDiscoverable(duration) }
    fun getConnectedDeviceAddress(): String? = connection?.getConnectedDeviceAddress()
    fun isBluetoothEnabled(): Boolean = adapter?.isEnabled == true
    fun isBluetoothAvailable(): Boolean = adapter != null

    override fun onCleared() {
        super.onCleared()
        stopDiscovery()
        disconnect()
    }
}
*/

/*
class BluetoothViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "BluetoothVM"
        private val APP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private const val APP_HEADER = "[PONG_APP]"
        private const val NICK_HEADER = "[NICKNAME]"
        private const val START_HEADER = "[START_GAME]"
    }

    private val context: Context = application.applicationContext
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

    // Expuesto para que la UI (cliente) pueda leer paquetes completos como lo haces con snapshotFlow
    private val _receivedData = MutableStateFlow<ByteArray?>(null)
    val receivedData: StateFlow<ByteArray?> = _receivedData

    private var myNickname: String? = null
    var isHost: Boolean = false

    // small helper to avoid spamming toast on rapid disconnect/connect
    private var toastShown = false

    init {
        if (adapter != null) {
            discovery = BluetoothDiscovery(context, adapter)
            connection = BluetoothConnection(context, APP_UUID).apply {
                onDataReceived = { data ->
                    val message = String(data, Charsets.UTF_8)
                    // Guardamos el paquete recibido para la UI y procesamos el encabezado
                    viewModelScope.launch(Dispatchers.Main) {
                        _receivedData.value = data
                        Log.d(TAG, "Datos recibidos en VM: $message")

                        when {
                            message.startsWith("$NICK_HEADER$APP_HEADER") -> {
                                val nickname = message.removePrefix("$NICK_HEADER$APP_HEADER")
                                _opponentNickname.value = nickname

                                // Guardar nickname asociado a la dirección
                                getConnectedDeviceAddress()?.let { addr ->
                                    _deviceNicknames.value = _deviceNicknames.value + (addr to nickname)
                                }

                                Log.d(TAG, "Nickname recibido: $nickname")

                                // Si soy host, envío START con pequeño delay para asegurar que cliente procese NICK
                                if (isHost) {
                                    viewModelScope.launch {
                                        delay(120) // pequeño margen
                                        sendStartSignal()
                                    }
                                }
                            }

                            message.startsWith(START_HEADER) -> {
                                // Cliente o Host que recibe señal: marcar listo
                                _startSignalReceived.value = true
                                Log.d(TAG, "Señal de inicio de juego recibida")
                            }

                            else -> {
                                // Otros mensajes (por ejemplo [BALL]...) quedan en receivedData para la UI
                            }
                        }
                    }
                }

                onConnected = {
                    // onConnected se dispara desde la capa IO: actualizamos desde el main
                    viewModelScope.launch(Dispatchers.Main) {
                        _isConnected.value = true
                        Log.d(TAG, "Conectado correctamente")
                        myNickname?.let { nickname ->
                            try {
                                sendData("$NICK_HEADER$APP_HEADER$nickname".toByteArray(Charsets.UTF_8))
                                Log.d(TAG, "Nickname enviado automáticamente: $nickname")
                            } catch (t: Throwable) {
                                Log.e(TAG, "Error enviando nickname automáticamente: ${t.message}", t)
                            }
                        }
                    }
                }

                onDisconnected = {
                    // onDisconnected desde capa IO -> actualizar estados en main
                    viewModelScope.launch(Dispatchers.Main) {
                        _isConnected.value = false
                        _opponentNickname.value = null
                        _startSignalReceived.value = false
                        _receivedData.value = null
                        Log.d(TAG, "Conexión cerrada")

                        // Mensaje corto al usuario (evitar spam)
                        if (!toastShown) {
                            toastShown = true
                            Handler(Looper.getMainLooper()).postDelayed({
                                Toast.makeText(context, "Conexión cerrada", Toast.LENGTH_SHORT).show()
                                toastShown = false
                            }, 50)
                        }
                    }
                }
            }

            // recolectar dispositivos publicados por tu BluetoothDiscovery (si tu clase lo expone)
            viewModelScope.launch {
                discovery?.devices?.collect { list ->
                    _devices.value = list
                }
            }
        } else {
            Log.w(TAG, "Bluetooth no disponible en este dispositivo")
        }
    }

    /** ----------- DISCOVERY ----------- **/
    fun startDiscovery() = viewModelScope.launch {
        try {
            discovery?.startDiscovery()
        } catch (e: SecurityException) {
            Log.e(TAG, "Permiso faltante para discovery", e)
        }
    }

    fun stopDiscovery() {
        @SuppressLint("MissingPermission")
        discovery?.stopDiscovery()
    }

    /** ----------- CONEXIÓN ----------- **/
    /**
     * Conectar como cliente a device; nickname es el mío
     */
    fun connect(device: BluetoothDevice, nickname: String) {
        myNickname = nickname
        isHost = false
        // detener discovery al conectar (evita interferencias)
        stopDiscovery()
        connection?.connectToDevice(device)
    }

    /**
     * Iniciar servidor (host)
     */
    fun startServer(nickname: String) {
        myNickname = nickname
        isHost = true
        connection?.startServer()
    }

    /**
     * Enviar bytes crudos
     */
    fun sendData(data: ByteArray) {
        connection?.sendBytes(data)
    }

    /**
     * Enviar la señal de inicio de juego.
     * Nota: el host marca también su propio _startSignalReceived para evitar esperar a sí mismo.
     */
    fun sendStartSignal() {
        connection?.sendBytes(START_HEADER.toByteArray(Charsets.UTF_8))
        // marcar localmente que el juego empezó (útil para el host)
        _startSignalReceived.value = true
        Log.d(TAG, "Señal de inicio enviada")
    }

    fun disconnect() {
        connection?.disconnect()
    }

    fun makeDiscoverable(duration: Int = 300) {
        connection?.makeDiscoverable(duration)
    }

    /** ----------- HELPERS ----------- **/
    fun getConnectedDeviceAddress(): String? = connection?.getConnectedDeviceAddress()

    fun isBluetoothEnabled(): Boolean = adapter?.isEnabled == true
    fun isBluetoothAvailable(): Boolean = adapter != null

    override fun onCleared() {
        super.onCleared()
        stopDiscovery()
        disconnect()
    }
}
*/