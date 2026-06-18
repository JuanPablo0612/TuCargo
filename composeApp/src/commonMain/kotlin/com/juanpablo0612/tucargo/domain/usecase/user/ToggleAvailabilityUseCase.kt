package com.juanpablo0612.tucargo.domain.usecase.user

import com.juanpablo0612.tucargo.data.config.ConfigRepository
import com.juanpablo0612.tucargo.data.tracking.TrackingRepository
import com.juanpablo0612.tucargo.data.user.UserRepository
import com.juanpablo0612.tucargo.domain.model.AppError

class ToggleAvailabilityUseCase(
    private val userRepository: UserRepository,
    private val configRepository: ConfigRepository,
    private val trackingRepository: TrackingRepository
) {
    suspend operator fun invoke(userId: String, goOnline: Boolean): Result<Unit> {
        // Going offline is always allowed. Drop the live-location node now so the
        // scheduled sweep doesn't have to — and so dispatch won't see a stale fix.
        if (!goOnline) {
            trackingRepository.clearLocation(userId)
            return userRepository.updateDriverStatus(userId, false)
        }

        val user = userRepository.getCurrentUser().getOrElse { return Result.failure(it) }

        if (!user.isVerified) return Result.failure(AppError.Driver.DocNotApproved)
        if (user.vehicle == null) return Result.failure(AppError.Driver.NoActiveVehicle)

        val config = configRepository.getSystemConfig().getOrElse { return Result.failure(it) }
        if (user.walletBalance < config.minWalletBalance) {
            return Result.failure(AppError.Driver.WalletInsufficient)
        }

        return userRepository.updateDriverStatus(userId, true)
    }
}
