package com.juanpablo0612.tucargo.data.tracking

import com.juanpablo0612.tucargo.core.location.DriverLocation
import com.juanpablo0612.tucargo.data.tracking.room.LocationDao
import com.juanpablo0612.tucargo.data.tracking.room.LocationEntity
import kotlinx.datetime.Instant

private const val MAX_BUFFER_SIZE = 900

class RoomLocationBuffer(private val dao: LocationDao) : LocationBuffer {

    override suspend fun enqueue(driverId: String, location: DriverLocation) {
        val current = dao.count(driverId)
        if (current >= MAX_BUFFER_SIZE) {
            dao.evictOldest(driverId, current - MAX_BUFFER_SIZE + 1)
        }
        dao.insert(location.toEntity(driverId))
    }

    override suspend fun dequeue(driverId: String, limit: Int): List<DriverLocation> {
        val entities = dao.dequeue(driverId, limit)
        dao.delete(entities)
        return entities.map { it.toDomain() }
    }

    override suspend fun size(driverId: String): Int = dao.count(driverId)

    private fun DriverLocation.toEntity(driverId: String) = LocationEntity(
        driverId = driverId,
        lat = lat,
        lng = lng,
        accuracyM = accuracyM,
        speedKph = speedKph,
        headingDeg = headingDeg,
        capturedAtMs = capturedAt.toEpochMilliseconds(),
        tripId = tripId
    )

    private fun LocationEntity.toDomain() = DriverLocation(
        lat = lat,
        lng = lng,
        accuracyM = accuracyM,
        speedKph = speedKph,
        headingDeg = headingDeg,
        capturedAt = Instant.fromEpochMilliseconds(capturedAtMs),
        tripId = tripId
    )
}
