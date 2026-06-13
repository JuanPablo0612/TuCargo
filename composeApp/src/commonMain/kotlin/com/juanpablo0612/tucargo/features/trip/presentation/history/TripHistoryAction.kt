package com.juanpablo0612.tucargo.features.trip.presentation.history

sealed interface TripHistoryAction {
    data object Refresh : TripHistoryAction
}
