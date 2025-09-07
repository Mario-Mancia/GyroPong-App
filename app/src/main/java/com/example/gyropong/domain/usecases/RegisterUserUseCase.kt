/*Los archivos *UseCase.kt son los responsables de llevar a cabo las acciones de la aplicación.
es decir, no utilizaremos lógica en otro lado, todo se hará con funciones concretas.
*/

package com.example.gyropong.domain.usecases

import android.util.Log
import com.example.gyropong.data.repository.UserRepository
import com.example.gyropong.domain.models.User

class RegisterUserUseCase(
    private val userRepository: UserRepository
) {
    companion object { private const val TAG = "RegisterUserUC" }

    suspend operator fun invoke(user: User, password: String): Long {
        return try {
            Log.d(TAG, "Registrando usuario: ${user.username}")
            val id = userRepository.insertUser(user, password)
            Log.d(TAG, "Usuario registrado con id: $id")
            id
        } catch (e: Exception) {
            Log.e(TAG, "Error al registrar usuario: ${e.message}", e)
            -1L
        }
    }
}