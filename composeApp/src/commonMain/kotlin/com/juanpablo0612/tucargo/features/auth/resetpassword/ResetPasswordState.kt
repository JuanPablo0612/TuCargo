package com.juanpablo0612.tucargo.features.auth.resetpassword

import androidx.compose.runtime.Immutable
import com.juanpablo0612.tucargo.core.validation.FieldError

@Immutable
data class ResetPasswordState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val emailError: FieldError? = null,
    val authError: ResetPasswordError? = null
)
