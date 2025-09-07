//Las clases repository son puentes entre la lógica de negocios y los DAO para manipular la DB.

package com.example.gyropong.data.repository

import com.example.gyropong.data.database.dao.UserDao
import com.example.gyropong.domain.models.User
import com.example.gyropong.domain.mappers.toEntity
import com.example.gyropong.domain.mappers.toDomain

class UserRepository(private val userDao: UserDao) {

    suspend fun insertUser(user: User, password: String): Long {
        return userDao.insertUser(user.toEntity(password))
    }

    suspend fun getUserById(id: Long): User? {
        return userDao.getUserById(id)?.toDomain()
    }

    suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)?.toDomain()
    }

    suspend fun getAllUsers(): List<User> {
        return userDao.getAllUsers().map { it.toDomain() }
    }

    suspend fun updateUser(user: User, password: String? = null) {
        val entity = if (password != null) user.toEntity(password) else user.toEntity("")
        userDao.updateUser(entity)
    }

    suspend fun addPoints(userId: Long, points: Int) {
        userDao.addPointsToUser(userId, points)
    }

    suspend fun deleteUser(user: User) {
        userDao.deleteUser(user.toEntity(""))
    }

    suspend fun deleteAllUsers() {
        userDao.deleteAllUsers()
    }

    suspend fun login(email: String, password: String): User? {
        return userDao.login(email, password)?.toDomain()
    }
}