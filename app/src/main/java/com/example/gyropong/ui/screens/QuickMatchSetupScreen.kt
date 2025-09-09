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

@Composable
fun QuickMatchSetupScreen(
    modifier: Modifier = Modifier,
    avatars: List<Int>,
    onBack: () -> Unit,
    onContinue: (nickname: String, avatar: Int) -> Unit
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
        // Botón de retroceder
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

            // OutlinedTextField al estilo SessionScreen
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
                    disabledTextColor = Color.White,
                    errorTextColor = Color.Red,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    errorContainerColor = Color.Transparent,
                    cursorColor = Color.Yellow,
                    errorCursorColor = Color.Red,
                    focusedBorderColor = Color(0xFFFFD700),
                    unfocusedBorderColor = Color(0x99FFFFFF),
                    disabledBorderColor = Color.Transparent,
                    errorBorderColor = Color.Red,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White,
                    disabledLabelColor = Color.White,
                    errorLabelColor = Color.Red
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

/*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun QuickMatchSetupScreen(
    modifier: Modifier = Modifier,
    avatars: List<String> = listOf("🐱","🐶","🐵","🦊","🦁","🐸"),
    onBack: () -> Unit,
    onContinue: (nickname: String, avatar: String) -> Unit
) {
    var nickname by remember { mutableStateOf(TextFieldValue("")) }
    var selectedAvatar by remember { mutableStateOf(avatars.first()) }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp)
    ) {
        // Botón retroceder arriba
        IconButton(onClick = onBack) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Volver")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = { Text("Nickname") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text("Selecciona un avatar")
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow {
                items(avatars) { avatar ->
                    Text(
                        text = avatar,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(64.dp)
                            .clickable { selectedAvatar = avatar },
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onContinue(nickname.text, selectedAvatar) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continuar")
            }
        }
    }
}


@Composable
@Preview(showSystemUi = true)
fun QuickMatchSetupScreenPreview() {
    QuickMatchSetupScreen(
        onBack = {}, // para preview, no hace nada
        onContinue = { nickname, avatar -> /* demo */ }
    )
}
*/