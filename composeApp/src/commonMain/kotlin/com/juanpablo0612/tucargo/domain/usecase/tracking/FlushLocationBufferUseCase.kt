package com.juanpablo0612.tucargo.domain.usecase.tracking

import com.juanpablo0612.tucargo.core.logging.logError
import com.juanpablo0612.tucargo.data.tracking.LocationBuffer
import com.juanpablo0612.tucargo.data.tracking.TrackingRepository

private const val FLUSH_BATCH_SIZE = 200

class FlushLocationBufferUseCase(
    private val trackingRepository: TrackingRepository,
    private val locationBuffer: LocationBuffer
) {
    suspend operator fun invoke(driverId: String): Result<Unit> {
        val points = locationBuffer.dequeue(driverId, FLUSH_BATCH_SIZE)
        if (points.isEmpty()) return Result.success(Unit)
        return trackingRepository.writeLocationBatch(driverId, points).onFailure { e ->
            logError("FlushLocationBufferUseCase", "Batch flush failed: ${e.message}")
        }
    }
}
