package com.juanpablo0612.tucargo.features.auth.presentation.documents

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanpablo0612.tucargo.data.document.DocumentRepository
import com.juanpablo0612.tucargo.domain.model.KycDocument
import com.juanpablo0612.tucargo.domain.usecase.GetCurrentUserIdUseCase
import com.juanpablo0612.tucargo.domain.usecase.ObserveCurrentUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

@Immutable
data class KycPendingState(
    val isLoading: Boolean = true,
    val documents: List<KycDocument> = emptyList(),
    val isVerified: Boolean = false
)

class KycPendingViewModel(
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val documentRepository: DocumentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(KycPendingState())
    val uiState = _uiState.asStateFlow()

    init {
        observeVerification()
        observeDocuments()
    }

    private fun observeVerification() {
        observeCurrentUserUseCase()
            .filterNotNull()
            .onEach { user ->
                _uiState.update { it.copy(isVerified = user.isVerified) }
            }
            .launchIn(viewModelScope)
    }

    private fun observeDocuments() {
        val userId = getCurrentUserIdUseCase() ?: return
        documentRepository.observeDocumentsForUser(userId)
            .onEach { docs ->
                _uiState.update { it.copy(isLoading = false, documents = docs) }
            }
            .launchIn(viewModelScope)
    }
}
