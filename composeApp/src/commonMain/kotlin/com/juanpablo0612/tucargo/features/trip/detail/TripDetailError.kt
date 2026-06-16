package com.juanpablo0612.tucargo.features.trip.detail

sealed interface TripDetailError {
    data object LoadError : TripDetailError
    data object CancelError : TripDetailError
}
