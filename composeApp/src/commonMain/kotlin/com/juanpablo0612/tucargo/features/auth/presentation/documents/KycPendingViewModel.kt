package com.juanpablo0612.tucargo.features.auth.presentation.documents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanpablo0612.tucargo.core.logging.logError
import com.juanpablo0612.tucargo.domain.usecase.GetCurrentUserIdUseCase
import com.juanpablo0612.tucargo.domain.usecase.ObserveCurrentUserUseCase
import com.juanpablo0612.tucargo.domain.usecase.ObserveKycDocumentsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

class KycPendingViewModel(
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val observeKycDocumentsUseCase: ObserveKycDocumentsUseCase
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
        observeKycDocumentsUseCase(userId)
            .onEach { docs ->
                _uiState.update { it.copy(isLoading = false, documents = docs) }
            }
            .catch { e ->
                logError("KycPendingViewModel", "Failed to observe documents: ${e.message}")
                _uiState.update { it.copy(isLoading = false) }
            }
            .launchIn(viewModelScope)
    }
}
