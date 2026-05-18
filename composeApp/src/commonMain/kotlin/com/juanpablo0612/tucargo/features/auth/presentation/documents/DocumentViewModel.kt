package com.juanpablo0612.tucargo.features.auth.presentation.documents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanpablo0612.tucargo.core.ui.event.UiEvent
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
                _uiState.update { it.copy(idFront = action.file, error = null) }
            is DocumentAction.OnBackPhotoSelected ->
                _uiState.update { it.copy(idBack = action.file, error = null) }
            DocumentAction.OnSubmit -> performUpload()
            DocumentAction.OnBackClick -> {}
        }
    }

    private fun performUpload() {
        val state = _uiState.value
        val frontFile = state.idFront
        val backFile = state.idBack

        if (frontFile == null || backFile == null) {
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

            val frontBytes = frontFile.readBytes()
            val backBytes = backFile.readBytes()

            uploadDocumentsUseCase(userId, frontBytes, backBytes).fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, navigationEvent = UiEvent(Unit)) }
                },
                onFailure = {
                    _uiState.update { it.copy(isLoading = false, error = DocumentError.UploadError) }
                }
            )
        }
    }
}
