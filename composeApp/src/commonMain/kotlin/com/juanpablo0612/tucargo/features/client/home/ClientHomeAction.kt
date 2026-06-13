package com.juanpablo0612.tucargo.features.client.home

sealed interface ClientHomeAction {
    data object LoadData : ClientHomeAction
    data object RefreshTrips : ClientHomeAction
    data object SignOut : ClientHomeAction
    data class OnLocationUpdated(val latitude: Double, val longitude: Double) : ClientHomeAction
}
