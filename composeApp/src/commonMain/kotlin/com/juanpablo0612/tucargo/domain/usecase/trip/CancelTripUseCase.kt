package com.juanpablo0612.tucargo.domain.usecase.trip

import com.juanpablo0612.tucargo.data.trip.TripRepository
import com.juanpablo0612.tucargo.domain.model.AppError
import com.juanpablo0612.tucargo.domain.model.Trip
import com.juanpablo0612.tucargo.domain.model.TripStatus
import com.juanpablo0612.tucargo.domain.trip.canTransitionTo

class CancelTripUseCase(private val tripRepository: TripRepository) {
    suspend operator fun invoke(trip: Trip): Result<Unit> {
        // Mirrors the rules: a client may cancel only before pickup (REQUESTED or OFFERED states).
        if (!trip.status.canTransitionTo(TripStatus.CANCELLED_CLIENT)) {
            return Result.failure(AppError.Trip.InvalidTransition)
        }
        return tripRepository.updateTripStatus(trip.id, from = trip.status, to = TripStatus.CANCELLED_CLIENT)
    }
}
