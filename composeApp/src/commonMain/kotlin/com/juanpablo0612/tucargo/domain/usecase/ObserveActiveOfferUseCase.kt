package com.juanpablo0612.tucargo.domain.usecase

import com.juanpablo0612.tucargo.data.trip.TripRepository
import com.juanpablo0612.tucargo.domain.model.TripOffer
import kotlinx.coroutines.flow.Flow

class ObserveActiveOfferUseCase(private val tripRepository: TripRepository) {
    operator fun invoke(driverId: String): Flow<TripOffer?> =
        tripRepository.observeActiveOffer(driverId)
}
