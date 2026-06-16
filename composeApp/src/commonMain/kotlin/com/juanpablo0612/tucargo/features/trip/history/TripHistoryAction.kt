package com.juanpablo0612.tucargo.features.trip.history

sealed interface TripHistoryAction {
    data object Refresh : TripHistoryAction
}
