//Clase diseñada para definir la estructura de la tabla de usuarios.

package com.example.gyropong.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val username: String,
    val email: String,
    val password: String,
    val birthdate: Long,
    val createdAt: Long,
    val userPoints: Int = 0
)