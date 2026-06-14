package com.juanpablo0612.tucargo.features.trip.presentation.completed

import androidx.compose.runtime.Immutable
import com.juanpablo0612.tucargo.domain.model.Trip

@Immutable
data class TripCompletedState(
    val isLoading: Boolean = true,
    val trip: Trip? = null,
    val isDriver: Boolean = false,
    val loadError: Boolean = false,
)
