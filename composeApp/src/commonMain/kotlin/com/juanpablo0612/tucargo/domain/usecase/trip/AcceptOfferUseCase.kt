package com.juanpablo0612.tucargo.domain.usecase.trip

import com.juanpablo0612.tucargo.data.trip.TripRepository

class AcceptOfferUseCase(private val tripRepository: TripRepository) {
    suspend operator fun invoke(tripId: String, offerId: String): Result<Unit> =
        tripRepository.acceptOffer(tripId, offerId)
}
