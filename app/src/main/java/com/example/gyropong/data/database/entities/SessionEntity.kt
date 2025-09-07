//Clase para definir estructura de tabla de sesiones.
package com.example.gyropong.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val userId: Long,
    val isLoggedIn: Boolean = true,
    val lastLogin: Long = System.currentTimeMillis()
)