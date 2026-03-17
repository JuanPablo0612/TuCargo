// RUTA: composeApp/src/commonMain/kotlin/com/juanpablo0612/tucargo/features/auth/presentation/documents/DocumentViewModel.kt

package com.juanpablo0612.tucargo.features.auth.presentation.documents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanpablo0612.tucargo.data.user.UserRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DocumentViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DocumentState())
    val uiState = _uiState.asStateFlow()

    fun onAction(action: DocumentAction) {
        when (action) {
            is DocumentAction.OnFrontPhotoSelected ->
                _uiState.update { it.copy(idFrontPath = action.path, errorMessage = null) }
            is DocumentAction.OnBackPhotoSelected ->
                _uiState.update { it.copy(idBackPath = action.path, errorMessage = null) }
            DocumentAction.OnSubmit -> performUpload()
            DocumentAction.OnBackClick -> { }
        }
    }

    private fun performUpload() {
        val state = _uiState.value
        if (state.idFrontPath == null || state.idBackPath == null) {
            _uiState.update { it.copy(errorMessage = "Por favor sube ambas caras del documento") }
            return
        }
        val userId = userRepository.getCurrentUserId() ?: run {
            _uiState.update { it.copy(errorMessage = "Usuario no autenticado") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // TODO: subir a Firebase Storage con userId
                delay(1500)
                _uiState.update { it.copy(isLoading = false, isUploadSuccess = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "Error al subir documentos")
                }
            }
        }
    }
}
