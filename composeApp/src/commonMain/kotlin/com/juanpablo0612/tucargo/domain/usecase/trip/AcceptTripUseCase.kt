package com.juanpablo0612.tucargo.domain.usecase.trip

import com.juanpablo0612.tucargo.data.common.safeCall
import com.juanpablo0612.tucargo.data.trip.TripRepository
import com.juanpablo0612.tucargo.data.user.UserRepository
import com.juanpablo0612.tucargo.domain.model.AppError
import com.juanpablo0612.tucargo.domain.model.UserRole

class AcceptTripUseCase(
    private val tripRepository: TripRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(tripId: String): Result<Unit> = safeCall {
        val driver = userRepository.getCurrentUser().getOrThrow()
        if (driver.role != UserRole.DRIVER || !driver.isVerified) {
            throw AppError.Trip.DriverNotVerified
        }
        tripRepository.acceptTrip(
            tripId = tripId,
            driverId = driver.id,
            driverName = driver.fullName,
            driverPlate = driver.vehicle?.plate.orEmpty()
        ).getOrThrow()
    }
}
