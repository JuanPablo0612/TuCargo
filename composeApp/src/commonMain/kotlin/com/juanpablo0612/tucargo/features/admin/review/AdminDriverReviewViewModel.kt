package com.juanpablo0612.tucargo.features.admin.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanpablo0612.tucargo.core.logging.logError
import com.juanpablo0612.tucargo.domain.model.KycDocumentType
import com.juanpablo0612.tucargo.domain.usecase.document.ObserveKycDocumentsUseCase
import com.juanpablo0612.tucargo.domain.usecase.admin.ReviewKycDocumentUseCase
import com.juanpablo0612.tucargo.domain.usecase.admin.SetDriverVerifiedUseCase
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AdminDriverReviewViewModel(
    private val driverId: String,
    private val observeKycDocumentsUseCase: ObserveKycDocumentsUseCase,
    private val reviewKycDocumentUseCase: ReviewKycDocumentUseCase,
    private val setDriverVerifiedUseCase: SetDriverVerifiedUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminDriverReviewState())
    val uiState = _uiState.asStateFlow()

    private var documentsJob: Job? = null

    init {
        loadDocuments()
    }

    fun onAction(action: AdminDriverReviewAction) {
        when (action) {
            is AdminDriverReviewAction.Approve -> review(action.type, approve = true, reason = null)
            is AdminDriverReviewAction.Reject -> review(action.type, approve = false, reason = action.reason)
            AdminDriverReviewAction.VerifyDriver -> verifyDriver()
            AdminDriverReviewAction.Refresh -> {
                _uiState.update { it.copy(isLoading = true, error = null, documents = persistentListOf()) }
                loadDocuments()
            }
        }
    }

    private fun loadDocuments() {
        documentsJob?.cancel()
        documentsJob = observeKycDocumentsUseCase(driverId)
            .onEach { docs ->
                _uiState.update { it.copy(isLoading = false, documents = docs.toImmutableList()) }
            }
            .catch { e ->
                logError("AdminDriverReviewVM", "Failed to observe documents of $driverId: ${e.message}")
                _uiState.update { it.copy(isLoading = false, error = AdminDriverReviewError.LoadError) }
            }
            .launchIn(viewModelScope)
    }

    fun onVerifiedNavigated() {
        _uiState.update { it.copy(isDriverVerified = false) }
    }

    private fun review(type: KycDocumentType, approve: Boolean, reason: String?) {
        if (_uiState.value.processingType != null) return
        viewModelScope.launch {
            _uiState.update { it.copy(processingType = type, error = null) }
            reviewKycDocumentUseCase(
                userId = driverId,
                type = type,
                approve = approve,
                rejectionReason = reason?.trim()?.takeIf { it.isNotEmpty() }
            ).fold(
                onSuccess = { _uiState.update { it.copy(processingType = null) } },
                onFailure = {
                    _uiState.update {
                        it.copy(processingType = null, error = AdminDriverReviewError.ReviewError)
                    }
                }
            )
        }
    }

    private fun verifyDriver() {
        if (_uiState.value.isVerifying || !_uiState.value.allDocumentsApproved) return
        viewModelScope.launch {
            _uiState.update { it.copy(isVerifying = true, error = null) }
            setDriverVerifiedUseCase(driverId).fold(
                onSuccess = {
                    _uiState.update { it.copy(isVerifying = false, isDriverVerified = true) }
                },
                onFailure = {
                    _uiState.update {
                        it.copy(isVerifying = false, error = AdminDriverReviewError.VerifyError)
                    }
                }
            )
        }
    }
}
