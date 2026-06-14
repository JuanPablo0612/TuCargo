package com.juanpablo0612.tucargo.features.trip.presentation.active

sealed interface TripActiveError {
    data object LoadError : TripActiveError
    data object UpdateError : TripActiveError
    data class DeliveryCodeInvalid(val remaining: Int) : TripActiveError
    data object DeliveryCodeLocked : TripActiveError
}
