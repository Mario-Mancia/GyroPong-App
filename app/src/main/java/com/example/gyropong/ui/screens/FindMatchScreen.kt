package com.example.gyropong.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.room.util.TableInfo
import com.example.gyropong.domain.models.User
import com.example.gyropong.ui.components.GuestTopBar
import com.example.gyropong.ui.components.SessionTopBar
import com.example.gyropong.ui.navigation.Screen
import kotlinx.coroutines.launch

@Composable
fun FindMatchScreen(
    currentUser: User?, // null si es guest
    nickname: String,
    avatar: String,
    navController: NavHostController,
) {
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- TOP BAR ---
        if (currentUser != null) {
            // Usuario logueado
            SessionTopBar(
                username = currentUser.username,
                avatar = "🐱",
                points = currentUser.userPoints,
                onProfileClick = {
                    scope.launch {
                        navController.navigate(Screen.Profile.route)
                    }
                }
            )
        } else {
            // Invitado
            GuestTopBar(
                nickname = nickname,
                avatar = avatar,
                onLogoutClick = {
                    scope.launch {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- CONTENIDO PRINCIPAL ---
        Text(
            text = "Pantalla de emparejamiento",
            style = TextStyle(
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Ejemplo de botón para iniciar juego
        Button(onClick = {
            navController.navigate(Screen.Game.route)
        }) {
            Text("Iniciar Juego")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Información adicional del usuario logueado
        currentUser?.let { user ->
            Text("Email: ${user.email}", fontSize = 16.sp)
        }
    }
}