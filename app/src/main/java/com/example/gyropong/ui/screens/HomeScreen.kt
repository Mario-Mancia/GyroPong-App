// Este archivo contiene la estructura y funcionalidad de la pantalla de inicio o 'Home'
package com.example.gyropong.ui.screens

import android.graphics.fonts.FontFamily
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gyropong.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.gyropong.ui.components.GameButton
import com.example.gyropong.hardware.vibration.VibrationManager


val gameFont = androidx.compose.ui.text.font.FontFamily.Default

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onQuickMatchClick: () -> Unit,
    onSessionClick: () -> Unit
) {
    val gradientBackground = Brush.verticalGradient(
        colors = listOf(Color(0xFF3F51B5), Color(0xFF673AB7))
    )

    val context = LocalContext.current
    val vibrationManager = remember { VibrationManager(context) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(gradientBackground)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título
        Text(
            text = "JanKenPon!",
            style = TextStyle(
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = gameFont,
                color = Color.White,
                letterSpacing = 2.sp
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Botón de partida rápida
        GameButton(
            text = "PARTIDA RÁPIDA",
            onClick = onQuickMatchClick,
            enabled = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Botón de sesión
        GameButton(
            text = "INICIAR SESIÓN / REGISTRO",
            onClick = onSessionClick ,
            enabled = true
        )
    }
}