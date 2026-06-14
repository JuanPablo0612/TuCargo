package com.juanpablo0612.tucargo.features.driver.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanpablo0612.tucargo.core.fcm.OfferEventBus
import com.juanpablo0612.tucargo.core.logging.logError
import com.juanpablo0612.tucargo.core.service.LocationServiceController
import com.juanpablo0612.tucargo.domain.model.AppError
import com.juanpablo0612.tucargo.domain.model.Cop
import com.juanpablo0612.tucargo.domain.model.OfferResponse
import com.juanpablo0612.tucargo.domain.model.Trip
import com.juanpablo0612.tucargo.domain.model.TripOffer
import com.juanpablo0612.tucargo.domain.model.TripStatus
import com.juanpablo0612.tucargo.domain.trip.TrackingState
import com.juanpablo0612.tucargo.domain.trip.TripTracker
import com.juanpablo0612.tucargo.domain.usecase.AcceptOfferUseCase
import com.juanpablo0612.tucargo.domain.usecase.AcceptTripUseCase
import com.juanpablo0612.tucargo.domain.usecase.GetCurrentUserIdUseCase
import com.juanpablo0612.tucargo.domain.usecase.GetCurrentUserUseCase
import com.juanpablo0612.tucargo.domain.usecase.ObserveAvailableTripsUseCase
import com.juanpablo0612.tucargo.domain.usecase.ObserveDriverActiveTripsUseCase
import com.juanpablo0612.tucargo.domain.usecase.RejectOfferUseCase
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
    private val acceptOfferUseCase: AcceptOfferUseCase,
    private val rejectOfferUseCase: RejectOfferUseCase,
    private val tripTracker: TripTracker,
    private val locationServiceController: LocationServiceController
) : ViewModel() {

    private val _uiState = MutableStateFlow(DriverHomeState())
    val uiState = _uiState.asStateFlow()

    private var availableTripsJob: Job? = null

    init {
        loadDriverData()
        observeActiveTrips()
        observeTrackerState()
        observeOfferEvents()
    }

    fun onAction(action: DriverHomeAction) {
        when (action) {
            is DriverHomeAction.ToggleAvailability -> toggleAvailability(action.available)
            is DriverHomeAction.LocationPermissionResult -> onLocationPermissionResult(action.granted)
            is DriverHomeAction.AcceptTrip -> acceptTrip(action.tripId)
            is DriverHomeAction.AcceptOffer -> acceptOffer(action.offerId, action.tripId)
            is DriverHomeAction.RejectOffer -> rejectOffer(action.offerId, action.tripId)
            is DriverHomeAction.DismissOffer -> dismissOffer()
        }
    }

    private fun observeOfferEvents() {
        OfferEventBus.incomingOffers.onEach { data ->
            val offer = parseFcmOffer(data) ?: return@onEach
            _uiState.update { it.copy(activeOffer = offer, showOfferDialog = true) }
        }.launchIn(viewModelScope)

        OfferEventBus.cancelledOffers.onEach { tripId ->
            val current = _uiState.value.activeOffer
            if (current != null && current.tripId == tripId) {
                _uiState.update { it.copy(showOfferDialog = false, activeOffer = null) }
            }
        }.launchIn(viewModelScope)
    }

    private fun parseFcmOffer(data: Map<String, String>): TripOffer? {
        return try {
            TripOffer(
                id = data["offer_id"] ?: return null,
                tripId = data["trip_id"] ?: return null,
                driverId = data["driver_id"] ?: "",
                attempt = data["attempt"]?.toIntOrNull() ?: 0,
                sentAt = data["sent_at"]?.toLongOrNull() ?: 0L,
                expiresAt = data["expires_at"]?.toLongOrNull() ?: return null,
                response = OfferResponse.PENDING,
                totalPrice = Cop(data["total_price"]?.toIntOrNull() ?: return null),
                commissionFee = Cop(data["commission_fee"]?.toIntOrNull() ?: return null),
                distanceKm = data["distance_km"]?.toDoubleOrNull() ?: 0.0,
                originAddr = data["origin_addr"] ?: "",
                destAddr = data["dest_addr"] ?: ""
            )
        } catch (e: Exception) {
            logError("DriverHomeViewModel", "Failed to parse FCM offer: ${e.message}")
            null
        }
    }

    private fun acceptOffer(offerId: String, tripId: String) {
        if (_uiState.value.isAcceptingOffer) return
        viewModelScope.launch {
            _uiState.update { it.copy(isAcceptingOffer = true, error = null) }
            acceptOfferUseCase(tripId, offerId).fold(
                onSuccess = {
                    _uiState.update { it.copy(isAcceptingOffer = false, showOfferDialog = false, activeOffer = null) }
                },
                onFailure = { e ->
                    val error = when (e) {
                        is AppError.Trip.OfferExpired -> DriverHomeError.OfferExpiredError
                        is AppError.Trip.WalletInsufficient -> DriverHomeError.WalletInsufficientError
                        else -> DriverHomeError.AcceptOfferError
                    }
                    _uiState.update { it.copy(isAcceptingOffer = false, error = error) }
                }
            )
        }
    }

    private fun rejectOffer(offerId: String, tripId: String) {
        if (_uiState.value.isRejectingOffer) return
        viewModelScope.launch {
            _uiState.update { it.copy(isRejectingOffer = true, error = null) }
            rejectOfferUseCase(tripId, offerId).fold(
                onSuccess = {
                    _uiState.update { it.copy(isRejectingOffer = false, showOfferDialog = false, activeOffer = null) }
                },
                onFailure = { e ->
                    logError("DriverHomeViewModel", "Failed to reject offer: ${e.message}")
                    _uiState.update { it.copy(isRejectingOffer = false, error = DriverHomeError.RejectOfferError) }
                }
            )
        }
    }

    private fun dismissOffer() {
        _uiState.update { it.copy(showOfferDialog = false, activeOffer = null) }
    }

    private fun onLocationPermissionResult(granted: Boolean) {
        _uiState.update {
            it.copy(
                hasLocationPermission = granted,
                error = if (granted) it.error else DriverHomeError.LocationPermissionDenied
            )
        }
        if (granted) {
            val userId = getCurrentUserIdUseCase() ?: return
            locationServiceController.startService(userId)
            val activeTripId = trackableTripId(_uiState.value.activeTrips)
            locationServiceController.updateTripMode(activeTripId)
        }
    }

    private fun trackableTripId(trips: List<Trip>): String? = trips.firstOrNull {
        it.status == TripStatus.ACCEPTED ||
            it.status == TripStatus.AT_PICKUP ||
            it.status == TripStatus.IN_TRANSIT ||
            it.status == TripStatus.AT_DROPOFF
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
                locationServiceController.updateTripMode(activeTripId)
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
                        totalTrips = user.ratingCount
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
            updateDriverStatusUseCase(userId, available).fold(
                onSuccess = {
                    if (!available) locationServiceController.stopService()
                },
                onFailure = {
                    _uiState.update {
                        it.copy(isAvailable = !available, error = DriverHomeError.ToggleAvailabilityError)
                    }
                    setAvailableTripsCollection(!available)
                    if (available) locationServiceController.stopService()
                }
            )
        }
    }
}
