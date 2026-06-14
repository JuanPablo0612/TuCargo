package com.juanpablo0612.tucargo.domain.usecase

import com.juanpablo0612.tucargo.data.trip.TripRepository
import com.juanpablo0612.tucargo.domain.model.AppError

class CompleteTripUseCase(private val tripRepository: TripRepository) {
    suspend operator fun invoke(tripId: String, deliveryCode: String): Result<Unit> {
        if (!deliveryCode.matches(Regex("\\d{4}"))) {
            return Result.failure(AppError.Trip.InvalidCodeFormat)
        }
        return tripRepository.completeTrip(tripId, deliveryCode)
    }
}
