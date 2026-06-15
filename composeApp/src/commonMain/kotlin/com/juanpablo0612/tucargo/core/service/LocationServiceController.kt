package com.juanpablo0612.tucargo.core.service

interface LocationServiceController {
    fun startService(driverId: String)
    fun stopService()
    fun updateTripMode(tripId: String?)
}
