// RUTA: composeApp/src/commonMain/kotlin/com/juanpablo0612/tucargo/features/client/home/ClientHomeState.kt

package com.juanpablo0612.tucargo.features.client.home

import com.juanpablo0612.tucargo.data.trip.Trip
import com.juanpablo0612.tucargo.data.user.User

data class ClientHomeState(
    val isLoading: Boolean = false,
    val user: User = User(),
    val recentTrips: List<Trip> = emptyList(),
    val isLoadingTrips: Boolean = false,
    val userLatitude: Double = 4.7110,
    val userLongitude: Double = -74.0721,
    val errorMessage: String? = null
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
