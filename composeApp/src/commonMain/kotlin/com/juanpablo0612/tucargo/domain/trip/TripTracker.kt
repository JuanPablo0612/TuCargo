package com.juanpablo0612.tucargo.domain.trip

import com.juanpablo0612.tucargo.core.location.GeoUtils
import com.juanpablo0612.tucargo.core.location.LocationProvider
import com.juanpablo0612.tucargo.data.common.ExceptionMapper
import com.juanpablo0612.tucargo.data.trip.TripRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class)
class TripTracker(
    private val tripRepository: TripRepository,
    private val locationProvider: LocationProvider,
    private val applicationScope: CoroutineScope
) {
    private var trackingJob: Job? = null
    private val _state = MutableStateFlow<TrackingState>(TrackingState.Idle)
    val state = _state.asStateFlow()

    fun startTracking(tripId: String, intervalMillis: Long = 10_000L) {
        if (_state.value is TrackingState.Tracking) return

        trackingJob?.cancel()
        _state.value = TrackingState.Tracking(tripId)
        
        trackingJob = locationProvider.getLocations()
            .distinctUntilChanged { old, new ->
                GeoUtils.haversineDistance(old.latitude, old.longitude, new.latitude, new.longitude) < 10.0
            }
            .sample(intervalMillis)
            .onEach { location ->
                tripRepository.updateDriverLocation(
                    tripId = tripId,
                    lat = location.latitude,
                    lng = location.longitude
                ).onFailure { e ->
                    _state.value = TrackingState.Error(ExceptionMapper.map(e))
                    stopTracking()
                }
            }
            .launchIn(applicationScope)
    }

    fun stopTracking() {
        trackingJob?.cancel()
        trackingJob = null
        _state.value = TrackingState.Idle
    }
}
