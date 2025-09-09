package com.example.gyropong.hardware.bluetooth


import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.util.UUID

@SuppressLint("MissingPermission")
class BluetoothConnection(
    private val context: Context,
    private val uuid: UUID
) {
    companion object { private const val TAG = "BluetoothConnection" }

    private val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var clientSocket: BluetoothSocket? = null
    private var serverSocket: BluetoothServerSocket? = null
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    var onDataReceived: ((ByteArray) -> Unit)? = null
    var onConnected: (() -> Unit)? = null
    var onDisconnected: (() -> Unit)? = null

    @Volatile private var manualDisconnect = false
    @Volatile private var isReading = false
    @Volatile private var disconnectedNotified = false
    private var readJob: Job? = null

    // ---------------- CLIENTE ----------------
    fun connectToDevice(device: BluetoothDevice) {
        manualDisconnect = false
        disconnectedNotified = false
        readJob?.cancel()

        ioScope.launch {
            try {
                clientSocket = device.createRfcommSocketToServiceRecord(uuid)
                adapter?.cancelDiscovery()
                clientSocket?.connect()
                Log.d(TAG, "Cliente conectado: ${device.name} / ${device.address}")
                onConnected?.invoke() // <- aquí se activa isConnected
                Log.d(TAG, "onConnected() invocado")
                startReading(clientSocket)
            } catch (e: IOException) {
                Log.e(TAG, "Error conectando al dispositivo: ${e.message}", e)
                if (!manualDisconnect) notifyDisconnected()
            }
        }
    }

    /*
    fun connectToDevice(device: BluetoothDevice) {
        manualDisconnect = false
        disconnectedNotified = false
        readJob?.cancel()

        ioScope.launch {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
                ) return@launch

                clientSocket = device.createRfcommSocketToServiceRecord(uuid)
                adapter?.cancelDiscovery()
                clientSocket?.connect()
                Log.d(TAG, "Conexión exitosa con: ${device.name ?: "Sin nombre"} / ${device.address}")
                onConnected?.invoke()
                startReading(clientSocket)
            } catch (e: IOException) {
                Log.e(TAG, "Error conectando al dispositivo: ${e.message}", e)
                if (!manualDisconnect) notifyDisconnected()
            }
        }
    }*/

    // ---------------- SERVIDOR ----------------
    fun startServer() {
        manualDisconnect = false
        disconnectedNotified = false
        readJob?.cancel()

        ioScope.launch {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED
                ) return@launch

                serverSocket = adapter?.listenUsingRfcommWithServiceRecord("PONG_APP", uuid)
                Log.d(TAG, "Servidor escuchando conexiones...")
                clientSocket = serverSocket?.accept()
                Log.d(TAG, "Cliente conectado: ${clientSocket?.remoteDevice?.name ?: "Sin nombre"} / ${clientSocket?.remoteDevice?.address}")
                onConnected?.invoke()
                startReading(clientSocket)
            } catch (e: IOException) {
                Log.e(TAG, "Error en servidor: ${e.message}", e)
                if (!manualDisconnect) notifyDisconnected()
            }
        }
    }

    // ---------------- LECTURA ----------------
    private fun startReading(socket: BluetoothSocket?) {
        socket ?: return
        isReading = true
        readJob?.cancel()
        readJob = ioScope.launch {
            val buffer = ByteArray(1024)
            val inputStream = try { socket.inputStream } catch (e: IOException) {
                Log.e(TAG, "No se pudo obtener inputStream: ${e.message}", e)
                if (!manualDisconnect) notifyDisconnected()
                return@launch
            }

            try {
                Log.d(TAG, "Iniciando lectura de datos desde socket...")
                while (isActive && isReading) {
                    val bytes = try { inputStream.read(buffer) } catch (e: IOException) {
                        Log.e(TAG, "IOException leyendo stream: ${e.message}")
                        if (!manualDisconnect) notifyDisconnected()
                        return@launch
                    }

                    if (bytes > 0) {
                        val data = buffer.copyOf(bytes)
                        onDataReceived?.invoke(data)
                    }
                }
            } catch (t: Throwable) {
                Log.e(TAG, "Error en loop de lectura: ${t.message}", t)
                if (!manualDisconnect) notifyDisconnected()
            }
        }
    }

    // ---------------- ENVÍO ----------------
    fun sendBytes(data: ByteArray) {
        ioScope.launch {
            try {
                clientSocket?.outputStream?.write(data)
                Log.d(TAG, "Datos enviados (${data.size} bytes)")
            } catch (e: IOException) {
                Log.e(TAG, "Error enviando datos: ${e.message}", e)
                if (!manualDisconnect) notifyDisconnected()
            }
        }
    }

    // ---------------- DESCONECTAR ----------------
    fun disconnect() {
        manualDisconnect = true
        isReading = false
        readJob?.cancel()
        readJob = null

        try { clientSocket?.close() } catch (e: IOException) { Log.e(TAG, e.message ?: "", e) }
        try { serverSocket?.close() } catch (e: IOException) { Log.e(TAG, e.message ?: "", e) }

        clientSocket = null
        serverSocket = null
        Log.d(TAG, "Sockets cerrados, conexión terminada. manual=true")
        notifyDisconnected()
    }

    fun makeDiscoverable(duration: Int = 300) {
        val adapter = adapter ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
        ) return

        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, duration)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        Log.d(TAG, "Haciendo dispositivo descubrible por $duration segundos")
    }

    fun getConnectedDeviceAddress(): String? = clientSocket?.remoteDevice?.address

    private fun notifyDisconnected() {
        if (disconnectedNotified) return
        disconnectedNotified = true
        onDisconnected?.invoke()
    }
}

