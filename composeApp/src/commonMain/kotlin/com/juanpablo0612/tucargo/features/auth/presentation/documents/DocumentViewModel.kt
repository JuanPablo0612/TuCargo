package com.juanpablo0612.tucargo.features.auth.presentation.documents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanpablo0612.tucargo.core.ui.event.UiEvent
import com.juanpablo0612.tucargo.domain.usecase.GetCurrentUserIdUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DocumentViewModel(
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DocumentState())
    val uiState = _uiState.asStateFlow()

    fun onAction(action: DocumentAction) {
        when (action) {
            is DocumentAction.OnFrontPhotoSelected ->
                _uiState.update { it.copy(idFrontPath = action.path, error = null) }
            is DocumentAction.OnBackPhotoSelected ->
                _uiState.update { it.copy(idBackPath = action.path, error = null) }
            DocumentAction.OnSubmit -> performUpload()
            DocumentAction.OnBackClick -> { }
        }
    }

    private fun performUpload() {
        val state = _uiState.value
        if (state.idFrontPath == null || state.idBackPath == null) {
            _uiState.update { it.copy(error = DocumentError.BothSidesRequired) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val userId = getCurrentUserIdUseCase()
            if (userId == null) {
                _uiState.update { it.copy(isLoading = false, error = DocumentError.UserNotAuthenticated) }
                return@launch
            }
            try {
                // userId will be used for the Firebase Storage path: "docs/$userId/..."
                delay(1500)
                _uiState.update { it.copy(isLoading = false, navigationEvent = UiEvent(Unit)) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = DocumentError.UploadError) }
            }
        }
    }
}
