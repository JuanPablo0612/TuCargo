package com.juanpablo0612.tucargo.domain.usecase.trip

import com.juanpablo0612.tucargo.data.trip.TripRepository
import com.juanpablo0612.tucargo.domain.model.AppError
import com.juanpablo0612.tucargo.domain.model.Trip
import com.juanpablo0612.tucargo.domain.model.TripStatus
import com.juanpablo0612.tucargo.domain.trip.canTransitionTo

class AdvanceTripStatusUseCase(private val tripRepository: TripRepository) {
    suspend operator fun invoke(trip: Trip, to: TripStatus): Result<Unit> {
        if (!trip.status.canTransitionTo(to)) {
            return Result.failure(AppError.Trip.InvalidTransition)
        }
        return tripRepository.updateTripStatus(trip.id, from = trip.status, to = to)
    }
}
