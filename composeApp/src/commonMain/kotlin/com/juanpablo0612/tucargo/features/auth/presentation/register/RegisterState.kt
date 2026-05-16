package com.juanpablo0612.tucargo.features.auth.presentation.register

import androidx.compose.runtime.Immutable

@Immutable
data class RegisterState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
