package com.kira.superspm.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.kira.superspm.R

class LocationService : Service(), LocationListener {
    private lateinit var locationManager: LocationManager
    private var isGpsActive = true
    private var lastSpeed = 0.0
    private var idleStartTime: Long = 0
    private var isRunning = false
    private var lastPositionUpdateTime = 0L

    companion object {
        const val CHANNEL_ID = "LocationServiceChannel"
        const val NOTIFICATION_ID = 1
        var onLocationUpdate: ((latitude: Double, longitude: Double, speed: Double, accuracy: Float) -> Unit)? = null
        var onSpeedUpdate: ((speed: Double) -> Unit)? = null
        var onStatusChange: ((isGpsActive: Boolean) -> Unit)? = null
        var onError: ((errorMessage: String) -> Unit)? = null
        var currentInterval = 1000L
        var currentFastestInterval = 500L
        var positionRefreshInterval = 120000L
        private var instance: LocationService? = null

        fun updateSettings(powerSaving: Boolean, refreshTimeSec: Int) {
            currentInterval = if (powerSaving) 10000L else 1000L
            currentFastestInterval = currentInterval / 2
            positionRefreshInterval = refreshTimeSec * 1000L
        }

        fun requestSingleUpdate(context: Context) {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                onError?.invoke("位置权限未授予")
                return
            }

            try {
                locationManager.requestSingleUpdate(
                    LocationManager.GPS_PROVIDER,
                    object : LocationListener {
                        override fun onLocationChanged(location: Location) {
                            onLocationUpdate?.invoke(
                                location.latitude,
                                location.longitude,
                                location.speed * 3.6,
                                location.accuracy
                            )
                        }

                        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                        override fun onProviderEnabled(provider: String) {}
                        override fun onProviderDisabled(provider: String) {}
                    },
                    android.os.Looper.getMainLooper()
                )
            } catch (e: SecurityException) {
            } catch (e: Exception) {
            }

            try {
                locationManager.requestSingleUpdate(
                    LocationManager.NETWORK_PROVIDER,
                    object : LocationListener {
                        override fun onLocationChanged(location: Location) {
                            onLocationUpdate?.invoke(
                                location.latitude,
                                location.longitude,
                                location.speed * 3.6,
                                location.accuracy
                            )
                        }

                        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                        override fun onProviderEnabled(provider: String) {}
                        override fun onProviderDisabled(provider: String) {}
                    },
                    android.os.Looper.getMainLooper()
                )
            } catch (e: SecurityException) {
            } catch (e: Exception) {
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        
        if (!checkPermissions()) {
            onError?.invoke("位置权限未授予")
            return START_STICKY
        }

        if (!isGpsEnabled()) {
            onError?.invoke("GPS 未开启")
        }

        if (!isRunning) {
            startLocationUpdates()
            isRunning = true
        }

        return START_STICKY
    }

    private fun checkPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun isGpsEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun startLocationUpdates() {
        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                currentInterval,
                0f,
                this
            )
        } catch (e: SecurityException) {
            onError?.invoke("位置权限被拒绝: ${e.message}")
        } catch (e: Exception) {
            onError?.invoke("启动位置服务失败: ${e.message}")
        }

        try {
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                currentInterval,
                0f,
                this
            )
        } catch (e: SecurityException) {
        } catch (e: Exception) {
        }
    }

    override fun onLocationChanged(location: Location) {
        val speed = location.speed * 3.6
        lastSpeed = speed

        if (speed > 0.5) {
            idleStartTime = 0
            if (!isGpsActive) {
                isGpsActive = true
                onStatusChange?.invoke(true)
            }
        } else {
            if (idleStartTime == 0L) {
                idleStartTime = System.currentTimeMillis()
            } else if (System.currentTimeMillis() - idleStartTime > 60000 && isGpsActive) {
                isGpsActive = false
                onStatusChange?.invoke(false)
            }
        }

        onSpeedUpdate?.invoke(speed)

        val now = System.currentTimeMillis()
        if (now - lastPositionUpdateTime >= positionRefreshInterval) {
            lastPositionUpdateTime = now
            onLocationUpdate?.invoke(
                location.latitude,
                location.longitude,
                speed,
                location.accuracy
            )
        }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String) {
        isGpsActive = true
        onStatusChange?.invoke(true)
    }

    override fun onProviderDisabled(provider: String) {
        if (provider == LocationManager.GPS_PROVIDER) {
            isGpsActive = false
            onStatusChange?.invoke(false)
            onError?.invoke("GPS 已关闭")
        }
    }

    fun restartLocationUpdates() {
        if (!isGpsActive) {
            isGpsActive = true
            onStatusChange?.invoke(true)
            startLocationUpdates()
        }
    }

    fun requestSingleUpdate() {
        if (!checkPermissions()) {
            onError?.invoke("位置权限未授予")
            return
        }

        try {
            locationManager.requestSingleUpdate(
                LocationManager.GPS_PROVIDER,
                this,
                android.os.Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
        } catch (e: Exception) {
        }

        try {
            locationManager.requestSingleUpdate(
                LocationManager.NETWORK_PROVIDER,
                this,
                android.os.Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
        } catch (e: Exception) {
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "位置服务",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.recording))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        try {
            locationManager.removeUpdates(this)
        } catch (e: Exception) {
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}