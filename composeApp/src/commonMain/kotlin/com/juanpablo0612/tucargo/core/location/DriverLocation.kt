package com.juanpablo0612.tucargo.core.location

import kotlinx.datetime.Instant

data class DriverLocation(
    val lat: Double,
    val lng: Double,
    val accuracyM: Float,
    val speedKph: Float? = null,
    val headingDeg: Float? = null,
    val capturedAt: Instant,
    val tripId: String? = null
)
