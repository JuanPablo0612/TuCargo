package com.juanpablo0612.tucargo.features.trip.detail

import androidx.compose.runtime.Immutable
import com.juanpablo0612.tucargo.core.location.DriverLocation
import com.juanpablo0612.tucargo.domain.model.Trip

@Immutable
data class TripDetailState(
    val isLoading: Boolean = true,
    val trip: Trip? = null,
    val isClient: Boolean = false,
    val isCancelling: Boolean = false,
    val error: TripDetailError? = null,
    val driverLocation: DriverLocation? = null,
    val etaMinutes: Int? = null
)
