package com.juanpablo0612.tucargo.features.auth.presentation.resetpassword

import androidx.compose.runtime.Immutable

@Immutable
data class ResetPasswordState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: ResetPasswordError? = null
)

sealed interface ResetPasswordError {
    data object EmailRequired : ResetPasswordError
    data object NetworkError : ResetPasswordError
    data object UnknownError : ResetPasswordError
}
