package com.juanpablo0612.tucargo.core.service

expect class LocationServiceController {
    fun startService(driverId: String)
    fun stopService()
    fun updateTripMode(tripId: String?)
}
