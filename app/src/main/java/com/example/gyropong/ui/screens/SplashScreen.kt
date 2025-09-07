package com.example.gyropong.ui.screens

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.gyropong.ui.navigation.Screen
import com.example.gyropong.ui.viewmodels.SessionViewModel
import com.example.gyropong.ui.viewmodels.UserViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    navController: NavHostController,
    sessionViewModel: SessionViewModel,
    userViewModel: UserViewModel,
    splashTime: Long = 2000L
) {
    val currentUser by userViewModel.currentUser.collectAsState()
    val userAvatar = "🐱"

    // UI simple de splash
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "GyroPong",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold
        )
    }

    LaunchedEffect(Unit) {
        // Cargar sesión activa
        sessionViewModel.loadActiveSession()

        // Esperar un poco para el splash
        delay(splashTime)

        // Revisar si hay sesión activa
        val activeSession = sessionViewModel.activeSession.value
        if (activeSession != null) {
            // Cargar usuario correspondiente
            userViewModel.loadUserById(activeSession.userId)

            // Esperar hasta que currentUser se actualice
            while (userViewModel.currentUser.value == null) {
                delay(50)
            }

            // Navegar a FindMatch
            val user = userViewModel.currentUser.value!!
            navController.navigate("${Screen.FindMatch.route}/${user.username}/$userAvatar") {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        } else {
            // Si no hay una sesión entondes redirije a Home
            navController.navigate(Screen.Home.route) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }
}
