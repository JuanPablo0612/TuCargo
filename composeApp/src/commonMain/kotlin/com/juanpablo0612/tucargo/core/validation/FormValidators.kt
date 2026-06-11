package com.juanpablo0612.tucargo.core.validation

object FormValidators {
    fun email(value: String): FieldError? = when {
        value.isBlank() -> FieldError.EmailRequired
        !EmailValidator.isValid(value) -> FieldError.EmailInvalid
        else -> null
    }

    fun password(value: String): FieldError? = when {
        value.isBlank() -> FieldError.PasswordRequired
        value.length < 6 -> FieldError.PasswordTooShort
        !PasswordValidator.isValid(value) -> FieldError.PasswordWeak
        else -> null
    }

    fun phone(value: String): FieldError? = when {
        value.isBlank() -> FieldError.PhoneRequired
        !PhoneValidator.isValid(value) -> FieldError.PhoneInvalid
        else -> null
    }

    fun required(value: String, error: FieldError): FieldError? =
        if (value.isBlank()) error else null

    fun vehiclePlate(value: String): FieldError? = when {
        value.isBlank() -> FieldError.PlateRequired
        value.length < 5 -> FieldError.PlateInvalid
        else -> null
    }

    fun vehicleYear(value: String): FieldError? {
        if (value.isBlank()) return FieldError.VehicleYearRequired
        val year = value.toIntOrNull() ?: return FieldError.VehicleYearInvalid
        return if (year < 1990 || year > 2030) FieldError.VehicleYearInvalid else null
    }
}
