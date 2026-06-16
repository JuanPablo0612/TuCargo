package com.juanpablo0612.tucargo.domain.usecase.admin

import com.juanpablo0612.tucargo.data.user.UserRepository
import com.juanpablo0612.tucargo.domain.model.User

class GetPendingDriversUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(): Result<List<User>> = userRepository.getPendingDrivers()
}
