package com.juanpablo0612.tucargo.domain.usecase

import com.juanpablo0612.tucargo.data.auth.AuthRepository
import com.juanpablo0612.tucargo.data.user.User
import com.juanpablo0612.tucargo.domain.model.AuthError

class LoginUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(AuthError.InvalidCredentials)
        }
        val normalizedEmail = email.trim().lowercase()
        return authRepository.login(normalizedEmail, password)
    }
}
