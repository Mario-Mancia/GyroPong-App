package com.example.gyropong.ui.screens

import android.content.ContentValues.TAG
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.gyropong.hardware.sensors.gyroscope.GyroscopeManager
import com.example.gyropong.ui.viewmodels.BluetoothViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.example.gyropong.ui.components.Ball
import kotlin.math.abs
import com.example.gyropong.hardware.vibration.VibrationManager

@Composable
fun GyroPongGameScreen(
    bluetoothVM: BluetoothViewModel,
    gyroscopeManager: GyroscopeManager,
    nickname: String,
    initialOpponentNickname: String,
    onExit: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val TAG = "GyroPong"

    // --- Dimensiones relativas ---
    val paddleWidthFactor = 0.25f
    val paddleHeight = 30f
    val ballRadius = 15f

    // --- Estado ---
    var paddleX by remember { mutableStateOf(0f) }
    var ball by remember { mutableStateOf<Ball?>(null) }
    var hasBall by remember { mutableStateOf(false) }

    val screenWidth = remember { mutableStateOf(0f) }
    val screenHeight = remember { mutableStateOf(0f) }

    // --- Giroscopio ---
    var rotationY by remember { mutableStateOf(0f) }
    DisposableEffect(Unit) {
        gyroscopeManager.start()
        onDispose { gyroscopeManager.stop() }
    }
    LaunchedEffect(Unit) {
        gyroscopeManager.yRotation.collect { value -> rotationY = value }
    }

    // Paddle horizontal
    LaunchedEffect(rotationY, screenWidth.value) {
        val sensitivity = 25f
        val speed = rotationY * sensitivity
        val paddleWidth = screenWidth.value * paddleWidthFactor
        paddleX = (paddleX + speed).coerceIn(0f, screenWidth.value - paddleWidth)
    }

    // --- Host inicializa la bola ---
    if (bluetoothVM.isHost && ball == null && screenWidth.value > 0f) {
        ball = Ball(
            x = screenWidth.value / 2f,
            y = screenHeight.value / 2f,
            vx = 8f,
            vy = 10f
        )
        hasBall = true
        Log.d(TAG, "[INIT] Host creó la bola en (${ball!!.x}, ${ball!!.y})")
    }

    // --- Actualización local ---
    LaunchedEffect(ball, hasBall) {
        while (true) {
            if (hasBall && ball != null) {
                val paddleWidth = screenWidth.value * paddleWidthFactor
                var newX = ball!!.x + ball!!.vx
                var newY = ball!!.y + ball!!.vy
                var vx = ball!!.vx
                var vy = ball!!.vy

                // Rebote lateral
                if (newX <= 0f || newX >= screenWidth.value - ballRadius * 2) {
                    vx = -vx
                    Log.d(TAG, "[BOUNCE] Rebote lateral")
                }

                // Rebote paddle
                if (newY >= screenHeight.value - 50f &&
                    newX in paddleX..(paddleX + paddleWidth)
                ) {
                    vy = -abs(vy)
                    Log.d(TAG, "[BOUNCE] Rebote en paddle en X=$newX")
                }

                // Transferencia al rival
                if (newY <= 0f) {
                    val normalizedBall = Ball(
                        x = newX / screenWidth.value,
                        y = 0f,
                        vx = vx / screenWidth.value,
                        vy = abs(vy) / screenHeight.value
                    )
                    bluetoothVM.sendBall(normalizedBall)
                    Log.d(TAG, "[SEND] Bola enviada al rival: $normalizedBall")

                    // 👇 cortar de inmediato
                    ball = null
                    hasBall = false
                    Log.d(TAG, "[STATE] Host suelta la bola")
                } else {
                    ball = Ball(newX, newY, vx, vy)
                }
            }
            delay(16L)
        }
    }

    // --- Cliente recibe bola ---
    LaunchedEffect(Unit) {
        bluetoothVM.incomingBall.collect { incoming ->
            Log.d(TAG, "[RECV] Cliente recibió bola normalizada: $incoming")

            ball = Ball(
                x = incoming.x * screenWidth.value,
                y = incoming.y * screenHeight.value,
                vx = incoming.vx * screenWidth.value,
                vy = incoming.vy * screenHeight.value
            )
            hasBall = true
            Log.d(TAG, "[STATE] Cliente ahora tiene la bola en (${ball!!.x}, ${ball!!.y})")
        }
    }

    // --- Desconexión automática ---
    val isConnected by bluetoothVM.isConnected.collectAsState()
    LaunchedEffect(isConnected) {
        if (!isConnected) {
            Log.d(TAG, "[DISCONNECT] Jugador desconectado")
            onExit()
        }
    }

    // --- UI ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            screenWidth.value = size.width
            screenHeight.value = size.height
            val paddleWidth = size.width * paddleWidthFactor

            // Bola
            ball?.let {
                drawCircle(
                    Color.White,
                    radius = ballRadius,
                    center = Offset(it.x, it.y)
                )
            }

            // Paddle
            drawRect(
                Color.Green,
                topLeft = Offset(paddleX, size.height - 50f),
                size = androidx.compose.ui.geometry.Size(paddleWidth, paddleHeight)
            )
        }

        Button(
            onClick = {
                bluetoothVM.disconnect()
                onExit()
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Text("Salir")
        }
    }
}



/*
@Composable
fun GyroPongGameScreen(
    bluetoothVM: BluetoothViewModel,
    nickname: String,
    initialOpponentNickname: String,
    initialIsConnected: Boolean,
    onExit: () -> Unit
) {
    val isConnected by bluetoothVM.isConnected.collectAsState()
    val opponentNickname by bluetoothVM.opponentNickname.collectAsState()
    val startSignalReceived by bluetoothVM.startSignalReceived.collectAsState()

    // Log inmediato cada vez que cambie el estado
    LaunchedEffect(isConnected, opponentNickname, startSignalReceived) {
        Log.d(
            "GyroPongGameScreen",
            "Estado actualizado -> isConnected: $isConnected | " +
                    "Oponente: ${opponentNickname ?: initialOpponentNickname} | " +
                    "startSignalReceived: $startSignalReceived"
        )
    }

    // 👇 Log periódico cada 5 segundos
    LaunchedEffect(Unit) {
        while (true) {
            Log.d(
                "GyroPongGameScreen",
                "[Heartbeat] Estado actual -> isConnected: $isConnected | " +
                        "Oponente: ${opponentNickname ?: initialOpponentNickname} | " +
                        "startSignalReceived: $startSignalReceived"
            )
            delay(5000) // 5 segundos
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Tu nickname: $nickname",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Oponente: ${opponentNickname ?: initialOpponentNickname}",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (isConnected) "Conexión establecida ✅" else "Conexión no establecida ❌",
            style = MaterialTheme.typography.bodyLarge,
            color = if (isConnected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onExit) { Text("Salir") }
    }
}
*/