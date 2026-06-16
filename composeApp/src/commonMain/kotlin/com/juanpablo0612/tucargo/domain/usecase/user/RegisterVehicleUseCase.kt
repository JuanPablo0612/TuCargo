package com.juanpablo0612.tucargo.domain.usecase.user

import com.juanpablo0612.tucargo.data.user.UserRepository
import com.juanpablo0612.tucargo.domain.model.AppError
import com.juanpablo0612.tucargo.domain.model.UserVehicle

class RegisterVehicleUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(vehicle: UserVehicle): Result<Unit> {
        val cleanPlate = vehicle.plate.trim().uppercase()
        if (cleanPlate.length < 5) {
            return Result.failure(AppError.Validation.InvalidPlate)
        }

        return userRepository.getCurrentUser().mapCatching { user ->
            userRepository.updateUser(
                user.copy(vehicle = vehicle.copy(plate = cleanPlate))
            ).getOrThrow()
        }
    }
}
