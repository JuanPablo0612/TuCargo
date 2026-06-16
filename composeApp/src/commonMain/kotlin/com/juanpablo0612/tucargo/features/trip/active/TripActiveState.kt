package com.juanpablo0612.tucargo.features.trip.active

import androidx.compose.runtime.Immutable
import com.juanpablo0612.tucargo.domain.model.Trip

@Immutable
data class TripActiveState(
    val isLoading: Boolean = true,
    val trip: Trip? = null,
    val isUpdating: Boolean = false,
    val error: TripActiveError? = null,
    val showGeofenceDialog: Boolean = false,
)
