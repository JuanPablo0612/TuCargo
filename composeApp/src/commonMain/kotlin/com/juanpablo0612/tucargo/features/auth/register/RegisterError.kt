package com.juanpablo0612.tucargo.features.auth.register

sealed interface RegisterError {
    data object EmailAlreadyInUse : RegisterError
    data object WeakPassword : RegisterError
    data object NetworkError : RegisterError
    data object UnknownError : RegisterError
}
