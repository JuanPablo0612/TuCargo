package com.juanpablo0612.tucargo.features.auth.presentation.register

sealed interface RegisterError {
    data object FieldsRequired : RegisterError
    data object PasswordTooShort : RegisterError
    data object WeakPassword : RegisterError
    data object InvalidEmailFormat : RegisterError
    data object InvalidPhoneFormat : RegisterError
    data object UserAlreadyExists : RegisterError
    data object NetworkError : RegisterError
    data object UnknownError : RegisterError
}
