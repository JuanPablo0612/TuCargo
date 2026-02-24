package com.juanpablo0612.tucargo.features.auth.presentation.documents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DocumentViewModel : ViewModel() {

    // Cambiamos el nombre a _uiState para que coincida con tu Login
    private val _uiState = MutableStateFlow(DocumentState())
    val uiState = _uiState.asStateFlow() // Esto arregla el error de la línea 30

    fun onAction(action: DocumentAction) {
        when (action) {
            is DocumentAction.OnFrontPhotoSelected -> {
                _uiState.update { it.copy(idFrontPath = action.path, errorMessage = null) }
            }
            is DocumentAction.OnBackPhotoSelected -> {
                _uiState.update { it.copy(idBackPath = action.path, errorMessage = null) }
            }
            DocumentAction.OnSubmit -> performUpload()
            DocumentAction.OnBackClick -> { /* Navegación */ }
        }
    }

    private fun performUpload() {
        val state = _uiState.value
        if (state.idFrontPath == null || state.idBackPath == null) {
            _uiState.update { it.copy(errorMessage = "Por favor sube ambas caras del documento") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            delay(2000)
            _uiState.update { it.copy(isLoading = false, isUploadSuccess = true) }
        }
    }
}