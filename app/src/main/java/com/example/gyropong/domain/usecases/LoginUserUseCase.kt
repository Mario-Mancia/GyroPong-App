/*Los archivos *UseCase.kt son los responsables de llevar a cabo las acciones de la aplicación.
es decir, no utilizaremos lógica en otro lado, todo se hará con funciones concretas.
*/

package com.example.gyropong.domain.usecases

import android.util.Log
import com.example.gyropong.data.repository.UserRepository
import com.example.gyropong.data.repository.SessionRepository
import com.example.gyropong.domain.models.User

class LoginUserUseCase(
    private val userRepository: UserRepository,
    private val sessionRepository: SessionRepository
) {
    companion object { private const val TAG = "LoginUserUC" }

    suspend operator fun invoke(email: String, password: String): User? {
        return try {
            Log.d(TAG, "Intentando login para email: $email")
            val user = userRepository.login(email, password)
            if (user != null) {
                sessionRepository.startSession(user.id)
                Log.d(TAG, "Login exitoso: ${user.username}")
            } else {
                Log.d(TAG, "Login fallido para email: $email")
            }
            user
        } catch (e: Exception) {
            Log.e(TAG, "Error en login: ${e.message}", e)
            null
        }
    }
}