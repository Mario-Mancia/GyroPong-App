package com.example.gyropong.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.gyropong.ui.navigation.Screen
import com.example.gyropong.ui.viewmodels.SessionViewModel
import com.example.gyropong.ui.viewmodels.UserViewModel
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    navController: NavHostController,
    sessionViewModel: SessionViewModel,
    userViewModel: UserViewModel
) {
    val scope = rememberCoroutineScope()
    val currentUser by userViewModel.currentUser.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Perfil de usuario",
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 36.sp
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        currentUser?.let { user ->
            Text("Usuario: ${user.username}", fontSize = 20.sp)
            Text("Email: ${user.email}", fontSize = 16.sp)
            Text("Puntos: ${user.userPoints}", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(64.dp))

        Button(
            onClick = {
                scope.launch {
                    // 1️⃣ Cerrar sesión en el SessionViewModel
                    currentUser?.let { user ->
                        sessionViewModel.logout(user.id)
                        userViewModel.logout()
                    }

                    // 2️⃣ Limpiar stack y regresar a Home
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        ) {
            Text(text = "Cerrar sesión")
        }
    }
}