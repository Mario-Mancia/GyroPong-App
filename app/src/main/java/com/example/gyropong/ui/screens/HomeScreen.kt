package com.example.gyropong.ui.screens

import android.graphics.fonts.FontFamily
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.gyropong.ui.components.GameButton


val gameFont = androidx.compose.ui.text.font.FontFamily.Default // temporal, puedes reemplazar por tu font descargada
//val gameFontFamily = FontFamily(Font(R.font.press_start_2p))

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onQuickMatchClick: () -> Unit,
    onSessionClick: () -> Unit
) {
    // Fondo degradado
    val gradientBackground = Brush.verticalGradient(
        colors = listOf(Color(0xFF3F51B5), Color(0xFF673AB7)) // morado oscuro → morado claro
    )

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
            text = "GyroPong",
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
            onClick = onSessionClick,
            enabled = true
        )
    }
}

/*
@Composable
fun GameButton(
    text: String,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope() // <-- CoroutineScope para eventos

    val scale by animateFloatAsState(if (pressed) 0.95f else 1f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(8.dp, RoundedCornerShape(24.dp))
            .background(
                color = Color(0xFFFFD700),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // Animación de pulsado con coroutine
                pressed = true
                scope.launch {
                    onClick()
                    delay(100) // breve retraso para efecto de pulsado
                    pressed = false
                }
            }
            .border(BorderStroke(3.dp, Color.Black), RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black,
                fontFamily = gameFont,
                letterSpacing = 1.sp
            )
        )
    }
}
*/
/*
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
*/
@Composable
@Preview(showSystemUi = true)
fun HomeScreenPreview() {
    HomeScreen(
        onQuickMatchClick = {},
        onSessionClick = {}
    )
}