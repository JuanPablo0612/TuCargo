package com.juanpablo0612.tucargo.core.validation

sealed interface FieldError {
    data object EmailRequired : FieldError
    data object EmailInvalid : FieldError
    data object PasswordRequired : FieldError
    data object PasswordTooShort : FieldError
    data object PasswordWeak : FieldError
    data object NameRequired : FieldError
    data object PhoneRequired : FieldError
    data object PhoneInvalid : FieldError
    data object IdFrontRequired : FieldError
    data object IdBackRequired : FieldError
    data object PlateRequired : FieldError
    data object PlateInvalid : FieldError
    data object VehicleModelRequired : FieldError
    data object VehicleColorRequired : FieldError
    data object VehicleYearRequired : FieldError
    data object VehicleYearInvalid : FieldError
    data object AddressRequired : FieldError
    data object CargoDescriptionRequired : FieldError
    data class DocumentRequired(val typeName: String) : FieldError
}
