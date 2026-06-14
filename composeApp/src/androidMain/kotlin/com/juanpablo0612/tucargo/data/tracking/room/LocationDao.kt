package com.juanpablo0612.tucargo.data.tracking.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: LocationEntity)

    @Query("SELECT * FROM pending_locations WHERE driverId = :driverId ORDER BY capturedAtMs ASC LIMIT :limit")
    suspend fun dequeue(driverId: String, limit: Int): List<LocationEntity>

    @Delete
    suspend fun delete(entities: List<LocationEntity>)

    @Query("SELECT COUNT(*) FROM pending_locations WHERE driverId = :driverId")
    suspend fun count(driverId: String): Int

    @Query("DELETE FROM pending_locations WHERE driverId = :driverId AND id IN (SELECT id FROM pending_locations WHERE driverId = :driverId ORDER BY capturedAtMs ASC LIMIT :excess)")
    suspend fun evictOldest(driverId: String, excess: Int)
}
