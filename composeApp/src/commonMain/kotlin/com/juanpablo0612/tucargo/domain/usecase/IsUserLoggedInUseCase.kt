package com.juanpablo0612.tucargo.domain.usecase

import com.juanpablo0612.tucargo.data.user.UserRepository

class IsUserLoggedInUseCase(private val userRepository: UserRepository) {
    operator fun invoke(): Boolean = userRepository.isUserLoggedIn()
}
