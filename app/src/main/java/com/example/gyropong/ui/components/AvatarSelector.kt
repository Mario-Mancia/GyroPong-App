package com.example.gyropong.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun AvatarSelector(
    avatars: List<Int>,
    selectedAvatar: Int,
    onAvatarSelected: (Int) -> Unit
) {
    LazyRow {
        items(avatars) { avatar ->
            Card(
                modifier = Modifier
                    .size(72.dp) // tamaño consistente para los avatares
                    .padding(8.dp)
                    .clickable { onAvatarSelected(avatar) },
                shape = RoundedCornerShape(16.dp),
                border = if (avatar == selectedAvatar) BorderStroke(2.dp, Color(0xFFFFD700)) else null,
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Image(
                    painter = painterResource(id = avatar),
                    contentDescription = null,
                    modifier = Modifier
                        .size(72.dp)
                )
            }
        }
    }
}