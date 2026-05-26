package com.juanpablo0612.tucargo.domain.model

sealed class AuthError : Exception() {
    data object InvalidCredentials : AuthError()
    data object EmailAlreadyInUse : AuthError()
    data object WeakPassword : AuthError()
    data object NetworkError : AuthError()
    data class Unknown(override val message: String?) : AuthError()
}
