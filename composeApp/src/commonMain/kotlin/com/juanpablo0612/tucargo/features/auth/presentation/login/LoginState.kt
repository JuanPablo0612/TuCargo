package com.juanpablo0612.tucargo.features.auth.presentation.login

import androidx.compose.runtime.Immutable

@Immutable
data class LoginState(
    val isLoading: Boolean = false,
    val isEmailValid: Boolean = true,
    val isPasswordValid: Boolean = true,
    val loginError: LoginError? = null
)