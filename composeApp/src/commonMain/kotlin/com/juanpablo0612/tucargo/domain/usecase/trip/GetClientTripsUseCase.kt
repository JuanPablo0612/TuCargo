package com.juanpablo0612.tucargo.domain.usecase.trip

import com.juanpablo0612.tucargo.data.trip.TripRepository
import com.juanpablo0612.tucargo.domain.model.Trip

class GetClientTripsUseCase(private val tripRepository: TripRepository) {
    suspend operator fun invoke(clientId: String, limit: Int = 5): Result<List<Trip>> =
        tripRepository.getClientTrips(clientId, limit)
}
