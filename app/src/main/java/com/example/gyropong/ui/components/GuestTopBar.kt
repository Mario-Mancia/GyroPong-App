// Componente reutilizable para introducir una topbar si el usuario no ha iniciado sesión.

package com.example.gyropong.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.gyropong.ui.viewmodels.BluetoothViewModel

@Composable
fun GuestTopBar(
    nickname: String,
    avatarRes: Int,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(Color(0xFF621BC7), RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .shadow(4.dp, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Avatar
            Image(
                painter = painterResource(id = avatarRes),
                contentDescription = "Avatar",
                modifier = Modifier.size(56.dp)
            )

            // Nickname centrado
            Text(
                text = nickname,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            // Botón Cancelar
            Button(
                onClick = onLogoutClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .height(40.dp)
                    .width(100.dp)
            ) {
                Text("Salir", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
