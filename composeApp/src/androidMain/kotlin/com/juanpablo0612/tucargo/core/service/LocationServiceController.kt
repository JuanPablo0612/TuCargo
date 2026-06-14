package com.juanpablo0612.tucargo.core.service

import android.content.Context
import android.content.Intent

actual class LocationServiceController(private val context: Context) {

    actual fun startService(driverId: String) {
        val intent = Intent(context, DriverLocationService::class.java).apply {
            action = DriverLocationService.ACTION_START
            putExtra(DriverLocationService.EXTRA_DRIVER_ID, driverId)
        }
        context.startForegroundService(intent)
    }

    actual fun stopService() {
        val intent = Intent(context, DriverLocationService::class.java).apply {
            action = DriverLocationService.ACTION_STOP
        }
        context.startService(intent)
    }

    actual fun updateTripMode(tripId: String?) {
        val intent = Intent(context, DriverLocationService::class.java).apply {
            action = DriverLocationService.ACTION_UPDATE_TRIP_MODE
            putExtra(DriverLocationService.EXTRA_TRIP_ID, tripId)
        }
        context.startService(intent)
    }
}
