package com.juanpablo0612.tucargo.domain.usecase

import com.juanpablo0612.tucargo.data.trip.TripRepository
import com.juanpablo0612.tucargo.domain.model.Trip

class GetDriverTripsUseCase(private val tripRepository: TripRepository) {
    suspend operator fun invoke(driverId: String, limit: Int = 20): Result<List<Trip>> =
        tripRepository.getDriverTrips(driverId, limit)
}
