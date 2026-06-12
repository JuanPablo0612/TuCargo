package com.juanpablo0612.tucargo.domain.usecase

import com.juanpablo0612.tucargo.data.trip.TripRepository
import com.juanpablo0612.tucargo.domain.model.AppError
import com.juanpablo0612.tucargo.domain.model.Trip

class CreateTripUseCase(private val tripRepository: TripRepository) {
    suspend operator fun invoke(trip: Trip): Result<String> {
        val isValid = trip.priceTotal > 0.0 &&
            trip.distanceKm > 0.0 &&
            trip.origin.address.isNotBlank() &&
            trip.destination.address.isNotBlank() &&
            trip.cargoDescription.isNotBlank() &&
            !(trip.origin.lat == trip.destination.lat && trip.origin.lng == trip.destination.lng)
        if (!isValid) return Result.failure(AppError.Validation.InvalidTrip)
        return tripRepository.createTrip(trip)
    }
}
