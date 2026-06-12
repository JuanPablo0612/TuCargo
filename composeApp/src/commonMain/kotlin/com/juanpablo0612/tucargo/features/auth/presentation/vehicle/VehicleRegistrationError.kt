package com.juanpablo0612.tucargo.features.auth.presentation.vehicle

sealed interface VehicleRegistrationError {
    data object SaveError : VehicleRegistrationError
    data object UserNotAuthenticated : VehicleRegistrationError
}
