package com.juanpablo0612.tucargo.features.trip.presentation.detail

sealed interface TripDetailError {
    data object LoadError : TripDetailError
    data object CancelError : TripDetailError
}
