package com.example.gyropong.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onQuickMatchClick: () -> Unit,
    onSessionClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Bienvenido a GyroPong",
            style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onQuickMatchClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Partida rápida")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onSessionClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Iniciar sesión / Registro")
        }
    }
}

@Composable
@Preview(showSystemUi = true)
fun HomeScreenPreview() {
    HomeScreen(
        onQuickMatchClick = {},
        onSessionClick = {}
    )
}