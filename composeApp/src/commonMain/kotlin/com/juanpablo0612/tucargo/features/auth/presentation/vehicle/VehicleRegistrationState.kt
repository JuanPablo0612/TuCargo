package com.juanpablo0612.tucargo.features.auth.presentation.vehicle

import androidx.compose.runtime.Immutable
import com.juanpablo0612.tucargo.core.validation.FieldError
import com.juanpablo0612.tucargo.domain.model.VehicleType

@Immutable
data class VehicleRegistrationState(
    val isLoading: Boolean = false,
    val selectedVehicleType: VehicleType = VehicleType.MOTORCYCLE,
    val plateError: FieldError? = null,
    val modelError: FieldError? = null,
    val colorError: FieldError? = null,
    val yearError: FieldError? = null,
    val saveError: VehicleRegistrationError? = null,
    val isSaveComplete: Boolean = false
)
