package com.juanpablo0612.tucargo.domain.usecase

import com.juanpablo0612.tucargo.data.trip.Trip
import com.juanpablo0612.tucargo.data.trip.TripRepository

class GetClientTripsUseCase(private val tripRepository: TripRepository) {
    suspend operator fun invoke(clientId: String, limit: Int = 5): Result<List<Trip>> =
        tripRepository.getClientTrips(clientId, limit)
}
