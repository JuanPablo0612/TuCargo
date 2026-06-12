package com.juanpablo0612.tucargo.domain.model

sealed class AppError(message: String? = null, cause: Throwable? = null) : Exception(message, cause) {
    sealed class Auth : AppError() {
        data object InvalidCredentials : Auth()
        data object EmailAlreadyInUse : Auth()
        data object WeakPassword : Auth()
        data object NotAuthenticated : Auth()
    }
    sealed class Validation : AppError() {
        data object FileTooLarge : Validation()
    }
    data object Network : AppError()
    data class Unknown(override val cause: Throwable?) : AppError(cause?.message, cause)
}
