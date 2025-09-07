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
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.util.UUID

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

    private var isReading = true

    /** ----------- CLIENTE ----------- **/
    fun connectToDevice(device: BluetoothDevice) {
        ioScope.launch {
            try {
                val name = if (
                    ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) == PackageManager.PERMISSION_GRANTED
                ) device.name ?: "Sin nombre" else "Nombre no disponible"

                Log.d(TAG, "Intentando conectar al dispositivo: $name / ${device.address}")

                @SuppressLint("MissingPermission")
                clientSocket = device.createRfcommSocketToServiceRecord(uuid)
                clientSocket?.connect()

                Log.d(TAG, "Conexión exitosa con: $name")
                onConnected?.invoke()
                readFromSocket(clientSocket)
            } catch (e: IOException) {
                Log.e(TAG, "Error conectando al dispositivo: ${e.message}", e)
                onDisconnected?.invoke()
            }
        }
    }

    /** ----------- SERVIDOR ----------- **/
    fun startServer() {
        ioScope.launch {
            try {
                @SuppressLint("MissingPermission")
                serverSocket = adapter?.listenUsingRfcommWithServiceRecord("PONG_APP", uuid)
                Log.d(TAG, "Servidor escuchando conexiones...")

                clientSocket = serverSocket?.accept()

                val clientName = clientSocket?.remoteDevice?.let { device ->
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) == PackageManager.PERMISSION_GRANTED
                    ) device.name ?: "Sin nombre" else "Nombre no disponible"
                }

                Log.d(TAG, "Cliente conectado: $clientName")
                onConnected?.invoke()
                readFromSocket(clientSocket)
            } catch (e: IOException) {
                Log.e(TAG, "Error en servidor: ${e.message}", e)
                onDisconnected?.invoke()
            }
        }
    }

    /** ----------- LECTURA DE DATOS ----------- **/
    private fun readFromSocket(socket: BluetoothSocket?) {
        socket?.let {
            isReading = true
            val inputStream: InputStream = it.inputStream
            val buffer = ByteArray(1024)
            try {
                Log.d(TAG, "Iniciando lectura de datos desde socket...")
                while (isReading) {
                    val bytes = inputStream.read(buffer)
                    if (bytes > 0) {
                        val data = buffer.copyOf(bytes)
                        onDataReceived?.invoke(data)
                        Log.d(TAG, "Datos recibidos (${bytes} bytes)")
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error leyendo datos: ${e.message}", e)
                onDisconnected?.invoke()
            }
        }
    }

    /** ----------- ENVÍO DE DATOS ----------- **/
    fun sendBytes(data: ByteArray) {
        ioScope.launch {
            try {
                clientSocket?.outputStream?.write(data)
                Log.d(TAG, "Datos enviados (${data.size} bytes)")
            } catch (e: IOException) {
                Log.e(TAG, "Error enviando datos: ${e.message}", e)
            }
        }
    }

    /** ----------- DESCONECTAR ----------- **/
    fun disconnect() {
        isReading = false
        try {
            clientSocket?.close()
            serverSocket?.close()
            clientSocket = null
            serverSocket = null
            Log.d(TAG, "Sockets cerrados, conexión terminada")
            onDisconnected?.invoke()
        } catch (e: IOException) {
            Log.e(TAG, "Error cerrando conexión: ${e.message}", e)
            onDisconnected?.invoke()
        }
    }

    /** ----------- HACER DESCUBRIBLE ----------- **/
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

    /** ----------- HELPER PÚBLICO ----------- **/
    fun getConnectedDeviceAddress(): String? = clientSocket?.remoteDevice?.address
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

    /** ----------- CLIENTE ----------- **/
    fun connectToDevice(device: BluetoothDevice) {
        ioScope.launch {
            try {
                val name = if (
                    ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    device.name ?: "Sin nombre"
                } else {
                    "Nombre no disponible"
                }

                Log.d(TAG, "Intentando conectar al dispositivo: $name / ${device.address}")

                @SuppressLint("MissingPermission")
                clientSocket = device.createRfcommSocketToServiceRecord(uuid)
                clientSocket?.connect()

                Log.d(TAG, "Conexión exitosa con: $name")
                onConnected?.invoke()
                readFromSocket(clientSocket)
            } catch (e: IOException) {
                Log.e(TAG, "Error conectando al dispositivo: ${e.message}", e)
                onDisconnected?.invoke()
            }
        }
    }

    /** ----------- SERVIDOR ----------- **/
    fun startServer() {
        ioScope.launch {
            try {
                @SuppressLint("MissingPermission")
                serverSocket = adapter?.listenUsingRfcommWithServiceRecord("PONG_APP", uuid)
                Log.d(TAG, "Servidor escuchando conexiones...")

                clientSocket = serverSocket?.accept()

                val clientName = clientSocket?.remoteDevice?.let { device ->
                    if (
                        ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        device.name ?: "Sin nombre"
                    } else {
                        "Nombre no disponible"
                    }
                }

                Log.d(TAG, "Cliente conectado: $clientName")
                onConnected?.invoke()
                readFromSocket(clientSocket)
            } catch (e: IOException) {
                Log.e(TAG, "Error en servidor: ${e.message}", e)
                onDisconnected?.invoke()
            }
        }
    }

    /** ----------- LECTURA DE DATOS ----------- **/
    private fun readFromSocket(socket: BluetoothSocket?) {
        socket?.let {
            val inputStream: InputStream = it.inputStream
            val buffer = ByteArray(1024)
            try {
                Log.d(TAG, "Iniciando lectura de datos desde socket...")
                while (true) {
                    val bytes = inputStream.read(buffer)
                    if (bytes > 0) {
                        val data = buffer.copyOf(bytes)
                        Log.d(TAG, "Datos recibidos (${bytes} bytes)")
                        onDataReceived?.invoke(data)
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error leyendo datos: ${e.message}", e)
                onDisconnected?.invoke()
            }
        }
    }

    /** ----------- ENVÍO DE DATOS ----------- **/
    fun sendBytes(data: ByteArray) {
        ioScope.launch {
            try {
                clientSocket?.outputStream?.write(data)
                Log.d(TAG, "Datos enviados (${data.size} bytes)")
            } catch (e: IOException) {
                Log.e(TAG, "Error enviando datos: ${e.message}", e)
            }
        }
    }

    /** ----------- DESCONECTAR ----------- **/
    fun disconnect() {
        try {
            clientSocket?.close()
            serverSocket?.close()
            Log.d(TAG, "Sockets cerrados, conexión terminada")
            onDisconnected?.invoke()
        } catch (e: IOException) {
            Log.e(TAG, "Error cerrando conexión: ${e.message}", e)
            onDisconnected?.invoke()
        }
    }

    /** ----------- HACER DESCUBRIBLE ----------- **/
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

    /** ----------- HELPER PÚBLICO ----------- **/
    fun getConnectedDeviceAddress(): String? {
        return clientSocket?.remoteDevice?.address
    }
}
*/
