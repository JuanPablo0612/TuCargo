package com.juanpablo0612.tucargo.features.trip.active

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanpablo0612.tucargo.core.location.GeoUtils
import com.juanpablo0612.tucargo.core.location.LocationProvider
import com.juanpablo0612.tucargo.core.logging.logError
import com.juanpablo0612.tucargo.domain.model.AppError
import com.juanpablo0612.tucargo.domain.model.TripStatus
import com.juanpablo0612.tucargo.domain.trip.nextDriverStatus
import com.juanpablo0612.tucargo.domain.usecase.trip.AdvanceTripStatusUseCase
import com.juanpablo0612.tucargo.domain.usecase.trip.CompleteTripUseCase
import com.juanpablo0612.tucargo.domain.usecase.trip.ObserveTripUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val GEOFENCE_RADIUS_METERS = 50.0

class TripActiveViewModel(
    private val tripId: String,
    observeTripUseCase: ObserveTripUseCase,
    private val advanceTripStatusUseCase: AdvanceTripStatusUseCase,
    private val completeTripUseCase: CompleteTripUseCase,
    private val locationProvider: LocationProvider,
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
            TripActiveAction.AdvanceStatus -> handleAdvanceStatus()
            is TripActiveAction.CompleteWithCode -> completeWithCode(action.code)
            TripActiveAction.ConfirmGeofenceOverride -> {
                _uiState.update { it.copy(showGeofenceDialog = false) }
                doAdvance()
            }
            TripActiveAction.DismissGeofenceDialog ->
                _uiState.update { it.copy(showGeofenceDialog = false) }
        }
    }

    private fun handleAdvanceStatus() {
        val trip = _uiState.value.trip ?: return
        if (_uiState.value.isUpdating) return

        if (trip.status == TripStatus.ACCEPTED) {
            viewModelScope.launch {
                val location = locationProvider.getCurrentLocation()
                if (location != null) {
                    val distance = GeoUtils.haversineDistance(
                        location.latitude, location.longitude,
                        trip.origin.lat, trip.origin.lng
                    )
                    if (distance > GEOFENCE_RADIUS_METERS) {
                        _uiState.update { it.copy(showGeofenceDialog = true) }
                        return@launch
                    }
                }
                doAdvance()
            }
        } else {
            doAdvance()
        }
    }

    private fun doAdvance() {
        val trip = _uiState.value.trip ?: return
        val next = trip.status.nextDriverStatus() ?: return
        if (next == TripStatus.COMPLETED) return  // handled by completeWithCode

        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true, error = null) }
            advanceTripStatusUseCase(trip, next).fold(
                onSuccess = { _uiState.update { it.copy(isUpdating = false) } },
                onFailure = {
                    _uiState.update { it.copy(isUpdating = false, error = TripActiveError.UpdateError) }
                }
            )
        }
    }

    private fun completeWithCode(code: String) {
        val trip = _uiState.value.trip ?: return
        if (_uiState.value.isUpdating) return

        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true, error = null) }
            completeTripUseCase(trip.id, code).fold(
                onSuccess = { _uiState.update { it.copy(isUpdating = false) } },
                onFailure = { e ->
                    val error = when (e) {
                        is AppError.Trip.DeliveryCodeInvalid ->
                            TripActiveError.DeliveryCodeInvalid(e.remaining)
                        is AppError.Trip.DeliveryCodeLocked ->
                            TripActiveError.DeliveryCodeLocked
                        else -> TripActiveError.UpdateError
                    }
                    _uiState.update { it.copy(isUpdating = false, error = error) }
                }
            )
        }
    }
}
