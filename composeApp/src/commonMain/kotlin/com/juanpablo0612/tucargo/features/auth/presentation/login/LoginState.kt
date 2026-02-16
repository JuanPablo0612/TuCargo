package com.juanpablo0612.tucargo.features.auth.presentation.login

data class LoginState(
    val isLoading: Boolean = false,
    val isLoginSuccess: Boolean = false,
    val loginError: LoginError? = null,
    val isEmailValid: Boolean = true,
    val isPasswordValid: Boolean = true,
    val isPasswordVisible: Boolean = false,
)
