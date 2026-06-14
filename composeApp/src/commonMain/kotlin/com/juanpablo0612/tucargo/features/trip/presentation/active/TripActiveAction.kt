package com.juanpablo0612.tucargo.features.trip.presentation.active

sealed interface TripActiveAction {
    /** Single forward step in the lifecycle (ACCEPTED → AT_PICKUP → IN_TRANSIT → AT_DROPOFF). */
    data object AdvanceStatus : TripActiveAction

    /** Final step: complete the delivery after the driver enters the recipient's 4-digit code. */
    data class CompleteWithCode(val code: String) : TripActiveAction

    /** Driver confirmed the geofence override (> 50 m from origin). */
    data object ConfirmGeofenceOverride : TripActiveAction

    /** Driver cancelled the geofence override dialog. */
    data object DismissGeofenceDialog : TripActiveAction
}
