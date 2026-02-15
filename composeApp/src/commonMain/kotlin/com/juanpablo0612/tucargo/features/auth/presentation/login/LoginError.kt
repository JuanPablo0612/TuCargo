package com.juanpablo0612.tucargo.features.auth.presentation.login

sealed interface LoginError {
    data object InvalidCredentials : LoginError
    data object NetworkError : LoginError
    data object UnknownError : LoginError
}