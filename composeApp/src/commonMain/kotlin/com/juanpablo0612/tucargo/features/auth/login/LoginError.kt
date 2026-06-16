package com.juanpablo0612.tucargo.features.auth.login

sealed interface LoginError {
    data object InvalidCredentials : LoginError
    data object NetworkError : LoginError
    data object UnknownError : LoginError
}