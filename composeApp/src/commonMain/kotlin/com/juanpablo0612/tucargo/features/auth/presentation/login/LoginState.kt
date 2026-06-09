package com.juanpablo0612.tucargo.features.auth.presentation.login

import androidx.compose.runtime.Immutable
import com.juanpablo0612.tucargo.core.validation.FieldError

@Immutable
data class LoginState(
    val isLoading: Boolean = false,
    val emailError: FieldError? = null,
    val passwordError: FieldError? = null,
    val authError: LoginError? = null,
    val successRole: String? = null
)
