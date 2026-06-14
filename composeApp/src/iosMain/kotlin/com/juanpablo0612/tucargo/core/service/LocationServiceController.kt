package com.juanpablo0612.tucargo.core.service

actual class LocationServiceController {
    actual fun startService(driverId: String) = Unit
    actual fun stopService() = Unit
    actual fun updateTripMode(tripId: String?) = Unit
}
