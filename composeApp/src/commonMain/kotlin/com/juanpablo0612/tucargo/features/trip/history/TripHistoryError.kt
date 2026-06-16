package com.juanpablo0612.tucargo.features.trip.history

sealed interface TripHistoryError {
    data object LoadError : TripHistoryError
}
