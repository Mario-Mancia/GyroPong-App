package com.example.gyropong.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.gyropong.ui.components.AvatarSelector
import com.example.gyropong.ui.components.GameButton
import com.example.gyropong.hardware.vibration.VibrationManager

@Composable
fun QuickMatchSetupScreen(
    modifier: Modifier = Modifier,
    avatars: List<Int>,
    onBack: () -> Unit,
    onContinue: (nickname: String, avatarRes: Int) -> Unit
) {
    var nickname by remember { mutableStateOf(TextFieldValue("")) }
    var selectedAvatar by remember { mutableStateOf<Int?>(null) }

    val gradientBackground = Brush.verticalGradient(
        colors = listOf(Color(0xFF3F51B5), Color(0xFF673AB7))
    )

    val isContinueEnabled = nickname.text.trim().length >= 5 && selectedAvatar != null

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(gradientBackground)
            .padding(16.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                "Debes ingresar tu nickname y avatar para ser encontrado",
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = { Text("Nickname") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(Color(0x33FFFFFF), RoundedCornerShape(8.dp)),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.Yellow,
                    focusedBorderColor = Color(0xFFFFD700),
                    unfocusedBorderColor = Color(0x99FFFFFF),
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Selecciona un avatar", color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))

            AvatarSelector(
                avatars = avatars,
                selectedAvatar = selectedAvatar ?: avatars.first(),
                onAvatarSelected = { selectedAvatar = it }
            )

            Spacer(modifier = Modifier.height(32.dp))

            GameButton(
                text = "Continuar",
                onClick = { onContinue(nickname.text.trim(), selectedAvatar!!) },
                enabled = isContinueEnabled
            )
        }
    }
}
