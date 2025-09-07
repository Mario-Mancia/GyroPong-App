package com.example.gyropong.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gyropong.domain.models.Session
import com.example.gyropong.data.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SessionViewModel(private val sessionRepository: SessionRepository) : ViewModel() {

    companion object {
        private const val TAG = "SessionVM"
    }

    private val _activeSession = MutableStateFlow<Session?>(null)
    val activeSession: StateFlow<Session?> get() = _activeSession

    private val _allSessions = MutableStateFlow<List<Session>>(emptyList())
    val allSessions: StateFlow<List<Session>> get() = _allSessions

    // Iniciar sesión
    fun startSession(userId: Long) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Iniciando sesión para usuarioId: $userId")
                val session = sessionRepository.startSession(userId)
                _activeSession.value = session
                Log.d(TAG, "Sesión iniciada correctamente, sessionId: ${session.id}")
            } catch (e: Exception) {
                Log.e(TAG, "Error al iniciar sesión: ${e.message}", e)
            }
        }
    }

    // Obtener sesión activa
    fun loadActiveSession() {
        viewModelScope.launch {
            try {
                _activeSession.value = sessionRepository.getActiveSession()
                Log.d(TAG, "Sesión activa cargada: ${_activeSession.value?.id}")
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar sesión activa: ${e.message}", e)
            }
        }
    }

    // Cerrar sesión
    fun logout(userId: Long) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Cerrando sesión para usuarioId: $userId")
                sessionRepository.logoutUser(userId)
                _activeSession.value = null
            } catch (e: Exception) {
                Log.e(TAG, "Error al cerrar sesión: ${e.message}", e)
            }
        }
    }

    // Obtener todas las sesiones
    fun loadAllSessions() {
        viewModelScope.launch {
            try {
                _allSessions.value = sessionRepository.getAllSessions()
                Log.d(TAG, "Sesiones cargadas: ${_allSessions.value.size}")
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar todas las sesiones: ${e.message}", e)
            }
        }
    }

    // Eliminar sesión
    fun deleteSession(session: Session) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Eliminando sesión id: ${session.id}")
                sessionRepository.deleteSession(session)
                loadAllSessions()
            } catch (e: Exception) {
                Log.e(TAG, "Error al eliminar sesión id ${session.id}: ${e.message}", e)
            }
        }
    }
}