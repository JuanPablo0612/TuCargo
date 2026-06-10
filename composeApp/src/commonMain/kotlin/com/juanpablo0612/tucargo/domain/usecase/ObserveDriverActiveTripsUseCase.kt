package com.juanpablo0612.tucargo.domain.usecase

import com.juanpablo0612.tucargo.data.trip.TripRepository
import com.juanpablo0612.tucargo.domain.model.Trip
import kotlinx.coroutines.flow.Flow

class ObserveDriverActiveTripsUseCase(private val tripRepository: TripRepository) {
    operator fun invoke(driverId: String): Flow<List<Trip>> =
        tripRepository.observeDriverActiveTrips(driverId)
}
