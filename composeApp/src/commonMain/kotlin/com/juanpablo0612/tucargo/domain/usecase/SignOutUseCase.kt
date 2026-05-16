package com.juanpablo0612.tucargo.domain.usecase

import com.juanpablo0612.tucargo.data.user.UserRepository

class SignOutUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke() = userRepository.signOut()
}
