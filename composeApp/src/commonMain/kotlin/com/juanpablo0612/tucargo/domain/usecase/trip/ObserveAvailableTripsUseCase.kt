package com.juanpablo0612.tucargo.domain.usecase.trip

import com.juanpablo0612.tucargo.data.trip.TripRepository
import com.juanpablo0612.tucargo.domain.model.Trip
import kotlinx.coroutines.flow.Flow

class ObserveAvailableTripsUseCase(private val tripRepository: TripRepository) {
    operator fun invoke(limit: Int = 20): Flow<List<Trip>> =
        tripRepository.observeAvailableTrips(limit)
}
