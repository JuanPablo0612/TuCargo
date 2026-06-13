package com.juanpablo0612.tucargo.features.trip.presentation.detail

sealed interface TripDetailAction {
    data object CancelTrip : TripDetailAction
}
