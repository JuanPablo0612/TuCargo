package com.juanpablo0612.tucargo.features.auth.presentation.register

import androidx.compose.runtime.Immutable
import com.juanpablo0612.tucargo.core.validation.FieldError

@Immutable
data class RegisterState(
    val isLoading: Boolean = false,
    val nameError: FieldError? = null,
    val emailError: FieldError? = null,
    val phoneError: FieldError? = null,
    val passwordError: FieldError? = null,
    val authError: RegisterError? = null,
    val successRole: String? = null
)
