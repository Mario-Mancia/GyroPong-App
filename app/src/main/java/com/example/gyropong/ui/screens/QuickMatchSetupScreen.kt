package com.example.gyropong.ui.screens

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