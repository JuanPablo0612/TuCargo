package com.juanpablo0612.tucargo.features.driver.home.presentation

sealed interface DriverHomeAction {
    data class ToggleAvailability(val available: Boolean) : DriverHomeAction
    data class LocationPermissionResult(val granted: Boolean) : DriverHomeAction
    data class AcceptTrip(val tripId: String) : DriverHomeAction
}
