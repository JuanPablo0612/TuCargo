package com.juanpablo0612.tucargo.features.driver.home.presentation

sealed interface DriverHomeError {
    data object LoadDriverError : DriverHomeError
    data object ToggleAvailabilityError : DriverHomeError
    data object TrackingError : DriverHomeError
    data object LocationPermissionDenied : DriverHomeError
    data object AvailableTripsError : DriverHomeError
    data object AcceptTripError : DriverHomeError
    data object TripAlreadyTaken : DriverHomeError
    data object AcceptOfferError : DriverHomeError
    data object RejectOfferError : DriverHomeError
    data object OfferExpiredError : DriverHomeError
    data object WalletInsufficientError : DriverHomeError
}
