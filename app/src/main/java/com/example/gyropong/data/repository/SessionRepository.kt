//Las clases repository son puentes entre la lógica de negocios y los DAO para manipular la DB.

package com.example.gyropong.data.repository

import com.example.gyropong.data.database.dao.SessionDao
import com.example.gyropong.data.database.entities.SessionEntity
import com.example.gyropong.domain.models.Session
import com.example.gyropong.domain.mappers.toDomain
import com.example.gyropong.domain.mappers.toEntity

class SessionRepository(private val sessionDao: SessionDao) {

    suspend fun startSession(userId: Long): Session {
        sessionDao.logoutAll()
        val entity = SessionEntity(userId = userId, isLoggedIn = true)
        val id = sessionDao.insertSession(entity)
        return entity.copy(id = id).toDomain()
    }

    suspend fun getSessionById(id: Long): Session? {
        return sessionDao.getSessionById(id)?.toDomain()
    }

    suspend fun getSessionByUser(userId: Long): Session? {
        return sessionDao.getSessionByUser(userId)?.toDomain()
    }

    suspend fun getAllSessions(): List<Session> {
        return sessionDao.getAllSessions().map { it.toDomain() }
    }

    suspend fun logoutUser(userId: Long) {
        sessionDao.updateLoginStatus(userId, false)
    }

    suspend fun logoutAll() {
        sessionDao.logoutAll()
    }

    suspend fun deleteSession(session: Session) {
        sessionDao.deleteSession(session.toEntity())
    }

    suspend fun deleteAllSessions() {
        sessionDao.deleteAllSessions()
    }

    suspend fun getActiveSession(): Session? {
        return sessionDao.getActiveSession()?.toDomain()
    }
}