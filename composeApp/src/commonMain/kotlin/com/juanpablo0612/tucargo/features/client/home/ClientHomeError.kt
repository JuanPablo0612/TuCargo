package com.juanpablo0612.tucargo.features.client.home

sealed interface ClientHomeError {
    data object LoadUserError : ClientHomeError
    data object LoadTripsError : ClientHomeError
}
