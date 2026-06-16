package com.juanpablo0612.tucargo.domain.usecase.user

import com.juanpablo0612.tucargo.data.user.UserRepository

class GetCurrentUserIdUseCase(private val userRepository: UserRepository) {
    operator fun invoke(): String? = userRepository.getCurrentUserId()
}
