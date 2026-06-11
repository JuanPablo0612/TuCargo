package com.juanpablo0612.tucargo.features.auth.presentation.driverdocs

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanpablo0612.tucargo.core.coroutines.AppDispatchers
import com.juanpablo0612.tucargo.data.document.DocumentRepository
import com.juanpablo0612.tucargo.domain.model.KycDocumentType
import com.juanpablo0612.tucargo.domain.usecase.GetCurrentUserIdUseCase
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Immutable
data class DriverDocsUploadState(
    val isLoading: Boolean = false,
    val documents: Map<KycDocumentType, PlatformFile?> = KycDocumentType.entries.associateWith { null },
    val documentErrors: Map<KycDocumentType, Boolean> = emptyMap(),
    val uploadError: DriverDocsError? = null,
    val isUploadComplete: Boolean = false
)

sealed interface DriverDocsAction {
    data class OnDocumentSelected(val type: KycDocumentType, val file: PlatformFile) : DriverDocsAction
    data object OnSubmit : DriverDocsAction
    data object OnBackClick : DriverDocsAction
}

sealed interface DriverDocsError {
    data object UserNotAuthenticated : DriverDocsError
    data object UploadError : DriverDocsError
    data object SomeDocsFailed : DriverDocsError
}

class DriverDocsUploadViewModel(
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val documentRepository: DocumentRepository,
    private val dispatchers: AppDispatchers
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
        val missing = KycDocumentType.entries.filter { state.documents[it] == null }
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

            val results = withContext(dispatchers.io) {
                coroutineScope {
                    state.documents.map { (type, file) ->
                        async {
                            val bytes = file!!.readBytes()
                            documentRepository.uploadDocument(userId, type, bytes)
                        }
                    }.awaitAll()
                }
            }

            val failures = results.filter { it.isFailure }
            if (failures.isEmpty()) {
                _uiState.update { it.copy(isLoading = false, isUploadComplete = true) }
            } else {
                _uiState.update { it.copy(isLoading = false, uploadError = DriverDocsError.SomeDocsFailed) }
            }
        }
    }
}
