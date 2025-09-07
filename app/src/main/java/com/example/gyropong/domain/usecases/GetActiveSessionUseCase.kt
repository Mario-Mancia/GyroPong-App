/*Los archivos *UseCase.kt son los responsables de llevar a cabo las acciones de la aplicación.
es decir, no utilizaremos lógica en otro lado, todo se hará con funciones concretas.
*/

package com.example.gyropong.domain.usecases

import android.util.Log
import com.example.gyropong.data.repository.SessionRepository
import com.example.gyropong.domain.models.Session

class GetActiveSessionUseCase(
    private val sessionRepository: SessionRepository
) {
    companion object { private const val TAG = "GetActiveSessionUC" }

    suspend operator fun invoke(): Session? {
        return try {
            val session = sessionRepository.getActiveSession()
            Log.d(TAG, "Sesión activa obtenida: ${session?.id}")
            session
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener sesión activa: ${e.message}", e)
            null
        }
    }
}
