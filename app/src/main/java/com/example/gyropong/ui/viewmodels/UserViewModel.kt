package com.example.gyropong.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gyropong.domain.models.User
import com.example.gyropong.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

open class UserViewModel(private val userRepository: UserRepository) : ViewModel() {

    companion object {
        private const val TAG = "UserVM"
    }

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> get() = _currentUser

    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    val allUsers: StateFlow<List<User>> get() = _allUsers

    // Crear nuevo usuario
    fun registerUser(user: User, password: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Registrando usuario: ${user.username}")
                userRepository.insertUser(user, password)
                loadAllUsers()
                Log.d(TAG, "Usuario registrado correctamente: ${user.username}")
            } catch (e: Exception) {
                Log.e(TAG, "Error al registrar usuario: ${e.message}", e)
            }
        }
    }

    // Login
    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Intentando login para email: $email")
                val user = userRepository.login(email, password)
                _currentUser.value = user
                if (user != null) {
                    Log.d(TAG, "Login exitoso: ${user.username}")
                } else {
                    Log.d(TAG, "Login fallido: usuario no encontrado o contraseña incorrecta")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error en login: ${e.message}", e)
            }
        }
    }

    // Logout
    fun logout() {
        Log.d(TAG, "Logout del usuario actual")
        _currentUser.value = null
    }

    // Sumar puntos
    fun addPoints(userId: Long, points: Int) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Sumando $points puntos al usuario con id $userId")
                userRepository.addPoints(userId, points)
                loadUserById(userId)
            } catch (e: Exception) {
                Log.e(TAG, "Error al sumar puntos: ${e.message}", e)
            }
        }
    }

    // Cargar todos los usuarios
    fun loadAllUsers() {
        viewModelScope.launch {
            try {
                _allUsers.value = userRepository.getAllUsers()
                Log.d(TAG, "Usuarios cargados: ${_allUsers.value.size}")
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar todos los usuarios: ${e.message}", e)
            }
        }
    }

    fun isEmailRegistered(email: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val user = userRepository.getUserByEmail(email)
            callback(user != null)
        }
    }

    // Cargar un usuario por Id
    fun loadUserById(userId: Long) {
        viewModelScope.launch {
            try {
                _currentUser.value = userRepository.getUserById(userId)
                Log.d(TAG, "Usuario cargado por id $userId: ${_currentUser.value?.username}")
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar usuario por id $userId: ${e.message}", e)
            }
        }
    }
}