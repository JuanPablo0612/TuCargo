package com.juanpablo0612.tucargo.data.tracking.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_locations")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val driverId: String,
    val lat: Double,
    val lng: Double,
    val accuracyM: Float,
    val speedKph: Float?,
    val headingDeg: Float?,
    val capturedAtMs: Long,
    val tripId: String?
)
