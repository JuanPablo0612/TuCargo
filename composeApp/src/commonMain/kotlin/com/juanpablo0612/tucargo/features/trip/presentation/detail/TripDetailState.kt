package com.juanpablo0612.tucargo.features.trip.presentation.detail

import androidx.compose.runtime.Immutable
import com.juanpablo0612.tucargo.domain.model.Trip

@Immutable
data class TripDetailState(
    val isLoading: Boolean = true,
    val trip: Trip? = null,
    val isClient: Boolean = false,
    val isCancelling: Boolean = false,
    val error: TripDetailError? = null
)
