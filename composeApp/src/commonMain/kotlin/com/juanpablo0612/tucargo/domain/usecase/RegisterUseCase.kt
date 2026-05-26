package com.juanpablo0612.tucargo.domain.usecase

import com.juanpablo0612.tucargo.data.auth.AuthRepository
import com.juanpablo0612.tucargo.data.user.User
import com.juanpablo0612.tucargo.domain.model.AuthError

class RegisterUseCase(private val authRepository: AuthRepository) {

    private val emailRegex = Regex("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,6}$")
    private val phoneRegex = Regex("^\\+[0-9]{8,15}$")

    suspend operator fun invoke(
        email: String,
        password: String,
        fullName: String,
        phone: String,
        role: String
    ): Result<User> {
        if (fullName.isBlank()) {
            return Result.failure(AuthError.Unknown("Full name required"))
        }
        if (!emailRegex.matches(email.trim())) {
            return Result.failure(AuthError.InvalidCredentials)
        }
        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }
        if (password.length < 6 || !hasLetter || !hasDigit) {
            return Result.failure(AuthError.WeakPassword)
        }
        if (!phoneRegex.matches(phone.trim())) {
            return Result.failure(AuthError.Unknown("Invalid phone format"))
        }
        val normalizedEmail = email.trim().lowercase()
        return authRepository.register(normalizedEmail, password, fullName, phone.trim(), role)
    }
}
