package com.juanpablo0612.tucargo.features.driver.home.presentation

sealed interface DriverHomeError {
    data object LoadDriverError : DriverHomeError
    data object ToggleAvailabilityError : DriverHomeError
    data object TrackingError : DriverHomeError
}
