package com.juanpablo0612.tucargo.domain.usecase.tracking

import com.juanpablo0612.tucargo.core.location.DriverLocation
import com.juanpablo0612.tucargo.core.location.GeoUtils
import com.juanpablo0612.tucargo.core.logging.logError
import com.juanpablo0612.tucargo.data.tracking.LocationBuffer
import com.juanpablo0612.tucargo.data.tracking.TrackingRepository
import kotlin.time.Clock

private const val MAX_FUTURE_OFFSET_MS = 60_000L
private const val MAX_STALENESS_MS = 5 * 60_000L

// Skip the live RTDB write when the driver has barely moved, but still emit a
// periodic heartbeat so the client map stays fresh while stopped at a light.
private const val MOVEMENT_THRESHOLD_M = 25.0
private const val LIVE_HEARTBEAT_MS = 15_000L

// The dispatch feed (users.last_lat/lng) only needs to be recent enough for
// nearest-driver matching, so it is written far less often than the live node.
private const val DISPATCH_THROTTLE_MS = 45_000L

class SendLocationUseCase(
    private val trackingRepository: TrackingRepository,
    private val locationBuffer: LocationBuffer
) {
    private var lastSentLat: Double? = null
    private var lastSentLng: Double? = null
    private var lastLiveWriteMs = 0L
    private var lastDispatchWriteMs = 0L

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

        // Throttled dispatch feed — time-based only, so a stationary online
        // driver stays matchable. Best-effort: never fail tracking over it.
        if (nowMs - lastDispatchWriteMs >= DISPATCH_THROTTLE_MS) {
            trackingRepository.updateDispatchLocation(driverId, location.lat, location.lng)
                .onSuccess { lastDispatchWriteMs = nowMs }
                .onFailure { logError("SendLocationUseCase", "Dispatch location write failed: ${it.message}") }
        }

        if (!shouldWriteLive(location, nowMs)) {
            return Result.success(Unit)
        }

        return trackingRepository.writeLocation(driverId, location)
            .onSuccess {
                lastSentLat = location.lat
                lastSentLng = location.lng
                lastLiveWriteMs = nowMs
            }
            .onFailure { locationBuffer.enqueue(driverId, location) }
    }

    private fun shouldWriteLive(location: DriverLocation, nowMs: Long): Boolean {
        val prevLat = lastSentLat
        val prevLng = lastSentLng
        if (prevLat == null || prevLng == null) return true
        if (nowMs - lastLiveWriteMs >= LIVE_HEARTBEAT_MS) return true
        return GeoUtils.haversineDistance(prevLat, prevLng, location.lat, location.lng) >= MOVEMENT_THRESHOLD_M
    }

    private fun isValidCoordinate(location: DriverLocation): Boolean {
        if (location.lat < -90.0 || location.lat > 90.0) return false
        if (location.lng < -180.0 || location.lng > 180.0) return false
        if (location.lat == 0.0 && location.lng == 0.0) return false
        return true
    }
}
