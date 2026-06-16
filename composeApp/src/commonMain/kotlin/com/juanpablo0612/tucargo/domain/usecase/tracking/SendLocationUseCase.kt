package com.juanpablo0612.tucargo.domain.usecase.tracking

import com.juanpablo0612.tucargo.core.location.DriverLocation
import com.juanpablo0612.tucargo.core.logging.logError
import com.juanpablo0612.tucargo.data.tracking.LocationBuffer
import com.juanpablo0612.tucargo.data.tracking.TrackingRepository
import kotlin.time.Clock

private const val MAX_FUTURE_OFFSET_MS = 60_000L
private const val MAX_STALENESS_MS = 5 * 60_000L

class SendLocationUseCase(
    private val trackingRepository: TrackingRepository,
    private val locationBuffer: LocationBuffer
) {
    suspend operator fun invoke(driverId: String, location: DriverLocation): Result<Unit> {
        if (!isValidCoordinate(location)) {
            logError("SendLocationUseCase", "Rejected invalid coordinate: (${location.lat}, ${location.lng})")
            return Result.failure(IllegalArgumentException("Invalid coordinate"))
        }

        val nowMs = Clock.System.now().toEpochMilliseconds()
        val capturedMs = location.capturedAt.toEpochMilliseconds()
        if (capturedMs > nowMs + MAX_FUTURE_OFFSET_MS) {
            logError("SendLocationUseCase", "Rejected future timestamp: capturedAt=$capturedMs now=$nowMs")
            return Result.failure(IllegalArgumentException("Timestamp too far in future"))
        }
        if (capturedMs < nowMs - MAX_STALENESS_MS) {
            logError("SendLocationUseCase", "Rejected stale timestamp: capturedAt=$capturedMs now=$nowMs")
            return Result.failure(IllegalArgumentException("Timestamp too old"))
        }

        return trackingRepository.writeLocation(driverId, location).onFailure {
            locationBuffer.enqueue(driverId, location)
        }
    }

    private fun isValidCoordinate(location: DriverLocation): Boolean {
        if (location.lat < -90.0 || location.lat > 90.0) return false
        if (location.lng < -180.0 || location.lng > 180.0) return false
        if (location.lat == 0.0 && location.lng == 0.0) return false
        return true
    }
}
