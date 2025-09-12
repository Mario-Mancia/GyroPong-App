// Modelos de tablas de DB en Kotlin puro.
package com.example.gyropong.domain.models

data class User(
    val id: Long,
    val username: String,
    val email: String,
    val birthdate: Long,
    val createdAt: Long,
    val userPoints: Int
)