package com.juanpablo0612.tucargo.data.tracking

import com.juanpablo0612.tucargo.core.location.DriverLocation
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val MAX_BUFFER_SIZE = 900

class InMemoryLocationBuffer : LocationBuffer {
    private val mutex = Mutex()
    private val buffer = ArrayDeque<DriverLocation>(MAX_BUFFER_SIZE)

    override suspend fun enqueue(driverId: String, location: DriverLocation) = mutex.withLock {
        if (buffer.size >= MAX_BUFFER_SIZE) buffer.removeFirst()
        buffer.addLast(location)
    }

    override suspend fun dequeue(driverId: String, limit: Int): List<DriverLocation> =
        mutex.withLock {
            val result = buffer.take(limit)
            repeat(result.size) { buffer.removeFirst() }
            result
        }

    override suspend fun size(driverId: String): Int = mutex.withLock { buffer.size }
}
