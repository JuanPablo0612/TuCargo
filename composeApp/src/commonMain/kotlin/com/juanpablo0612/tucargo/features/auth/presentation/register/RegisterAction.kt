package com.juanpablo0612.tucargo.features.auth.presentation.register

sealed interface RegisterUiAction {
    data object OnRegisterClick : RegisterUiAction
    data object OnBackClick : RegisterUiAction
    data class OnEmailChanged(val newEmail: String) : RegisterUiAction // Ejemplo de acción específica
}
