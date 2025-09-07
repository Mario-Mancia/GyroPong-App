/*Los mappers funcionarán como transformadores entre modelos y entidades para que los viewmodels no
toquen directamente a las entidades.
*/
package com.example.gyropong.domain.mappers

import com.example.gyropong.data.database.entities.UserEntity
import com.example.gyropong.domain.models.User

fun UserEntity.toDomain(): User {
    return User(
        id = id,
        username = username,
        email = email,
        birthdate = birthdate,
        createdAt = createdAt,
        userPoints = userPoints
    )
}

fun User.toEntity(password: String): UserEntity {
    return UserEntity(
        id = id,
        username = username,
        email = email,
        password = password, // Solo aquí lo pedimos explícitamente
        birthdate = birthdate,
        createdAt = createdAt,
        userPoints = userPoints
    )
}