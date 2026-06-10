package com.juanpablo0612.tucargo.domain.usecase

import com.juanpablo0612.tucargo.data.auth.AuthRepository

class SendPasswordResetEmailUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String): Result<Unit> =
        authRepository.sendPasswordResetEmail(email.trim().lowercase())
}
