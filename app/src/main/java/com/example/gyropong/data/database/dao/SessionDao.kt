package com.example.gyropong.data.database.dao

import androidx.room.*
import com.example.gyropong.data.database.entities.SessionEntity

@Dao
interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity): Long

    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): SessionEntity?

    @Query("SELECT * FROM sessions WHERE userId = :userId LIMIT 1")
    suspend fun getSessionByUser(userId: Long): SessionEntity?

    @Query("SELECT * FROM sessions")
    suspend fun getAllSessions(): List<SessionEntity>

    @Update
    suspend fun updateSession(session: SessionEntity)

    @Query("UPDATE sessions SET isLoggedIn = :isLoggedIn WHERE userId = :userId")
    suspend fun updateLoginStatus(userId: Long, isLoggedIn: Boolean)

    @Delete
    suspend fun deleteSession(session: SessionEntity)

    @Query("DELETE FROM sessions")
    suspend fun deleteAllSessions()

    @Query("SELECT * FROM sessions WHERE isLoggedIn = 1 LIMIT 1")
    suspend fun getActiveSession(): SessionEntity?

    @Query("UPDATE sessions SET isLoggedIn = 0")
    suspend fun logoutAll()
}

/*@Dao
interface SessionDao {

    //Create
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity): Long

    //Read
    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun getSessionById(id: Int): SessionEntity?

    @Query("SELECT * FROM sessions WHERE userId = :userId LIMIT 1")
    suspend fun getSessionByUser(userId: Int): SessionEntity?

    @Query("SELECT * FROM sessions")
    suspend fun getAllSessions(): List<SessionEntity>

    //Update
    @Update
    suspend fun updateSession(session: SessionEntity)

    @Query("UPDATE sessions SET isLoggedIn = :isLoggedIn WHERE userId = :userId")
    suspend fun updateLoginStatus(userId: Int, isLoggedIn: Boolean)

    //Delete
    @Delete
    suspend fun deleteSession(session: SessionEntity)

    @Query("DELETE FROM sessions")
    suspend fun deleteAllSessions()

    //Extra útiles
    @Query("SELECT * FROM sessions WHERE isLoggedIn = 1 LIMIT 1")
    suspend fun getActiveSession(): SessionEntity?

    @Query("UPDATE sessions SET isLoggedIn = 0")
    suspend fun logoutAll()
}*/