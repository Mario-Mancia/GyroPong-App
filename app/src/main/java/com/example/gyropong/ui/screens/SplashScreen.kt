package com.example.gyropong.ui.screens

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.gyropong.R
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
    val defaultAvatarRes = R.drawable.avatar_frog

    // UI simple de splash

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Fondo con drawable
        Image(
            painter = painterResource(id = R.drawable.chatgpt_image_sep_9__2025__06_01_55_pm), // tu drawable
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // ajusta según necesidad
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Texto grande y bold encima de la imagen
            Text(
                text = "JanKenPon!",
                fontSize = 64.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                modifier = Modifier
                    .background(Color(0x80000000), RoundedCornerShape(12.dp)) // opcional para resaltar
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Animación de cargando
            CircularProgressIndicator(
                color = Color.Yellow,
                strokeWidth = 4.dp,
                modifier = Modifier.size(48.dp)
            )
        }
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
            navController.navigate("${Screen.FindMatch.route}/${user.username}/$defaultAvatarRes") {
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
