package com.juanpablo0612.tucargo.core.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.juanpablo0612.tucargo.core.location.DriverLocation
import com.juanpablo0612.tucargo.domain.usecase.tracking.FlushLocationBufferUseCase
import com.juanpablo0612.tucargo.domain.usecase.tracking.SendLocationUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.time.Instant
import org.jetbrains.compose.resources.getString
import org.koin.android.ext.android.inject
import tucargo.composeapp.generated.resources.Res
import tucargo.composeapp.generated.resources.service_channel_description
import tucargo.composeapp.generated.resources.service_channel_name
import tucargo.composeapp.generated.resources.service_notification_text

class DriverLocationService : Service() {

    companion object {
        const val ACTION_START = "START"
        const val ACTION_STOP = "STOP"
        const val ACTION_UPDATE_TRIP_MODE = "UPDATE_TRIP_MODE"
        const val EXTRA_DRIVER_ID = "driverId"
        const val EXTRA_TRIP_ID = "tripId"

        private const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "tucargo_driver_location"

        // Brand name (not translated — see app_name translatable="false"). Used as the
        // immediate notification title and channel-name fallback so startForeground can
        // fire synchronously before the localized strings are resolved.
        private const val BRAND_TITLE = "TuCargo"

        private const val INTERVAL_ACTIVE_MS = 4_000L
        private const val INTERVAL_AVAILABLE_MS = 30_000L
    }

    private val sendLocationUseCase: SendLocationUseCase by inject()
    private val flushLocationBufferUseCase: FlushLocationBufferUseCase by inject()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var driverId: String? = null
    private var tripId: String? = null
    private var isActiveTripMode = false

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            val currentDriverId = driverId ?: return
            val driverLocation = DriverLocation(
                lat = location.latitude,
                lng = location.longitude,
                accuracyM = location.accuracy,
                speedKph = if (location.hasSpeed()) location.speed * 3.6f else null,
                headingDeg = if (location.hasBearing()) location.bearing else null,
                capturedAt = Instant.fromEpochMilliseconds(location.time),
                tripId = tripId
            )
            serviceScope.launch {
                sendLocationUseCase(currentDriverId, driverLocation)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // Create the channel immediately with the brand name so startForeground works,
        // then update it with the localized name/description once resources resolve.
        createNotificationChannel(name = BRAND_TITLE, description = null)
        serviceScope.launch {
            createNotificationChannel(
                name = getString(Res.string.service_channel_name),
                description = getString(Res.string.service_channel_description),
            )
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                driverId = intent.getStringExtra(EXTRA_DRIVER_ID)
                startForegroundService()
                startLocationUpdates(INTERVAL_AVAILABLE_MS)
                serviceScope.launch {
                    driverId?.let { flushLocationBufferUseCase(it) }
                }
            }
            ACTION_STOP -> {
                stopLocationUpdates()
                stopSelf()
            }
            ACTION_UPDATE_TRIP_MODE -> {
                tripId = intent.getStringExtra(EXTRA_TRIP_ID)
                isActiveTripMode = tripId != null
                restartLocationUpdates(if (isActiveTripMode) INTERVAL_ACTIVE_MS else INTERVAL_AVAILABLE_MS)
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        stopLocationUpdates()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startForegroundService() {
        // Show immediately with the brand title (no localized text needed yet) so the
        // foreground-service start deadline is met, then enrich with the localized body.
        promoteToForeground(buildNotification(contentText = null))
        serviceScope.launch {
            val text = getString(Res.string.service_notification_text)
            withContext(Dispatchers.Main) {
                promoteToForeground(buildNotification(contentText = text))
            }
        }
    }

    private fun buildNotification(contentText: String?): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(BRAND_TITLE)
            .apply { if (!contentText.isNullOrEmpty()) setContentText(contentText) }
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

    private fun promoteToForeground(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates(intervalMs: Long) {
        if (!hasLocationPermission()) return
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
            .setMinUpdateIntervalMillis(intervalMs / 2)
            .build()
        fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun restartLocationUpdates(intervalMs: Long) {
        stopLocationUpdates()
        startLocationUpdates(intervalMs)
    }

    // Re-creating a channel with the same id updates its name/description (API 26+), so this
    // is safe to call first with a fallback name and again with the localized strings.
    private fun createNotificationChannel(name: CharSequence, description: String?) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            name,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            if (description != null) this.description = description
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
}
