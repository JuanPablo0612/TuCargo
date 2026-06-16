package com.juanpablo0612.tucargo.domain.usecase.trip

import com.juanpablo0612.tucargo.data.trip.TripRepository
import com.juanpablo0612.tucargo.domain.model.Trip
import kotlinx.coroutines.flow.Flow

class ObserveTripUseCase(private val tripRepository: TripRepository) {
    operator fun invoke(tripId: String): Flow<Trip> = tripRepository.observeTrip(tripId)
}
