package com.juanpablo0612.tucargo.features.client.home

import androidx.compose.runtime.Immutable
import com.juanpablo0612.tucargo.data.trip.Trip
import com.juanpablo0612.tucargo.domain.model.User
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class ClientHomeState(
    val isLoading: Boolean = false,
    val user: User = User(),
    val recentTrips: ImmutableList<Trip> = persistentListOf(),
    val isLoadingTrips: Boolean = false,
    val userLatitude: Double = 4.7110,
    val userLongitude: Double = -74.0721,
    val error: ClientHomeError? = null
)

sealed interface ClientHomeAction {
    data object LoadData : ClientHomeAction
    data object RefreshTrips : ClientHomeAction
    data object NewTrip : ClientHomeAction
    data object SignOut : ClientHomeAction
    data class OnLocationUpdated(
        val latitude: Double,
        val longitude: Double
    ) : ClientHomeAction
}
