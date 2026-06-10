package com.juanpablo0612.tucargo.core.validation

object EmailValidator {
    private val regex = Regex("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,6}$")
    fun isValid(email: String): Boolean = email.isNotBlank() && regex.matches(email.trim())
}

object PhoneValidator {
    private val regex = Regex("^\\+[0-9]{8,15}$")
    fun isValid(phone: String): Boolean = phone.isNotBlank() && regex.matches(phone.trim())
}

object PasswordValidator {
    fun isValid(password: String): Boolean =
        password.length >= 6 && password.any { it.isLetter() } && password.any { it.isDigit() }
}
