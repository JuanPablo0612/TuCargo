package com.juanpablo0612.tucargo.domain.usecase

import com.juanpablo0612.tucargo.core.location.GeoUtils
import com.juanpablo0612.tucargo.data.common.safeCall
import com.juanpablo0612.tucargo.data.config.ConfigRepository
import com.juanpablo0612.tucargo.domain.model.AppError
import com.juanpablo0612.tucargo.domain.model.TripQuote
import kotlin.math.max

class CalculateTripPriceUseCase(private val configRepository: ConfigRepository) {

    /**
     * Quotes a trip from the operator-managed pricing in config/system.
     * Distance is straight-line (haversine); switching to road distance via a
     * routing API is a known follow-up.
     */
    suspend operator fun invoke(
        originLat: Double,
        originLng: Double,
        destinationLat: Double,
        destinationLng: Double
    ): Result<TripQuote> = safeCall {
        val config = configRepository.getSystemConfig().getOrThrow()
        val distanceKm =
            GeoUtils.haversineDistance(originLat, originLng, destinationLat, destinationLng) / 1000.0
        if (distanceKm <= 0.0) throw AppError.Validation.InvalidTrip

        val priceBase = config.basePrice
        val priceDistance = max(0.0, distanceKm - config.baseKmIncluded) * config.pricePerKm
        val priceTotal = priceBase + priceDistance
        TripQuote(
            distanceKm = distanceKm,
            priceBase = priceBase,
            priceDistance = priceDistance,
            priceTotal = priceTotal,
            commissionFee = priceTotal * config.commissionPercentage
        )
    }
}
