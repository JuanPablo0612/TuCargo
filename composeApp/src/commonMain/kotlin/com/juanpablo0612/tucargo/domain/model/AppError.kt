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
        data object InvalidTrip : Validation()
    }
    sealed class Trip : AppError() {
        data object AlreadyTaken : Trip()
        data object InvalidTransition : Trip()
        data object DriverNotVerified : Trip()
    }
    data class DataCorruption(override val message: String) : AppError(message)
    data object Network : AppError()
    data class Unknown(override val cause: Throwable?) : AppError(cause?.message, cause)
}
