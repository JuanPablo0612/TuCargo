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
}
