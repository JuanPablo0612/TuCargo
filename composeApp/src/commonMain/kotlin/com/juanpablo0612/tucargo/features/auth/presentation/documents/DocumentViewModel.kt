package com.juanpablo0612.tucargo.features.auth.presentation.documents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanpablo0612.tucargo.core.validation.FieldError
import com.juanpablo0612.tucargo.domain.usecase.GetCurrentUserIdUseCase
import com.juanpablo0612.tucargo.domain.usecase.UploadDocumentsUseCase
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DocumentViewModel(
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val uploadDocumentsUseCase: UploadDocumentsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DocumentState())
    val uiState = _uiState.asStateFlow()

    fun onAction(action: DocumentAction) {
        when (action) {
            is DocumentAction.OnFrontPhotoSelected ->
                _uiState.update { it.copy(idFront = action.file, frontFileError = null) }
            is DocumentAction.OnBackPhotoSelected ->
                _uiState.update { it.copy(idBack = action.file, backFileError = null) }
            DocumentAction.OnSubmit -> performUpload()
            DocumentAction.OnBackClick -> {}
        }
    }

    fun onNavigated() {
        _uiState.update { it.copy(isUploadComplete = false) }
    }

    private fun performUpload() {
        val state = _uiState.value
        val frontError: FieldError? = if (state.idFront == null) FieldError.IdFrontRequired else null
        val backError: FieldError? = if (state.idBack == null) FieldError.IdBackRequired else null

        if (frontError != null || backError != null) {
            _uiState.update { it.copy(frontFileError = frontError, backFileError = backError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, authError = null) }

            val userId = getCurrentUserIdUseCase()
            if (userId == null) {
                _uiState.update { it.copy(isLoading = false, authError = DocumentError.UserNotAuthenticated) }
                return@launch
            }

            uploadDocumentsUseCase(userId, state.idFront!!, state.idBack!!).fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, isUploadComplete = true) }
                },
                onFailure = {
                    _uiState.update { it.copy(isLoading = false, authError = DocumentError.UploadError) }
                }
            )
        }
    }
}
