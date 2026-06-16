package com.juanpablo0612.tucargo.domain.usecase.quote

import com.juanpablo0612.tucargo.domain.model.AppError
import com.juanpablo0612.tucargo.domain.model.Cop
import com.juanpablo0612.tucargo.domain.model.PricingConfig
import kotlin.math.floor
import kotlin.math.max

class ComputeQuoteUseCase {

    data class Input(val distanceKm: Double, val config: PricingConfig)
    data class Output(val totalPrice: Cop, val commissionFee: Cop)

    operator fun invoke(input: Input): Result<Output> {
        val (distanceKm, config) = input

        if (distanceKm.isNaN() || distanceKm.isInfinite() || distanceKm < 0.0) {
            return Result.failure(AppError.Validation.InvalidTrip)
        }
        if (distanceKm > 60.0) {
            return Result.failure(AppError.Validation.QuoteOutOfRange)
        }

        val distanceCharged = max(distanceKm, 1.0)
        val rawTotal = config.baseFare.amount + (distanceCharged - 1.0) * config.perKmFare.amount
        val totalPrice = roundToNearest100(rawTotal)
        val rawCommission = floor(totalPrice.toDouble() * config.commissionRate / 100.0).toInt()
        val commissionFee = roundToNearest100(rawCommission.toDouble())

        return Result.success(Output(Cop(totalPrice), Cop(commissionFee)))
    }

    private fun roundToNearest100(value: Double): Int =
        (Math.round(value / 100.0) * 100).toInt()
}
