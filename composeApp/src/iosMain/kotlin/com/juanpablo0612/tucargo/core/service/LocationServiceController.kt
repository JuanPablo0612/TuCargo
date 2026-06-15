package com.juanpablo0612.tucargo.core.service

class IosLocationServiceController : LocationServiceController {
    override fun startService(driverId: String) = Unit
    override fun stopService() = Unit
    override fun updateTripMode(tripId: String?) = Unit
}
