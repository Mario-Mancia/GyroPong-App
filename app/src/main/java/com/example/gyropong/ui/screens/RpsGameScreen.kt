//Este archivo contiene la estructura y funcionalidades de la pantalla de juego.

package com.example.gyropong.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.gyropong.hardware.vibration.VibrationManager
import com.example.gyropong.hardware.sensors.accelerometer.AccelerometerManager
import com.example.gyropong.ui.viewmodels.BluetoothViewModel
import com.example.gyropong.ui.viewmodels.UserViewModel

@Composable
fun RpsGameScreen(
    bluetoothVM: BluetoothViewModel,
    accelerometerManager: AccelerometerManager,
    userViewModel: UserViewModel,
    nickname: String,
    avatar: Int,
    opponentNickname: String,
    opponentAvatar: Int,
    onExit: () -> Unit
) {
    val context = LocalContext.current
    val vibrationManager = remember { VibrationManager(context) }
    val scope = rememberCoroutineScope()

    var myScore by remember { mutableStateOf(0) }
    var opponentScore by remember { mutableStateOf(0) }
    var round by remember { mutableStateOf(1) }
    var timer by remember { mutableStateOf(5) }

    var myChoice by remember { mutableStateOf<String?>(null) }
    var opponentChoice by remember { mutableStateOf<String?>(null) }
    var resultMessage by remember { mutableStateOf("Agita para elegir") }

    // Encargada de invocar las propiedades del acelerómetro desde la clase.
    DisposableEffect(Unit) {
        accelerometerManager.start()
        onDispose { accelerometerManager.stop() }
    }

    val isFastMovement by accelerometerManager.isFastMovement.collectAsState()

    // Se encarga de asignar una opción aleatoria.
    LaunchedEffect(isFastMovement) {
        if (isFastMovement && myChoice == null) {
            val options = listOf("Piedra", "Papel", "Tijera")
            val choice = options.random()
            myChoice = choice
            bluetoothVM.sendRpsChoice(choice)
            vibrationManager.vibrateSoft()
        }
    }

    // Esta función captura la elección del rival.
    LaunchedEffect(Unit) {
        bluetoothVM.incomingRps.collect { choice ->
            opponentChoice = choice
            vibrationManager.vibrateMedium()
        }
    }

    // Timer
    LaunchedEffect(round) {
        timer = 5
        while (timer > 0) {
            delay(1000L)
            timer--
        }

        // Evaluación de los resultados
        when {
            myChoice != null && opponentChoice != null -> {
                when {
                    myChoice == opponentChoice -> {
                        resultMessage = "Empate"
                        vibrationManager.vibrateStrong()
                    }
                    (myChoice == "Piedra" && opponentChoice == "Tijera") ||
                            (myChoice == "Papel" && opponentChoice == "Piedra") ||
                            (myChoice == "Tijera" && opponentChoice == "Papel") -> {
                        resultMessage = "¡Ganaste!"
                        myScore++
                        vibrationManager.vibrateStrong()
                    }
                    else -> {
                        resultMessage = "Perdiste..."
                        opponentScore++
                        vibrationManager.vibrateStrong()
                    }
                }
            }
            myChoice == null && opponentChoice != null -> {
                resultMessage = "No agitaste. Perdiste la ronda"
                opponentScore++
                vibrationManager.vibrateStrong()
            }
            myChoice != null && opponentChoice == null -> {
                resultMessage = "Rival no agitó. Ganaste la ronda"
                myScore++
                vibrationManager.vibrateStrong()
            }
            else -> resultMessage = "Ronda inválida"
        }

        delay(2000L)

        if (round < 5) {
            round++
            myChoice = null
            opponentChoice = null
            resultMessage = "Agita para elegir"
        } else {
            // Partida finalizada
            resultMessage = when {
                myScore > opponentScore -> "Victoria final"
                myScore < opponentScore -> "Derrota final"
                else -> "Empate final"
            }

            // Suma puntos al usuario según resultado
            userViewModel.currentUser.value?.let { user ->
                val pointsToAdd = when {
                    myScore > opponentScore -> 3
                    myScore == opponentScore -> 1
                    else -> 0
                }
                if (pointsToAdd > 0) {
                    userViewModel.addPoints(user.id, pointsToAdd)
                }
            }

            delay(3000L)
            Log.d("RpsGameScreen", "Juego terminado, saliendo...")
            onExit()
        }
    }

    // Sección de diseño de la pantalla:
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF6843A8), Color(0xFFE91E63)))
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Ronda $round / 5", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = timer / 5f,
                color = if (timer > 2) Color(0xFF4CAF50) else Color(0xFFE91E63),
                strokeWidth = 8.dp,
                modifier = Modifier.size(80.dp)
            )
            Text("$timer", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(24.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C54)),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ScoreBoardModern(name = nickname, avatarRes = avatar, score = myScore)
                ScoreBoardModern(name = opponentNickname, avatarRes = opponentAvatar, score = opponentScore, isOpponent = true)
            }
        }

        Spacer(Modifier.height(32.dp))

        Text("Tu elección: ${myChoice ?: "?"}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("Rival: ${opponentChoice ?: "?"}", color = Color(0xFFFFD700), fontSize = 18.sp, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(20.dp))

        val resultColor = when {
            "Ganaste" in resultMessage || "Victoria" in resultMessage -> Color(0xFF4CAF50)
            "Perdiste" in resultMessage || "Derrota" in resultMessage -> Color(0xFFE91E63)
            "Empate" in resultMessage -> Color(0xFFFFC107)
            else -> Color.Gray
        }
        Card(
            colors = CardDefaults.cardColors(containerColor = resultColor),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(resultMessage, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = {
                Log.d("RpsGameScreen", "Salir presionado")
                onExit()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth(0.7f).height(55.dp)
        ) {
            Text("Salir", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// Composable individual para recolectar los datos de los usuarios.
@Composable
private fun ScoreBoardModern(name: String, avatarRes: Int, score: Int, isOpponent: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            painter = painterResource(id = avatarRes),
            contentDescription = "Avatar",
            tint = Color.Unspecified,
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            name,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            repeat(5) { i ->
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .padding(2.dp)
                        .background(
                            if (i < score) if (isOpponent) Color.Red else Color.Green else Color.Gray,
                            CircleShape
                        )
                )
            }
        }
    }
}