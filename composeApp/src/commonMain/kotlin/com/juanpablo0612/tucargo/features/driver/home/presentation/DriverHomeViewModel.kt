package com.juanpablo0612.tucargo.features.driver.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanpablo0612.tucargo.core.logging.logError
import com.juanpablo0612.tucargo.domain.model.AppError
import com.juanpablo0612.tucargo.domain.model.Trip
import com.juanpablo0612.tucargo.domain.model.TripStatus
import com.juanpablo0612.tucargo.domain.trip.TrackingState
import com.juanpablo0612.tucargo.domain.trip.TripTracker
import com.juanpablo0612.tucargo.domain.usecase.AcceptTripUseCase
import com.juanpablo0612.tucargo.domain.usecase.GetCurrentUserIdUseCase
import com.juanpablo0612.tucargo.domain.usecase.GetCurrentUserUseCase
import com.juanpablo0612.tucargo.domain.usecase.ObserveAvailableTripsUseCase
import com.juanpablo0612.tucargo.domain.usecase.ObserveDriverActiveTripsUseCase
import com.juanpablo0612.tucargo.domain.usecase.UpdateDriverStatusUseCase
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

class DriverHomeViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val updateDriverStatusUseCase: UpdateDriverStatusUseCase,
    private val observeDriverActiveTripsUseCase: ObserveDriverActiveTripsUseCase,
    private val observeAvailableTripsUseCase: ObserveAvailableTripsUseCase,
    private val acceptTripUseCase: AcceptTripUseCase,
    private val tripTracker: TripTracker
) : ViewModel() {

    private val _uiState = MutableStateFlow(DriverHomeState())
    val uiState = _uiState.asStateFlow()

    private var availableTripsJob: Job? = null

    init {
        loadDriverData()
        observeActiveTrips()
        observeTrackerState()
    }

    fun onAction(action: DriverHomeAction) {
        when (action) {
            is DriverHomeAction.ToggleAvailability -> toggleAvailability(action.available)
            is DriverHomeAction.LocationPermissionResult -> onLocationPermissionResult(action.granted)
            is DriverHomeAction.AcceptTrip -> acceptTrip(action.tripId)
        }
    }

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
                val activeTrip = trips.firstOrNull {
                    it.status == TripStatus.IN_PROGRESS || it.status == TripStatus.ON_WAY || it.status == TripStatus.ARRIVED_PICKUP
                }
                if (activeTrip != null) {
                    tripTracker.startTracking(activeTrip.id)
                } else {
                    tripTracker.stopTracking()
                }
            }.launchIn(viewModelScope)
    }

    private fun setAvailableTripsCollection(enabled: Boolean) {
        if (enabled) {
            if (availableTripsJob?.isActive == true) return
            availableTripsJob = observeAvailableTripsUseCase()
                .onEach { trips ->
                    _uiState.update { it.copy(availableTrips = trips.toImmutableList()) }
                }
                .catch { e ->
                    logError("DriverHomeViewModel", "Failed to observe available trips: ${e.message}")
                    _uiState.update { it.copy(error = DriverHomeError.AvailableTripsError) }
                }
                .launchIn(viewModelScope)
        } else {
            availableTripsJob?.cancel()
            availableTripsJob = null
            _uiState.update { it.copy(availableTrips = persistentListOf()) }
        }
    }

    private fun acceptTrip(tripId: String) {
        if (_uiState.value.isAccepting) return
        viewModelScope.launch {
            _uiState.update { it.copy(isAccepting = true, error = null) }
            acceptTripUseCase(tripId).fold(
                onSuccess = {
                    // The trip moves from the available list to the active
                    // list through the existing snapshot listeners.
                    _uiState.update { it.copy(isAccepting = false) }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isAccepting = false,
                            error = when (e) {
                                is AppError.Trip.AlreadyTaken -> DriverHomeError.TripAlreadyTaken
                                else -> DriverHomeError.AcceptTripError
                            }
                        )
                    }
                }
            )
        }
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
                setAvailableTripsCollection(user.isOnline)
            }.onFailure {
                _uiState.update { it.copy(isLoading = false, error = DriverHomeError.LoadDriverError) }
            }
        }
    }

    private fun toggleAvailability(available: Boolean) {
        viewModelScope.launch {
            val userId = getCurrentUserIdUseCase() ?: return@launch
            _uiState.update { it.copy(isAvailable = available) }
            setAvailableTripsCollection(available)
            updateDriverStatusUseCase(userId, available).onFailure {
                _uiState.update {
                    it.copy(isAvailable = !available, error = DriverHomeError.ToggleAvailabilityError)
                }
                setAvailableTripsCollection(!available)
            }
        }
    }
}
