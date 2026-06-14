package com.juanpablo0612.tucargo.domain.usecase

import com.juanpablo0612.tucargo.data.trip.TripRepository
import com.juanpablo0612.tucargo.domain.model.AppError

class RequestTripUseCase(private val tripRepository: TripRepository) {

    suspend operator fun invoke(
        quoteId: String,
        cargoDescription: String,
        weightConfirmed: Boolean
    ): Result<Pair<String, String>> {
        if (!weightConfirmed) {
            return Result.failure(AppError.Validation.InvalidTrip)
        }
        if (cargoDescription.isBlank()) {
            return Result.failure(AppError.Validation.InvalidTrip)
        }
        return tripRepository.requestTrip(
            quoteId = quoteId,
            cargoDescription = cargoDescription,
            weightConfirmed = weightConfirmed
        )
    }
}
