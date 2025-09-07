/*Los archivos *UseCase.kt son los responsables de llevar a cabo las acciones de la aplicación.
es decir, no utilizaremos lógica en otro lado, todo se hará con funciones concretas.
*/

package com.example.gyropong.domain.usecases

import android.util.Log
import com.example.gyropong.data.repository.UserRepository
import com.example.gyropong.data.repository.SessionRepository
import com.example.gyropong.data.database.entities.UserEntity


class IncrementPointsUseCase(
    private val userRepository: UserRepository
) {
    companion object { private const val TAG = "IncrementPointsUC" }

    suspend operator fun invoke(userId: Long, points: Int) {
        try {
            Log.d(TAG, "Sumando $points puntos al usuario $userId")
            userRepository.addPoints(userId, points)
        } catch (e: Exception) {
            Log.e(TAG, "Error al sumar puntos: ${e.message}", e)
        }
    }
}