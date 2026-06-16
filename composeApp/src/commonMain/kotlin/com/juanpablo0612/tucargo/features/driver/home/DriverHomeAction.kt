package com.juanpablo0612.tucargo.features.driver.home

sealed interface DriverHomeAction {
    data class ToggleAvailability(val available: Boolean) : DriverHomeAction
    data class LocationPermissionResult(val granted: Boolean) : DriverHomeAction
    data class AcceptTrip(val tripId: String) : DriverHomeAction
    data class AcceptOffer(val offerId: String, val tripId: String) : DriverHomeAction
    data class RejectOffer(val offerId: String, val tripId: String) : DriverHomeAction
    data object DismissOffer : DriverHomeAction
}