/*
class BluetoothConnection(
    private val context: Context,
    private val uuid: UUID
) {
    companion object {
        private const val TAG = "BluetoothConnection"
    }

    private val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var clientSocket: BluetoothSocket? = null
    private var serverSocket: BluetoothServerSocket? = null
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    var onDataReceived: ((ByteArray) -> Unit)? = null
    var onConnected: (() -> Unit)? = null
    var onDisconnected: (() -> Unit)? = null

    @Volatile private var manualDisconnect = false
    @Volatile private var isReading = false

    // Evita llamar onDisconnected más de una vez
    @Volatile private var disconnectedNotified = false

    // Job de lectura para poder cancelarla en disconnect()
    private var readJob: Job? = null

    // ---------------- CLIENTE ----------------
    fun connectToDevice(device: BluetoothDevice) {
        // Preparar flags para nueva conexión
        manualDisconnect = false
        disconnectedNotified = false
        readJob?.cancel()

        ioScope.launch {
            try {
                @SuppressLint("MissingPermission")
                clientSocket = device.createRfcommSocketToServiceRecord(uuid)
                clientSocket?.connect()
                @SuppressLint("MissingPermission")
                Log.d(TAG, "Conexión exitosa con: ${device.name ?: "Sin nombre"} / ${device.address}")
                onConnected?.invoke()
                startReading(clientSocket)
            } catch (e: IOException) {
                Log.e(TAG, "Error conectando al dispositivo: ${e.message}", e)
                if (!manualDisconnect) notifyDisconnected()
            }
        }
    }

    // ---------------- SERVIDOR ----------------
    fun startServer() {
        // Preparar flags para nueva conexión
        manualDisconnect = false
        disconnectedNotified = false
        readJob?.cancel()

        ioScope.launch {
            try {
                @SuppressLint("MissingPermission")
                serverSocket = adapter?.listenUsingRfcommWithServiceRecord("PONG_APP", uuid)
                Log.d(TAG, "Servidor escuchando conexiones...")

                clientSocket = serverSocket?.accept()
                @SuppressLint("MissingPermission")
                Log.d(TAG, "Cliente conectado: ${clientSocket?.remoteDevice?.name ?: "Sin nombre"} / ${clientSocket?.remoteDevice?.address}")
                onConnected?.invoke()
                startReading(clientSocket)
            } catch (e: IOException) {
                Log.e(TAG, "Error en servidor: ${e.message}", e)
                if (!manualDisconnect) notifyDisconnected()
            }
        }
    }

    // ---------------- LECTURA DE DATOS ----------------
    private fun startReading(socket: BluetoothSocket?) {
        socket?.let { sock ->
            isReading = true
            readJob?.cancel()
            readJob = ioScope.launch {
                val buffer = ByteArray(1024)
                val inputStream = try { sock.inputStream } catch (e: IOException) { return@launch }

                while (isActive && isReading) {
                    val bytes = try { inputStream.read(buffer) } catch (e: IOException) { continue }
                    if (bytes > 0) {
                        val data = buffer.copyOf(bytes)
                        onDataReceived?.invoke(data)
                    }
                    // Ignorar read == -1 temporal
                }
                // solo notificar desconexión si fue manual
                if (manualDisconnect) notifyDisconnected()
            }
        }
    }

    // ---------------- ENVÍO DE DATOS ----------------
    fun sendBytes(data: ByteArray) {
        ioScope.launch {
            try {
                clientSocket?.outputStream?.write(data)
                Log.d(TAG, "Datos enviados (${data.size} bytes)")
            } catch (e: IOException) {
                Log.e(TAG, "Error enviando datos: ${e.message}", e)
                // Si falla el envío por cierre del socket y no fue manual, notifica
                if (!manualDisconnect) notifyDisconnected()
            }
        }
    }

    // ---------------- DESCONECTAR ----------------
    fun disconnect() {
        manualDisconnect = true
        isReading = false
        readJob?.cancel()
        readJob = null

        try { clientSocket?.close() } catch (e: IOException) {}
        try { serverSocket?.close() } catch (e: IOException) {}

        clientSocket = null
        serverSocket = null
        notifyDisconnected()
    }

    // ---------------- HACER DESCUBRIBLE ----------------
    fun makeDiscoverable(duration: Int = 120) {
        val adapter = adapter ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) return
        }
        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, duration)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        Log.d(TAG, "Haciendo dispositivo descubrible por $duration segundos")
    }

    fun getConnectedDeviceAddress(): String? = clientSocket?.remoteDevice?.address

    // ---------------- HELPERS ----------------
    private fun notifyDisconnected() {
        // aseguramos una única invocación
        if (disconnectedNotified) return
        disconnectedNotified = true
        try {
            onDisconnected?.invoke()
        } catch (t: Throwable) {
            Log.e(TAG, "onDisconnected lanzó excepción: ${t.message}", t)
        }
    }
}
*
 */
