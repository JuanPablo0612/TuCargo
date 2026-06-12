package com.juanpablo0612.tucargo.features.trip.presentation.active

sealed interface TripActiveAction {
    /** Single forward step in the lifecycle (ASSIGNED→ON_WAY→…→IN_PROGRESS). */
    data object AdvanceStatus : TripActiveAction

    /** Final step: complete the delivery after checking the recipient's code. */
    data class CompleteWithCode(val code: String) : TripActiveAction
}
