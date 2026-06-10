package com.juanpablo0612.tucargo.domain.usecase

import com.juanpablo0612.tucargo.data.auth.AuthRepository
import com.juanpablo0612.tucargo.domain.model.User
import com.juanpablo0612.tucargo.domain.model.UserRole

class RegisterUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(
        email: String,
        password: String,
        fullName: String,
        phone: String,
        role: UserRole
    ): Result<User> = authRepository.register(
        email = email.trim().lowercase(),
        password = password,
        fullName = fullName.trim(),
        phone = phone.trim(),
        role = role
    )
}
