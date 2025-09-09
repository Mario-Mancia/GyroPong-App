package com.example.gyropong.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.room.util.TableInfo
import com.example.gyropong.domain.models.User
import com.example.gyropong.ui.components.GuestTopBar
import com.example.gyropong.ui.components.SessionTopBar
import com.example.gyropong.ui.navigation.Screen
import com.example.gyropong.hardware.bluetooth.BluetoothHelper
import com.example.gyropong.hardware.bluetooth.BluetoothDiscovery
import com.example.gyropong.hardware.bluetooth.BluetoothConnection
import com.example.gyropong.ui.viewmodels.BluetoothViewModel
import com.example.gyropong.ui.viewmodels.BluetoothViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.provider.Settings
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.core.app.ActivityCompat
import com.example.gyropong.ui.components.GameButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.time.withTimeoutOrNull
import kotlinx.coroutines.withTimeoutOrNull

@Composable
fun FindMatchScreen(
    bluetoothVM: BluetoothViewModel, // 👈 ahora se recibe desde afuera
    nickname: String,
    avatar: String,
    currentUser: User?,
    navController: NavHostController
) {
    val devices by bluetoothVM.devices.collectAsState()
    val isConnected by bluetoothVM.isConnected.collectAsState()
    val startSignalReceived by bluetoothVM.startSignalReceived.collectAsState()
    val nicknameMap by bluetoothVM.deviceNicknames.collectAsState()
    val opponentNickname by bluetoothVM.opponentNickname.collectAsState()

    var isHosting by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val showCancel = isHosting || isSearching

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF6843A8))
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (currentUser != null) {
            SessionTopBar(
                username = currentUser.username,
                avatar = avatar,
                points = currentUser.userPoints,
                onProfileClick = { navController.navigate(Screen.Profile.route) }
            )
        } else {
            GuestTopBar(
                nickname = nickname,
                avatar = avatar,
                onLogoutClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        Spacer(Modifier.height(24.dp))
        Text("Ingresa nickname y avatar para ser encontrado", color = Color.White)
        Spacer(Modifier.height(16.dp))

        // --- BOTONES ---
        GameButton(
            text = "Crear sala",
            onClick = {
                bluetoothVM.makeDiscoverable()
                bluetoothVM.startServer(currentUser?.username ?: nickname)
                isHosting = true
            },
            enabled = !isHosting && !isSearching
        )

        Spacer(Modifier.height(12.dp))

        GameButton(
            text = "Buscar partida",
            onClick = {
                bluetoothVM.startDiscovery()
                isSearching = true
            },
            enabled = !isHosting && !isSearching
        )

        Spacer(Modifier.height(24.dp))

        if (isSearching) {
            if (devices.isEmpty()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color.Yellow)
                    Spacer(Modifier.height(12.dp))
                    Text("Buscando jugadores...", color = Color.White)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    items(devices) { device ->
                        @SuppressLint("MissingPermission")
                        val displayName = nicknameMap[device.address] ?: device.name ?: "Sin nombre"
                        var pressed by remember { mutableStateOf(false) }
                        val scale by animateFloatAsState(if (pressed) 0.95f else 1f)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(displayName, color = Color.White, fontSize = 16.sp)
                                Text(device.address, color = Color.Gray, fontSize = 12.sp)
                            }
                            Box(
                                modifier = Modifier
                                    .width(120.dp)
                                    .height(48.dp)
                                    .graphicsLayer { scaleX = scale; scaleY = scale }
                                    .shadow(4.dp, RoundedCornerShape(16.dp))
                                    .background(Color(0xFFFFD700), RoundedCornerShape(16.dp))
                                    .clickable {
                                        pressed = true
                                        scope.launch {
                                            bluetoothVM.connect(device, currentUser?.username ?: nickname)
                                            delay(100)
                                            pressed = false
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Conectar",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showCancel) {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    bluetoothVM.disconnect()
                    bluetoothVM.stopDiscovery()
                    isHosting = false
                    isSearching = false
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
            ) { Text("CANCELAR", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold) }
        }

        // --- Navegación a juego ---
        LaunchedEffect(isConnected, startSignalReceived, opponentNickname) {
            if (isConnected && startSignalReceived && opponentNickname != null) {
                Log.d(
                    "FindMatchScreen",
                    "Navegando a GyroPongGameScreen -> Opponent: $opponentNickname"
                )
                navController.navigate("${Screen.GyroPongGame.route}/$nickname/$opponentNickname") {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            } else {
                Log.d(
                    "FindMatchScreen",
                    "Esperando señal de inicio... isConnected=$isConnected startSignal=$startSignalReceived opponent=$opponentNickname"
                )
            }
        }
    }
}


/*
@Composable
fun FindMatchScreen(
    bluetoothVM: BluetoothViewModel = viewModel(),
    nickname: String,
    avatar: String,
    currentUser: User?,
    navController: NavHostController
) {
    val devices by bluetoothVM.devices.collectAsState()
    val isConnected by bluetoothVM.isConnected.collectAsState()
    val startSignalReceived by bluetoothVM.startSignalReceived.collectAsState()
    val nicknameMap by bluetoothVM.deviceNicknames.collectAsState()
    val opponentNickname by bluetoothVM.opponentNickname.collectAsState()

    var isHosting by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val showCancel = isHosting || isSearching

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF6843A8)).padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (currentUser != null) {
            SessionTopBar(
                username = currentUser.username,
                avatar = avatar,
                points = currentUser.userPoints,
                onProfileClick = { navController.navigate(Screen.Profile.route) }
            )
        } else {
            GuestTopBar(
                nickname = nickname,
                avatar = avatar,
                onLogoutClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        Spacer(Modifier.height(24.dp))
        Text("Ingresa nickname y avatar para ser encontrado", color = Color.White)
        Spacer(Modifier.height(16.dp))

        // --- BOTONES ---
        GameButton(
            text = "Crear sala",
            onClick = {
                bluetoothVM.makeDiscoverable()
                bluetoothVM.startServer(currentUser?.username ?: nickname)
                isHosting = true
            },
            enabled = !isHosting && !isSearching
        )

        Spacer(Modifier.height(12.dp))

        GameButton(
            text = "Buscar partida",
            onClick = {
                bluetoothVM.startDiscovery()
                isSearching = true
            },
            enabled = !isHosting && !isSearching
        )

        Spacer(Modifier.height(24.dp))

        if (isSearching) {
            if (devices.isEmpty()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color.Yellow)
                    Spacer(Modifier.height(12.dp))
                    Text("Buscando jugadores...", color = Color.White)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    items(devices) { device ->
                        @SuppressLint("MissingPermission")
                        val displayName = nicknameMap[device.address] ?: device.name ?: "Sin nombre"
                        var pressed by remember { mutableStateOf(false) }
                        val scale by animateFloatAsState(if (pressed) 0.95f else 1f)

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(displayName, color = Color.White, fontSize = 16.sp)
                                Text(device.address, color = Color.Gray, fontSize = 12.sp)
                            }
                            Box(
                                modifier = Modifier.width(120.dp).height(48.dp)
                                    .graphicsLayer { scaleX = scale; scaleY = scale }
                                    .shadow(4.dp, RoundedCornerShape(16.dp))
                                    .background(Color(0xFFFFD700), RoundedCornerShape(16.dp))
                                    .clickable {
                                        pressed = true
                                        scope.launch {
                                            bluetoothVM.connect(device, currentUser?.username ?: nickname)
                                            delay(100)
                                            pressed = false
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Conectar", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }

        if (showCancel) {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { bluetoothVM.disconnect(); bluetoothVM.stopDiscovery(); isHosting=false; isSearching=false },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.Black),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth().height(70.dp)
            ) { Text("CANCELAR", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold) }
        }

        // --- Navegación a juego ---
        LaunchedEffect(isConnected, startSignalReceived, opponentNickname) {
            if (isConnected && startSignalReceived && opponentNickname != null) {
                Log.d("FindMatchScreen", "Navegando a GyroPongGameScreen -> Opponent: $opponentNickname")
                navController.navigate("${Screen.GyroPongGame.route}/$nickname/$opponentNickname") {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            } else {
                Log.d("FindMatchScreen", "Esperando señal de inicio... isConnected=$isConnected startSignal=$startSignalReceived opponent=$opponentNickname")
            }
        }

        /*
        LaunchedEffect(isConnected, startSignalReceived, opponentNickname) {
            if (isConnected && startSignalReceived && opponentNickname != null) {
                navController.navigate("${Screen.GyroPongGame.route}/$nickname/$opponentNickname") {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }*/
    }
}
*/