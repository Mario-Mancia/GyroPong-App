// Componente reutilizable para implementar la topbar del usuario con sesión activa.
package com.example.gyropong.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SessionTopBar(
    username: String,
    avatar: Int,
    points: Int,
    avatars: List<Int>,
    onAvatarSelected: (Int) -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

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
            Box {
                Image(
                    painter = painterResource(id = avatar),
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .clickable { expanded = true }
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    offset = DpOffset(x = 0.dp, y = 8.dp)
                ) {
                    avatars.forEach { avatarRes ->
                        DropdownMenuItem(
                            text = {
                                Image(
                                    painter = painterResource(id = avatarRes),
                                    contentDescription = "Avatar opción",
                                    modifier = Modifier.size(36.dp)
                                )
                            },
                            onClick = {
                                onAvatarSelected(avatarRes)
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Nombre y puntos
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    username,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp
                )
                Text(
                    "Puntos: $points",
                    color = Color.Yellow,
                    fontSize = 14.sp
                )
            }

            Button(
                onClick = onProfileClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .height(40.dp)
                    .width(100.dp)
            ) {
                Text("Perfil", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

