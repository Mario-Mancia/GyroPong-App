// Pequeño componente reutilizable para hacer botones consistentes en toda la app.
package com.example.gyropong.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gyropong.ui.screens.gameFont
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun GameButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    var pressed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val scale by animateFloatAsState(if (pressed) 0.95f else 1f)
    val backgroundColor = if (enabled) Color(0xFFFFD700) else Color(0x99FFD700)
    val textColor = if (enabled) Color.Black else Color.Gray

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
                color = backgroundColor,
                shape = RoundedCornerShape(24.dp)
            )
            .then(
                if (enabled) Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    pressed = true
                    scope.launch {
                        onClick()
                        delay(100)
                        pressed = false
                    }
                } else Modifier
            )
            .border(BorderStroke(3.dp, Color.Black), RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor,
                fontFamily = gameFont,
                letterSpacing = 1.sp
            )
        )
    }
}