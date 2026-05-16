package com.juanpablo0612.tucargo.domain.usecase

import com.juanpablo0612.tucargo.data.user.User
import com.juanpablo0612.tucargo.data.user.UserRepository

class GetCurrentUserUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(): Result<User> = userRepository.getCurrentUser()
}
