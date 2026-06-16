package com.juanpablo0612.tucargo.features.auth.driverdocs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanpablo0612.tucargo.domain.model.AppError
import com.juanpablo0612.tucargo.domain.usecase.GetCurrentUserIdUseCase
import com.juanpablo0612.tucargo.domain.usecase.UploadKycDocumentUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DriverDocsUploadViewModel(
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val uploadKycDocumentUseCase: UploadKycDocumentUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DriverDocsUploadState())
    val uiState = _uiState.asStateFlow()

    fun onAction(action: DriverDocsAction) {
        when (action) {
            is DriverDocsAction.OnDocumentSelected -> {
                _uiState.update { state ->
                    state.copy(
                        documents = state.documents + (action.type to action.file),
                        documentErrors = state.documentErrors - action.type
                    )
                }
            }
            DriverDocsAction.OnSubmit -> performUpload()
            DriverDocsAction.OnBackClick -> {}
        }
    }

    fun onNavigated() {
        _uiState.update { it.copy(isUploadComplete = false) }
    }

    private fun performUpload() {
        val state = _uiState.value
        val missing = state.documents.filterValues { it == null }.keys
        if (missing.isNotEmpty()) {
            _uiState.update { it.copy(documentErrors = missing.associateWith { true }) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, uploadError = null) }

            val userId = getCurrentUserIdUseCase()
            if (userId == null) {
                _uiState.update { it.copy(isLoading = false, uploadError = DriverDocsError.UserNotAuthenticated) }
                return@launch
            }

            val results = coroutineScope {
                state.documents.mapNotNull { (type, file) ->
                    file?.let { async { uploadKycDocumentUseCase(userId, type, it) } }
                }.awaitAll()
            }

            val failures = results.mapNotNull { it.exceptionOrNull() }
            val error = when {
                failures.isEmpty() -> null
                failures.any { it is AppError.Validation.FileTooLarge } -> DriverDocsError.FileTooLarge
                else -> DriverDocsError.SomeDocsFailed
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isUploadComplete = error == null,
                    uploadError = error
                )
            }
        }
    }
}
