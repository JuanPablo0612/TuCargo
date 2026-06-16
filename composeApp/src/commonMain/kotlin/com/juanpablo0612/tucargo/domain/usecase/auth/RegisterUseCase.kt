package com.juanpablo0612.tucargo.domain.usecase.auth

import com.juanpablo0612.tucargo.data.auth.AuthRepository
import com.juanpablo0612.tucargo.domain.model.AppError
import com.juanpablo0612.tucargo.domain.model.User
import com.juanpablo0612.tucargo.domain.model.UserRole

class RegisterUseCase(private val authRepository: AuthRepository) {

    suspend operator fun invoke(
        email: String,
        password: String,
        fullName: String,
        phone: String,
        role: UserRole
    ): Result<User> {
        val normalizedEmail = email.trim().lowercase()
        val normalizedName = fullName.trim()
        val normalizedPhone = phone.trim()

        if (normalizedName.isBlank()) {
            return Result.failure(AppError.Auth.InvalidCredentials)
        }
        if (!isValidEmail(normalizedEmail)) {
            return Result.failure(AppError.Auth.InvalidCredentials)
        }
        if (!isValidPassword(password)) {
            return Result.failure(AppError.Auth.WeakPassword)
        }
        if (!isValidPhone(normalizedPhone)) {
            return Result.failure(AppError.Auth.InvalidCredentials)
        }

        return authRepository.register(
            email = normalizedEmail,
            password = password,
            fullName = normalizedName,
            phone = normalizedPhone,
            role = role
        )
    }

    private fun isValidEmail(email: String): Boolean =
        email.isNotBlank() && Regex("^[^@]+@[^@]+\\.[^@]+$").matches(email)

    // Min 6 chars, at least one letter, at least one digit
    private fun isValidPassword(password: String): Boolean =
        password.length >= 6 &&
            password.any { it.isLetter() } &&
            password.any { it.isDigit() }

    // E.164: + followed by 8–15 digits
    private fun isValidPhone(phone: String): Boolean =
        Regex("^\\+\\d{8,15}$").matches(phone)
}
