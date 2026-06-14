package com.juanpablo0612.tucargo

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.juanpablo0612.tucargo.core.service.DriverLocationService
import com.juanpablo0612.tucargo.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class TuCargoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        initKoin {
            androidContext(this@TuCargoApplication)
            androidLogger()
        }
    }

    private fun createNotificationChannels() {
        val channel = NotificationChannel(
            DriverLocationService.CHANNEL_ID,
            "Ubicación del conductor",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Canal para el seguimiento de ubicación del conductor"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}