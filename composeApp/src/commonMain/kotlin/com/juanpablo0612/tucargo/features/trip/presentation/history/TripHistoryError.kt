package com.juanpablo0612.tucargo.features.trip.presentation.history

sealed interface TripHistoryError {
    data object LoadError : TripHistoryError
}
