package com.juanpablo0612.tucargo.domain.usecase.user

import com.juanpablo0612.tucargo.data.user.UserRepository

class UpdateDriverStatusUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(userId: String, isOnline: Boolean): Result<Unit> =
        userRepository.updateDriverStatus(userId, isOnline)
}
