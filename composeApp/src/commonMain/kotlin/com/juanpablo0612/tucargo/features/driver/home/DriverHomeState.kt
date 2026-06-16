package com.juanpablo0612.tucargo.features.driver.home

import androidx.compose.runtime.Immutable
import com.juanpablo0612.tucargo.domain.model.Trip
import com.juanpablo0612.tucargo.domain.model.TripOffer
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class DriverHomeState(
    val isLoading: Boolean = false,
    val driverName: String = "",
    val isAvailable: Boolean = false,
    val hasLocationPermission: Boolean = false,
    val balance: Double = 0.0,
    val totalTrips: Int = 0,
    val activeTrips: ImmutableList<Trip> = persistentListOf(),
    val availableTrips: ImmutableList<Trip> = persistentListOf(),
    val isAccepting: Boolean = false,
    val activeOffer: TripOffer? = null,
    val showOfferDialog: Boolean = false,
    val isAcceptingOffer: Boolean = false,
    val isRejectingOffer: Boolean = false,
    val error: DriverHomeError? = null
)
