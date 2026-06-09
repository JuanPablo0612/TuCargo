package com.juanpablo0612.tucargo.features.driver.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanpablo0612.tucargo.data.trip.TripStatus
import com.juanpablo0612.tucargo.data.trip.TripTrackingManager
import com.juanpablo0612.tucargo.domain.usecase.GetCurrentUserIdUseCase
import com.juanpablo0612.tucargo.domain.usecase.GetCurrentUserUseCase
import com.juanpablo0612.tucargo.domain.usecase.UpdateDriverStatusUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DriverHomeViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val updateDriverStatusUseCase: UpdateDriverStatusUseCase,
    private val trackingManager: TripTrackingManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DriverHomeState())
    val uiState = _uiState.asStateFlow()

    private val currentUserId: String = getCurrentUserIdUseCase() ?: ""

    init {
        loadDriverData()
        observeActiveTrips()
    }

    fun onAction(action: DriverHomeAction) {
        when (action) {
            is DriverHomeAction.ToggleAvailability -> toggleAvailability(action.available)
        }
    }

    private fun observeActiveTrips() {
        uiState.onEach { currentState ->
            val activeTrip = currentState.activeTrips.firstOrNull {
                it.status == TripStatus.IN_PROGRESS || it.status == TripStatus.ON_WAY
            }
            if (activeTrip != null) trackingManager.startTracking(activeTrip.id)
            else trackingManager.stopTracking()
        }.launchIn(viewModelScope)
    }

    private fun loadDriverData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getCurrentUserUseCase().onSuccess { user ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        driverName = user.fullName,
                        balance = user.walletBalance,
                        isAvailable = user.isOnline
                    )
                }
            }.onFailure {
                _uiState.update { it.copy(isLoading = false, error = DriverHomeError.LoadDriverError) }
            }
        }
    }

    private fun toggleAvailability(available: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAvailable = available) }
            if (currentUserId.isNotEmpty()) {
                updateDriverStatusUseCase(currentUserId, available).onFailure {
                    _uiState.update {
                        it.copy(isAvailable = !available, error = DriverHomeError.ToggleAvailabilityError)
                    }
                }
            }
        }
    }
}
