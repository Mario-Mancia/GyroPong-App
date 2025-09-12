// Consultas SQL para la tabla de users
package com.example.gyropong.data.database.dao

import androidx.room.*
import com.example.gyropong.data.database.entities.UserEntity

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Long): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<UserEntity>

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET userPoints = userPoints + :points WHERE id = :id")
    suspend fun addPointsToUser(id: Long, points: Int)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()

    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    suspend fun login(email: String, password: String): UserEntity?
}


/*@Dao
interface UserDao {

    //Crear nuevo usuario.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    //Recuperar usuarios por Id.
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Int): UserEntity?

    //Recuperar usuarios por su email.
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    //Recuperar todos los usuarios.
    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<UserEntity>

    //Actualizar información de usuario,
    @Update
    suspend fun updateUser(user: UserEntity)

    //Incrementar los puntos del usuario.
    @Query("UPDATE users SET userPoints = userPoints + :points WHERE id = :id")
    suspend fun addPointsToUser(id: Int, points: Int)

    //Eliminar usuario.
    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()

    //Utilidad extra: verificar login
    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    suspend fun login(email: String, password: String): UserEntity?
}*/