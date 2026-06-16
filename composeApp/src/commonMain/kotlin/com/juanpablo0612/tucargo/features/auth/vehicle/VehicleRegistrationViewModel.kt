package com.juanpablo0612.tucargo.features.auth.vehicle

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanpablo0612.tucargo.core.validation.FieldError
import com.juanpablo0612.tucargo.core.validation.FormValidators
import com.juanpablo0612.tucargo.domain.model.UserVehicle
import com.juanpablo0612.tucargo.domain.usecase.user.RegisterVehicleUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VehicleRegistrationViewModel(
    private val registerVehicleUseCase: RegisterVehicleUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(VehicleRegistrationState())
    val uiState = _uiState.asStateFlow()

    val plateState = TextFieldState()
    val modelState = TextFieldState()
    val colorState = TextFieldState()
    val yearState = TextFieldState()

    fun onAction(action: VehicleRegistrationAction) {
        when (action) {
            is VehicleRegistrationAction.SelectVehicleType ->
                _uiState.update { it.copy(selectedVehicleType = action.type) }
            VehicleRegistrationAction.Submit -> onSubmit()
            VehicleRegistrationAction.OnBackClick -> {}
        }
    }

    fun onNavigated() {
        _uiState.update { it.copy(isSaveComplete = false) }
    }

    private fun onSubmit() {
        val plate = plateState.text.toString().trim().uppercase()
        val model = modelState.text.toString().trim()
        val color = colorState.text.toString().trim()
        val year = yearState.text.toString().trim()

        val plateError = FormValidators.vehiclePlate(plate)
        val modelError = FormValidators.required(model, FieldError.VehicleModelRequired)
        val colorError = FormValidators.required(color, FieldError.VehicleColorRequired)
        val yearError = FormValidators.vehicleYear(year)

        _uiState.update {
            it.copy(
                plateError = plateError,
                modelError = modelError,
                colorError = colorError,
                yearError = yearError,
                saveError = null
            )
        }

        if (plateError != null || modelError != null || colorError != null || yearError != null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val vehicle = UserVehicle(
                plate = plate,
                model = model,
                color = color,
                year = year.toInt(),
                type = _uiState.value.selectedVehicleType
            )
            registerVehicleUseCase(vehicle).fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, isSaveComplete = true) }
                },
                onFailure = {
                    _uiState.update {
                        it.copy(isLoading = false, saveError = VehicleRegistrationError.SaveError)
                    }
                }
            )
        }
    }
}
