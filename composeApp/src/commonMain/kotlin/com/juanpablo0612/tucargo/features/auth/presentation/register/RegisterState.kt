package com.juanpablo0612.tucargo.features.auth.presentation.register

import androidx.compose.runtime.Immutable
import com.juanpablo0612.tucargo.core.ui.event.UiEvent

@Immutable
data class RegisterState(
    val isLoading: Boolean = false,
    val error: RegisterError? = null,
    val navigationEvent: UiEvent<String>? = null,
    val isNameError: Boolean = false,
    val isEmailError: Boolean = false,
    val isPhoneError: Boolean = false,
    val isPasswordError: Boolean = false,
)
