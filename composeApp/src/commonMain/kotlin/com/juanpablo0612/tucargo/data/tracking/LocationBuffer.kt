package com.juanpablo0612.tucargo.data.tracking

import com.juanpablo0612.tucargo.core.location.DriverLocation

interface LocationBuffer {
    suspend fun enqueue(driverId: String, location: DriverLocation)
    suspend fun dequeue(driverId: String, limit: Int): List<DriverLocation>
    suspend fun size(driverId: String): Int
}
