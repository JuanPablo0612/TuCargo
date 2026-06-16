package com.juanpablo0612.tucargo.features.trip.completed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanpablo0612.tucargo.core.logging.logError
import com.juanpablo0612.tucargo.domain.usecase.GetCurrentUserIdUseCase
import com.juanpablo0612.tucargo.domain.usecase.ObserveTripUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

class TripCompletedViewModel(
    private val tripId: String,
    observeTripUseCase: ObserveTripUseCase,
    getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TripCompletedState())
    val uiState = _uiState.asStateFlow()

    init {
        val currentUserId = getCurrentUserIdUseCase()
        observeTripUseCase(tripId)
            .onEach { trip ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        trip = trip,
                        isDriver = trip.driverId == currentUserId,
                    )
                }
            }
            .catch { e ->
                logError("TripCompletedViewModel", "Failed to observe trip $tripId: ${e.message}")
                _uiState.update { it.copy(isLoading = false, loadError = true) }
            }
            .launchIn(viewModelScope)
    }
}
