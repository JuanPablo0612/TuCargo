package com.juanpablo0612.tucargo.domain.trip

import com.juanpablo0612.tucargo.core.location.DriverLocation
import com.juanpablo0612.tucargo.core.location.LocationProvider
import com.juanpablo0612.tucargo.data.common.ExceptionMapper
import com.juanpablo0612.tucargo.domain.model.AppError
import com.juanpablo0612.tucargo.domain.usecase.tracking.SendLocationUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.FlowPreview
import kotlin.time.Instant

@OptIn(FlowPreview::class)
class TripTracker(
    private val sendLocationUseCase: SendLocationUseCase,
    private val locationProvider: LocationProvider,
    private val applicationScope: CoroutineScope
) {
    private var trackingJob: Job? = null
    private val _state = MutableStateFlow<TrackingState>(TrackingState.Idle)
    val state = _state.asStateFlow()

    fun startTracking(tripId: String, driverId: String, intervalMillis: Long = 4_000L) {
        if (_state.value is TrackingState.Tracking) return

        trackingJob?.cancel()
        _state.value = TrackingState.Tracking(tripId)

        trackingJob = locationProvider.getLocations()
            .sample(intervalMillis)
            .onEach { update ->
                val driverLocation = DriverLocation(
                    lat = update.latitude,
                    lng = update.longitude,
                    accuracyM = update.accuracy ?: 0f,
                    speedKph = update.speedMs?.let { it * 3.6f },
                    headingDeg = update.bearingDeg,
                    capturedAt = Instant.fromEpochMilliseconds(update.timestamp),
                    tripId = tripId
                )
                sendLocationUseCase(driverId, driverLocation).onFailure { e ->
                    failTracking(ExceptionMapper.map(e))
                }
            }
            .catch { e -> _state.value = TrackingState.Error(ExceptionMapper.map(e)) }
            .launchIn(applicationScope)
    }

    fun stopTracking() {
        trackingJob?.cancel()
        trackingJob = null
        _state.value = TrackingState.Idle
    }

    private fun failTracking(error: AppError) {
        _state.value = TrackingState.Error(error)
        trackingJob?.cancel()
        trackingJob = null
    }
}
