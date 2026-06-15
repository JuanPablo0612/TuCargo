package com.juanpablo0612.tucargo.core.service

import android.content.Context
import android.content.Intent

class AndroidLocationServiceController(private val context: Context) : LocationServiceController {

    override fun startService(driverId: String) {
        val intent = Intent(context, DriverLocationService::class.java).apply {
            action = DriverLocationService.ACTION_START
            putExtra(DriverLocationService.EXTRA_DRIVER_ID, driverId)
        }
        context.startForegroundService(intent)
    }

    override fun stopService() {
        val intent = Intent(context, DriverLocationService::class.java).apply {
            action = DriverLocationService.ACTION_STOP
        }
        context.startService(intent)
    }

    override fun updateTripMode(tripId: String?) {
        val intent = Intent(context, DriverLocationService::class.java).apply {
            action = DriverLocationService.ACTION_UPDATE_TRIP_MODE
            putExtra(DriverLocationService.EXTRA_TRIP_ID, tripId)
        }
        context.startService(intent)
    }
}
