package com.juanpablo0612.tucargo.features.trip.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanpablo0612.tucargo.core.logging.logError
import com.juanpablo0612.tucargo.domain.usecase.CancelTripUseCase
import com.juanpablo0612.tucargo.domain.usecase.GetCurrentUserIdUseCase
import com.juanpablo0612.tucargo.domain.usecase.ObserveTripUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TripDetailViewModel(
    private val tripId: String,
    observeTripUseCase: ObserveTripUseCase,
    getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val cancelTripUseCase: CancelTripUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TripDetailState())
    val uiState = _uiState.asStateFlow()

    init {
        val currentUserId = getCurrentUserIdUseCase()
        observeTripUseCase(tripId)
            .onEach { trip ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        trip = trip,
                        isClient = trip.clientId == currentUserId
                    )
                }
            }
            .catch { e ->
                logError("TripDetailViewModel", "Failed to observe trip $tripId: ${e.message}")
                _uiState.update { it.copy(isLoading = false, error = TripDetailError.LoadError) }
            }
            .launchIn(viewModelScope)
    }

    fun onAction(action: TripDetailAction) {
        when (action) {
            TripDetailAction.CancelTrip -> cancelTrip()
        }
    }

    private fun cancelTrip() {
        val trip = _uiState.value.trip ?: return
        if (_uiState.value.isCancelling) return
        viewModelScope.launch {
            _uiState.update { it.copy(isCancelling = true, error = null) }
            cancelTripUseCase(trip).fold(
                onSuccess = { _uiState.update { it.copy(isCancelling = false) } },
                onFailure = {
                    _uiState.update {
                        it.copy(isCancelling = false, error = TripDetailError.CancelError)
                    }
                }
            )
        }
    }
}
