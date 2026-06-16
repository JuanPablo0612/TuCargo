package com.juanpablo0612.tucargo.features.trip.detail

sealed interface TripDetailAction {
    data object CancelTrip : TripDetailAction
}
