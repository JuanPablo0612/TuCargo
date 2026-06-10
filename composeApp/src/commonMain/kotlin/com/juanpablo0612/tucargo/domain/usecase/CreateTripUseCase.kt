package com.juanpablo0612.tucargo.domain.usecase

import com.juanpablo0612.tucargo.data.trip.TripRepository
import com.juanpablo0612.tucargo.domain.model.Trip

class CreateTripUseCase(private val tripRepository: TripRepository) {
    suspend operator fun invoke(trip: Trip): Result<String> =
        tripRepository.createTrip(trip)
}
