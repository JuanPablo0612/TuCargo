package com.juanpablo0612.tucargo.features.trip.presentation.active

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanpablo0612.tucargo.core.logging.logError
import com.juanpablo0612.tucargo.domain.model.TripStatus
import com.juanpablo0612.tucargo.domain.trip.nextDriverStatus
import com.juanpablo0612.tucargo.domain.usecase.AdvanceTripStatusUseCase
import com.juanpablo0612.tucargo.domain.usecase.ObserveTripUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TripActiveViewModel(
    private val tripId: String,
    observeTripUseCase: ObserveTripUseCase,
    private val advanceTripStatusUseCase: AdvanceTripStatusUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TripActiveState())
    val uiState = _uiState.asStateFlow()

    init {
        observeTripUseCase(tripId)
            .onEach { trip -> _uiState.update { it.copy(isLoading = false, trip = trip) } }
            .catch { e ->
                logError("TripActiveViewModel", "Failed to observe trip $tripId: ${e.message}")
                _uiState.update { it.copy(isLoading = false, error = TripActiveError.LoadError) }
            }
            .launchIn(viewModelScope)
    }

    fun onAction(action: TripActiveAction) {
        when (action) {
            TripActiveAction.AdvanceStatus -> advance(requireCode = false, code = null)
            is TripActiveAction.CompleteWithCode -> advance(requireCode = true, code = action.code)
        }
    }

    private fun advance(requireCode: Boolean, code: String?) {
        val trip = _uiState.value.trip ?: return
        if (_uiState.value.isUpdating) return
        val next = trip.status.nextDriverStatus() ?: return

        if (next == TripStatus.COMPLETED) {
            // UI-level deterrent only: real enforcement needs a Cloud
            // Function, since the driver can read the trip document.
            if (!requireCode || code?.trim() != trip.deliveryCode) {
                _uiState.update { it.copy(error = TripActiveError.InvalidDeliveryCode) }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true, error = null) }
            advanceTripStatusUseCase(trip, next).fold(
                onSuccess = { _uiState.update { it.copy(isUpdating = false) } },
                onFailure = {
                    _uiState.update {
                        it.copy(isUpdating = false, error = TripActiveError.UpdateError)
                    }
                }
            )
        }
    }
}
