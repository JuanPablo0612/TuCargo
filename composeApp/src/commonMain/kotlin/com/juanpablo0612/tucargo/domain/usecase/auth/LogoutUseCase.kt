package com.juanpablo0612.tucargo.domain.usecase.auth

import com.juanpablo0612.tucargo.data.auth.AuthRepository

class LogoutUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(): Result<Unit> = authRepository.logout()
}
