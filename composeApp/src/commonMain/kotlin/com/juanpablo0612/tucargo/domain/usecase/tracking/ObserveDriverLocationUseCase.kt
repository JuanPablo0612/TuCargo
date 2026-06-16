package com.juanpablo0612.tucargo.domain.usecase.tracking

import com.juanpablo0612.tucargo.core.location.DriverLocation
import com.juanpablo0612.tucargo.data.tracking.TrackingRepository
import kotlinx.coroutines.flow.Flow

class ObserveDriverLocationUseCase(
    private val trackingRepository: TrackingRepository
) {
    operator fun invoke(driverId: String): Flow<DriverLocation> =
        trackingRepository.observeDriverLocation(driverId)
}
