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
import com.example.gyropong.ui.viewmodels.BluetoothViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

/*
@Composable
fun GyroPongGameScreen(
    bluetoothVM: BluetoothViewModel, // 👈 ahora se recibe desde afuera
    nickname: String,
    initialOpponentNickname: String,
    initialIsConnected: Boolean,
    onExit: () -> Unit
) {
    val isConnected by bluetoothVM.isConnected.collectAsState()
    val opponentNickname by bluetoothVM.opponentNickname.collectAsState()
    val startSignalReceived by bluetoothVM.startSignalReceived.collectAsState()

    // Log cada vez que cambie el estado
    LaunchedEffect(isConnected, opponentNickname, startSignalReceived) {
        Log.d(
            "GyroPongGameScreen",
            "Estado actualizado -> isConnected: $isConnected | " +
                    "Oponente: ${opponentNickname ?: initialOpponentNickname} | " +
                    "startSignalReceived: $startSignalReceived"
        )
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