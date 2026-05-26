package com.juanpablo0612.tucargo.domain.usecase

import com.juanpablo0612.tucargo.data.auth.AuthRepository
import com.juanpablo0612.tucargo.domain.model.AuthError

class SendPasswordResetEmailUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String): Result<Unit> {
        if (email.isBlank()) {
            return Result.failure(AuthError.Unknown("Email required"))
        }
        return authRepository.sendPasswordResetEmail(email.trim().lowercase())
    }
}
