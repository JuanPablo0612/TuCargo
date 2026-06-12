package com.juanpablo0612.tucargo.features.driver.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanpablo0612.tucargo.domain.model.Trip
import com.juanpablo0612.tucargo.domain.model.TripStatus
import com.juanpablo0612.tucargo.domain.trip.TrackingState
import com.juanpablo0612.tucargo.domain.trip.TripTracker
import com.juanpablo0612.tucargo.domain.usecase.GetCurrentUserIdUseCase
import com.juanpablo0612.tucargo.domain.usecase.GetCurrentUserUseCase
import com.juanpablo0612.tucargo.domain.usecase.ObserveDriverActiveTripsUseCase
import com.juanpablo0612.tucargo.domain.usecase.UpdateDriverStatusUseCase
import kotlinx.collections.immutable.toImmutableList
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
    private val observeDriverActiveTripsUseCase: ObserveDriverActiveTripsUseCase,
    private val tripTracker: TripTracker
) : ViewModel() {

    private val _uiState = MutableStateFlow(DriverHomeState())
    val uiState = _uiState.asStateFlow()

    init {
        loadDriverData()
        observeActiveTrips()
        observeTrackerState()
    }

    fun onAction(action: DriverHomeAction) {
        when (action) {
            is DriverHomeAction.ToggleAvailability -> toggleAvailability(action.available)
            is DriverHomeAction.LocationPermissionResult -> onLocationPermissionResult(action.granted)
        }
    }

    private fun onLocationPermissionResult(granted: Boolean) {
        _uiState.update {
            it.copy(
                hasLocationPermission = granted,
                error = if (granted) it.error else DriverHomeError.LocationPermissionDenied
            )
        }
        if (granted) {
            trackableTripId(_uiState.value.activeTrips)?.let { tripId ->
                tripTracker.startTracking(tripId)
            }
        }
    }

    private fun trackableTripId(trips: List<Trip>): String? = trips.firstOrNull {
        it.status == TripStatus.IN_PROGRESS || it.status == TripStatus.ON_WAY || it.status == TripStatus.ARRIVED_PICKUP
    }?.id

    private fun observeTrackerState() {
        tripTracker.state.onEach { state ->
            if (state is TrackingState.Error) {
                _uiState.update { it.copy(error = DriverHomeError.TrackingError) }
            }
        }.launchIn(viewModelScope)
    }

    private fun observeActiveTrips() {
        val userId = getCurrentUserIdUseCase() ?: return
        observeDriverActiveTripsUseCase(userId)
            .onEach { trips ->
                _uiState.update { it.copy(activeTrips = trips.toImmutableList()) }
                val activeTripId = trackableTripId(trips)
                if (activeTripId != null) {
                    // Without the permission the provider flow fails
                    // immediately; tracking starts later from
                    // onLocationPermissionResult once it is granted.
                    if (_uiState.value.hasLocationPermission) {
                        tripTracker.startTracking(activeTripId)
                    }
                } else {
                    tripTracker.stopTracking()
                }
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
                        isAvailable = user.isOnline,
                        totalTrips = user.ratingCount // Fallback since totalTrips isn't in User yet
                    )
                }
            }.onFailure {
                _uiState.update { it.copy(isLoading = false, error = DriverHomeError.LoadDriverError) }
            }
        }
    }

    private fun toggleAvailability(available: Boolean) {
        viewModelScope.launch {
            val userId = getCurrentUserIdUseCase() ?: return@launch
            _uiState.update { it.copy(isAvailable = available) }
            updateDriverStatusUseCase(userId, available).onFailure {
                _uiState.update {
                    it.copy(isAvailable = !available, error = DriverHomeError.ToggleAvailabilityError)
                }
            }
        }
    }
}
