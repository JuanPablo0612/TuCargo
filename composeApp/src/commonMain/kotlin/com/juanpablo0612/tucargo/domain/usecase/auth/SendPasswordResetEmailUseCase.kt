package com.juanpablo0612.tucargo.domain.usecase.auth

import com.juanpablo0612.tucargo.data.auth.AuthRepository
import com.juanpablo0612.tucargo.domain.model.AppError

class SendPasswordResetEmailUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String): Result<Unit> {
        val normalized = email.trim().lowercase()
        if (normalized.isBlank()) {
            return Result.failure(AppError.Auth.InvalidCredentials)
        }
        return authRepository.sendPasswordResetEmail(normalized)
    }
}
