package com.juanpablo0612.tucargo.features.auth.resetpassword

sealed interface ResetPasswordError {
    data object NetworkError : ResetPasswordError
    data object UnknownError : ResetPasswordError
}
