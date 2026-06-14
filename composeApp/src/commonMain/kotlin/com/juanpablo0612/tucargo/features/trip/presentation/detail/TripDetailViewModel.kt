package com.juanpablo0612.tucargo.features.trip.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanpablo0612.tucargo.core.location.DriverLocation
import com.juanpablo0612.tucargo.core.location.GeoUtils
import com.juanpablo0612.tucargo.core.logging.logError
import com.juanpablo0612.tucargo.domain.model.Trip
import com.juanpablo0612.tucargo.domain.model.TripStatus
import com.juanpablo0612.tucargo.domain.usecase.CancelTripUseCase
import com.juanpablo0612.tucargo.domain.usecase.GetCurrentUserIdUseCase
import com.juanpablo0612.tucargo.domain.usecase.ObserveDriverLocationUseCase
import com.juanpablo0612.tucargo.domain.usecase.ObserveTripUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.max

private val ACTIVE_TRACKING_STATUSES = setOf(
    TripStatus.ACCEPTED, TripStatus.AT_PICKUP, TripStatus.IN_TRANSIT, TripStatus.AT_DROPOFF
)

class TripDetailViewModel(
    private val tripId: String,
    observeTripUseCase: ObserveTripUseCase,
    getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val cancelTripUseCase: CancelTripUseCase,
    private val observeDriverLocationUseCase: ObserveDriverLocationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TripDetailState())
    val uiState = _uiState.asStateFlow()

    private var driverLocationJob: Job? = null
    private var lastObservedDriverId: String? = null

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
                maybeStartDriverLocationObservation(trip)
            }
            .catch { e ->
                logError("TripDetailViewModel", "Failed to observe trip $tripId: ${e.message}")
                _uiState.update { it.copy(isLoading = false, error = TripDetailError.LoadError) }
            }
            .launchIn(viewModelScope)
    }

    private fun maybeStartDriverLocationObservation(trip: Trip) {
        val driverId = trip.driverId ?: return
        if (!ACTIVE_TRACKING_STATUSES.contains(trip.status)) return
        if (driverId == lastObservedDriverId) return

        lastObservedDriverId = driverId
        driverLocationJob?.cancel()
        driverLocationJob = observeDriverLocationUseCase(driverId)
            .onEach { loc ->
                val eta = computeEta(trip, loc)
                _uiState.update { it.copy(driverLocation = loc, etaMinutes = eta) }
            }
            .catch { e ->
                logError("TripDetailViewModel", "Driver location observation failed: ${e.message}")
            }
            .launchIn(viewModelScope)
    }

    private fun computeEta(trip: Trip, loc: DriverLocation): Int {
        val target = when (trip.status) {
            TripStatus.ACCEPTED, TripStatus.AT_PICKUP -> trip.origin
            else -> trip.destination
        }
        val distanceM = GeoUtils.haversineDistance(loc.lat, loc.lng, target.lat, target.lng)
        val minutes = ceil(distanceM / 1000.0 / 20.0 * 60.0).toInt()
        return max(1, minutes)
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
