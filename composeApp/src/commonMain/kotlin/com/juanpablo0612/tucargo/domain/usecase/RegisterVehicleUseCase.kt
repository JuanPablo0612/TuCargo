package com.juanpablo0612.tucargo.domain.usecase

import com.juanpablo0612.tucargo.data.user.UserRepository
import com.juanpablo0612.tucargo.domain.model.UserVehicle

class RegisterVehicleUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(vehicle: UserVehicle): Result<Unit> =
        userRepository.getCurrentUser().mapCatching { user ->
            userRepository.updateUser(user.copy(vehicle = vehicle)).getOrThrow()
        }
}
