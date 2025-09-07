/*Los archivos *UseCase.kt son los responsables de llevar a cabo las acciones de la aplicación.
es decir, no utilizaremos lógica en otro lado, todo se hará con funciones concretas.
*/

package com.example.gyropong.domain.usecases

import android.util.Log
import com.example.gyropong.data.repository.SessionRepository

class LogoutUserUseCase(
    private val sessionRepository: SessionRepository
) {
    companion object { private const val TAG = "LogoutUserUC" }

    suspend operator fun invoke() {
        try {
            Log.d(TAG, "Cerrando todas las sesiones")
            sessionRepository.logoutAll()
        } catch (e: Exception) {
            Log.e(TAG, "Error al cerrar sesión: ${e.message}", e)
        }
    }
}