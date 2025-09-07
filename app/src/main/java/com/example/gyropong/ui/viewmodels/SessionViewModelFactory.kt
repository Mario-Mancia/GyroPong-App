package com.example.gyropong.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gyropong.data.repository.SessionRepository

class SessionViewModelFactory(private val sessionRepository: SessionRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SessionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SessionViewModel(sessionRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}