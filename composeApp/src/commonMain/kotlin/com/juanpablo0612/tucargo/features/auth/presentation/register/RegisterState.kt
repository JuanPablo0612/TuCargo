package com.juanpablo0612.tucargo.features.auth.presentation.register
data class RegisterState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)
