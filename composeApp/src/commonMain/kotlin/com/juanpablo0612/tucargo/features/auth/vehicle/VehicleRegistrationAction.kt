package com.juanpablo0612.tucargo.features.auth.vehicle

import com.juanpablo0612.tucargo.domain.model.VehicleType

sealed interface VehicleRegistrationAction {
    data class SelectVehicleType(val type: VehicleType) : VehicleRegistrationAction
    data object Submit : VehicleRegistrationAction
    data object OnBackClick : VehicleRegistrationAction
}
