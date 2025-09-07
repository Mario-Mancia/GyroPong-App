package com.example.gyropong.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp

@Composable
fun GuestTopBar(
    nickname: String,
    avatar: String,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Avatar a la izquierda
        Text(
            text = avatar,
            style = MaterialTheme.typography.headlineSmall
        )

        // Nickname centrado
        Text(
            text = nickname,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )

        // Botón de salir a la derecha
        IconButton(onClick = onLogoutClick) {
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = "Salir"
            )
        }
    }
}