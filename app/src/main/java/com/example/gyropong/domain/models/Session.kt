package com.example.gyropong.domain.models

data class Session(
    val id: Long,
    val userId: Long,
    val isLoggedIn: Boolean,
    val createdAt: Long
)