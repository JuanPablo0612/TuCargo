package com.juanpablo0612.tucargo.features.trip.presentation.history

import androidx.compose.runtime.Immutable
import com.juanpablo0612.tucargo.domain.model.Trip
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class TripHistoryState(
    val isLoading: Boolean = true,
    val trips: ImmutableList<Trip> = persistentListOf(),
    val error: TripHistoryError? = null
)
