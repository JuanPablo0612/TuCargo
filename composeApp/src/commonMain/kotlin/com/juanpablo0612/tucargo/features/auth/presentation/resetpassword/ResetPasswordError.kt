package com.juanpablo0612.tucargo.features.auth.presentation.resetpassword

sealed interface ResetPasswordError {
    data object NetworkError : ResetPasswordError
    data object UnknownError : ResetPasswordError
}
