/*Los mappers funcionarán como transformadores entre modelos y entidades para que los viewmodels no
toquen directamente a las entidades.
*/

package com.example.gyropong.domain.mappers

import com.example.gyropong.data.database.entities.SessionEntity
import com.example.gyropong.domain.models.Session

fun SessionEntity.toDomain(): Session {
    return Session(
        id = id,
        userId = userId,
        isLoggedIn = isLoggedIn,
        createdAt = lastLogin // lastLogin → createdAt
    )
}

fun Session.toEntity(): SessionEntity {
    return SessionEntity(
        id = id,
        userId = userId,
        isLoggedIn = isLoggedIn,
        lastLogin = createdAt // createdAt → lastLogin
    )
}