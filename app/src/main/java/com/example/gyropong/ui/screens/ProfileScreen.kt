// Pequeño composable para una pantalla de perfil (muy básica)

package com.example.gyropong.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.gyropong.ui.navigation.Screen
import com.example.gyropong.ui.viewmodels.SessionViewModel
import com.example.gyropong.ui.viewmodels.UserViewModel
import kotlinx.coroutines.launch
import com.example.gyropong.hardware.vibration.VibrationManager
import com.example.gyropong.ui.components.GameButton

@Composable
fun ProfileScreen(
    navController: NavHostController,
    sessionViewModel: SessionViewModel,
    userViewModel: UserViewModel
) {
    val scope = rememberCoroutineScope()
    val currentUser by userViewModel.currentUser.collectAsState()
    val avatar = "🐱"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF6843A8))
            .padding(16.dp)
    ) {
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Volver",
                tint = Color.White
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Perfil de usuario",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp,
                    color = Color.White
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            currentUser?.let { user ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Usuario: ${user.username}", fontSize = 20.sp, color = Color.White)
                    Text("Email: ${user.email}", fontSize = 16.sp, color = Color.White)
                    Text("Puntos: ${user.userPoints}", fontSize = 16.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(64.dp))

            GameButton(
                text = "Cerrar sesión",
                onClick = {
                    scope.launch {
                        currentUser?.let { user ->
                            sessionViewModel.logout(user.id)
                            userViewModel.logout()
                        }
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
                enabled = true
            )
        }
    }
}
