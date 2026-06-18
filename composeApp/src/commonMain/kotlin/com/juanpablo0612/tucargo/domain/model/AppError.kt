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
        data object EmptyFile : Validation()
        data object InvalidTrip : Validation()
        data object InvalidPlate : Validation()
        data object KycIncomplete : Validation()
        data object SameOriginDest : Validation()
        data object QuoteOutOfRange : Validation()
        data object NoRoute : Validation()
        data object ServiceUnavailable : Validation()
    }
    sealed class Trip : AppError() {
        data object AlreadyTaken : Trip()
        data object InvalidTransition : Trip()
        data object DriverNotVerified : Trip()
        data object QuoteExpired : Trip()
        data object QuoteAlreadyUsed : Trip()
        data object OfferExpired : Trip()
        data object WalletInsufficient : Trip()
        data class DeliveryCodeInvalid(val remaining: Int) : Trip()
        data object DeliveryCodeLocked : Trip()
        data object InvalidCodeFormat : Trip()
    }
    sealed class Driver : AppError() {
        data object DocNotApproved : Driver()
        data object NoActiveVehicle : Driver()
        data object WalletInsufficient : Driver()
    }
    sealed class Places : AppError() {
        data object AutocompleteUnavailable : Places()
        data object GeocodingUnavailable : Places()
        data object PlaceNotFound : Places()
    }
    data class DataCorruption(override val message: String) : AppError(message)
    data object Network : AppError()
    data class Unknown(override val cause: Throwable?) : AppError(cause?.message, cause)
}
