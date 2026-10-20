package com.kira.superspm.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.kira.superspm.R
import com.kira.superspm.data.model.LocationPoint
import com.kira.superspm.ui.MainActivity
import com.kira.superspm.utils.MileageTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.android.ext.android.inject

class LocationService : Service(), LocationListener {
    private lateinit var locationManager: LocationManager
    private var isGpsActive = true
    private var lastSpeed = 0.0
    private var idleStartTime: Long = 0
    private var isRunning = false
    private var isSensorMode = false
    private val recordRepository by inject<com.kira.superspm.data.repository.RecordRepository>()
    private var lastPositionUpdateTime = 0L

    var currentSpeed = 0.0
    var maxSpeed = 0.0
    var avgSpeed = 0.0
    var totalDistance = 0.0
    var dataPoints = 0
    var isRecording = false
    var shouldSaveRecord = false
    var currentLatitude = 0.0
    var currentLongitude = 0.0
    var currentAccuracy = 0f
    var sensorAcceleration = 0.0
    var sensorVelocity = 0.0
    var recordingStartTime = 0L
    private val pathPoints = mutableListOf<LocationPoint>()
    private var startTime: Long = 0

    /** 里程唯一数据源：所有模式的里程都经它累加，UI 只做镜像不再自算 */
    private val mileageTracker = MileageTracker()

    /** 最近一次 GPS fix 的到达时间（毫秒），用于压制网络定位的插队 */
    private var lastGpsFixTime = 0L

    interface RecordingCallback {
        fun onRecordSaved()
    }

    private var recordingCallback: RecordingCallback? = null

    fun setRecordingCallback(callback: RecordingCallback) {
        recordingCallback = callback
    }

    companion object {
        const val CHANNEL_ID = "LocationServiceChannel"
        const val NOTIFICATION_ID = 1
        var onLocationUpdate: ((latitude: Double, longitude: Double, speed: Double, accuracy: Float) -> Unit)? = null
        var onSpeedUpdate: ((speed: Double) -> Unit)? = null
        var onStatusChange: ((isGpsActive: Boolean) -> Unit)? = null
        var onError: ((errorMessage: String) -> Unit)? = null
        var onGpsSignalUpdate: ((accuracy: Float) -> Unit)? = null
        var onServiceStopped: (() -> Unit)? = null
        /** 里程/速度统计回传：UI 只做镜像，禁止自行累加，避免与服务两套口径 */
        var onStatsUpdate: ((maxSpeed: Double, avgSpeed: Double, totalDistance: Double, dataPoints: Int) -> Unit)? = null
        var currentInterval = 1000L
        var currentFastestInterval = 500L
        var positionRefreshInterval = 120000L

        /** GPS 新鲜窗口：GPS 在此时长内有过 fix，则网络定位点整点丢弃 */
        const val GPS_FRESH_WINDOW_MS = 10_000L
        private var instance: LocationService? = null

        fun isRunning(): Boolean = instance?.isRunning == true
        fun isRecording(): Boolean = instance?.isRecording == true
        fun isSensorMode(): Boolean = instance?.isSensorMode == true
        fun getCurrentSpeed(): Double = instance?.currentSpeed ?: 0.0
        fun getMaxSpeed(): Double = instance?.maxSpeed ?: 0.0
        fun getAvgSpeed(): Double = instance?.avgSpeed ?: 0.0
        fun getTotalDistance(): Double = instance?.totalDistance ?: 0.0
        fun getDataPoints(): Int = instance?.dataPoints ?: 0
        fun getCurrentLatitude(): Double = instance?.currentLatitude ?: 0.0
        fun getCurrentLongitude(): Double = instance?.currentLongitude ?: 0.0
        fun getCurrentAccuracy(): Float = instance?.currentAccuracy ?: 0f
        fun getSensorAcceleration(): Double = instance?.sensorAcceleration ?: 0.0
        fun getSensorVelocity(): Double = instance?.sensorVelocity ?: 0.0
        fun getRecordingStartTime(): Long = instance?.recordingStartTime ?: 0L
        fun getRecordingDuration(): Long {
            if (instance?.isRecording == false) return 0L
            val start = instance?.recordingStartTime ?: System.currentTimeMillis()
            return System.currentTimeMillis() - start
        }

        fun stopRecording(): com.kira.superspm.data.model.LocationRecord? {
            return instance?.finishRecording()
        }

        fun finishRecording(): com.kira.superspm.data.model.LocationRecord? {
            return instance?.finishRecording()
        }

        fun updateSettings(powerSaving: Boolean, refreshTimeSec: Int) {
            currentInterval = if (powerSaving) 10000L else 1000L
            currentFastestInterval = currentInterval / 2
            positionRefreshInterval = if (refreshTimeSec == 0) currentInterval else refreshTimeSec * 1000L
        }

        fun updateWithSensorData(speed: Double, distanceDelta: Double) {
            instance?.updateWithSensorData(speed, distanceDelta)
        }

        fun requestSingleUpdate(context: Context) {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                onError?.invoke(context.getString(R.string.location_permission_not_granted))
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
                            onGpsSignalUpdate?.invoke(location.accuracy)
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
                            onGpsSignalUpdate?.invoke(location.accuracy)
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
        instance = this
        createNotificationChannel()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP_RECORDING") {
            finishRecording()
            stopSelf()
            return START_NOT_STICKY
        }

        val sensorMode = intent?.getBooleanExtra("sensorMode", false) ?: false
        isSensorMode = sensorMode

        val serviceType = if (isSensorMode)
            android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        else
            android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
        startForeground(NOTIFICATION_ID, createNotification(), serviceType)

        if (!isSensorMode) {
            if (!checkPermissions()) {
                onError?.invoke(getString(R.string.location_permission_not_granted))
                return START_STICKY
            }

            if (!isGpsEnabled()) {
                onError?.invoke(getString(R.string.gps_not_enabled))
            }
        }

        if (!isRunning) {
            if (!isSensorMode) {
                startLocationUpdates()
            }
            isRunning = true
        }

        val saveRecord = intent?.getBooleanExtra("saveRecord", false) ?: false
        if (!isRecording) {
            startRecording(saveRecord)
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
            onError?.invoke(getString(R.string.location_permission_denied_with_message, e.message))
        } catch (e: Exception) {
            onError?.invoke(getString(R.string.start_location_service_failed_with_message, e.message))
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

    fun startRecording(saveRecord: Boolean = false) {
        isRecording = true
        shouldSaveRecord = saveRecord
        startTime = System.currentTimeMillis()
        currentSpeed = 0.0
        maxSpeed = 0.0
        avgSpeed = 0.0
        totalDistance = 0.0
        dataPoints = 0
        pathPoints.clear()
        lastGpsFixTime = 0L
        mileageTracker.reset()
        speedHistory.clear()
        updateNotification()
    }

    fun stopRecording() {
        isRecording = false
    }

    fun finishRecording(): com.kira.superspm.data.model.LocationRecord? {
        if (!shouldSaveRecord || (pathPoints.isEmpty() && !isSensorMode)) {
            resetRecording()
            return null
        }

        return try {
            val record = com.kira.superspm.data.model.LocationRecord(
                name = generateRecordName(),
                startTime = kotlinx.datetime.Instant.fromEpochMilliseconds(startTime),
                endTime = kotlinx.datetime.Instant.fromEpochMilliseconds(System.currentTimeMillis()),
                maxSpeed = maxSpeed,
                avgSpeed = avgSpeed,
                totalDistance = totalDistance,
                dataPoints = dataPoints,
                pathData = kotlinx.serialization.json.Json.encodeToString(pathPoints)
            )
            CoroutineScope(Dispatchers.IO).launch {
                recordRepository.insertRecord(record)
            }
            resetRecording()
            recordingCallback?.onRecordSaved()
            record
        } catch (e: Exception) {
            resetRecording()
            null
        }
    }

    private fun resetRecording() {
        isRecording = false
        shouldSaveRecord = false
        startTime = 0
        currentSpeed = 0.0
        maxSpeed = 0.0
        avgSpeed = 0.0
        totalDistance = 0.0
        dataPoints = 0
        pathPoints.clear()
        lastGpsFixTime = 0L
        mileageTracker.reset()
        speedHistory.clear()
    }

    private fun generateRecordName(): String {
        return try {
            val records = kotlinx.coroutines.runBlocking<List<com.kira.superspm.data.model.LocationRecord>> {
                recordRepository.allRecords.first()
            }
            val count = records.size + 1
            "${getString(R.string.record)} $count"
        } catch (e: Exception) {
            "${getString(R.string.record)} ${System.currentTimeMillis()}"
        }
    }

    private val speedHistory = mutableListOf<Double>()
    private val HISTORY_SIZE = 5

    override fun onLocationChanged(location: Location) {
        // 单流优先：GPS 与 NETWORK 两个 provider 的结果会互跳（网络定位常差数百米），
        // 同时喂进同一累加器就是"幽灵里程"的根源。GPS 新鲜期内的网络定位整点丢弃。
        val now0 = System.currentTimeMillis()
        if (location.provider == LocationManager.GPS_PROVIDER) {
            lastGpsFixTime = now0
        } else if (now0 - lastGpsFixTime < GPS_FRESH_WINDOW_MS) {
            return
        }

        var speed = location.speed * 3.6

        speedHistory.add(speed)
        if (speedHistory.size > HISTORY_SIZE) {
            speedHistory.removeAt(0)
        }

        val avgHistorySpeed = speedHistory.average()

        if (speed < 0.5 && lastSpeed > 5) {
            speed = lastSpeed * 0.85
        }

        val finalSpeed = (speed * 0.6) + (avgHistorySpeed * 0.3) + (lastSpeed * 0.1)

        lastSpeed = finalSpeed
        currentSpeed = finalSpeed
        currentLatitude = location.latitude
        currentLongitude = location.longitude
        currentAccuracy = location.accuracy

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

        if (isRecording) {
            maxSpeed = maxOf(maxSpeed, finalSpeed)

            // 里程与轨迹只认 tracker 的裁决结果：精度 / 抖动 / 跳变 / 断续四道过滤全在这里
            val accepted = mileageTracker.add(
                latitude = location.latitude,
                longitude = location.longitude,
                // 不用 Location.hasAccuracy()（API 级别不确定，minSdk 26 下有风险）；
                // accuracy == 0 即设备未提供，按最差值处理，等价于被精度门限拒掉
                accuracyM = if (location.accuracy > 0f) location.accuracy else Float.MAX_VALUE,
                speedMps = location.speed,
                timestampMs = if (location.time > 0) location.time else now0
            )
            if (accepted != null) {
                pathPoints.add(accepted)
                dataPoints = mileageTracker.acceptedPoints
                avgSpeed = ((avgSpeed * (dataPoints - 1)) + finalSpeed) / dataPoints
            }
            totalDistance = mileageTracker.totalDistanceKm

            updateNotification()
            onStatsUpdate?.invoke(maxSpeed, avgSpeed, totalDistance, dataPoints)
        }

        onSpeedUpdate?.invoke(speed)
        onGpsSignalUpdate?.invoke(location.accuracy)

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
            onError?.invoke(getString(R.string.gps_turned_off))
        }
    }

    fun restartLocationUpdates() {
        if (!isGpsActive) {
            isGpsActive = true
            onStatusChange?.invoke(true)
            startLocationUpdates()
        }
    }

    fun updateWithSensorData(speed: Double, distanceDelta: Double) {
        if (!isSensorMode) return
        
        currentSpeed = speed
        
        if (isRecording) {
            maxSpeed = maxOf(maxSpeed, speed)
            dataPoints++
            avgSpeed = if (dataPoints > 0) ((avgSpeed * (dataPoints - 1)) + speed) / dataPoints else speed
            // 惯性推算无定位点可过滤，直接注入同一累加器，保证与 GPS 模式口径一致
            mileageTracker.addDistance(distanceDelta)
            totalDistance = mileageTracker.totalDistanceKm
            updateNotification()
            onStatsUpdate?.invoke(maxSpeed, avgSpeed, totalDistance, dataPoints)
        }
    }

    fun requestSingleUpdate() {
        if (!checkPermissions()) {
            onError?.invoke(getString(R.string.location_permission_not_granted))
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
            getString(R.string.location_service),
            NotificationManager.IMPORTANCE_LOW
        )
        channel.description = getString(R.string.realtime_speed_info)
        channel.setSound(null, null)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        val intent = createLaunchIntent()
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, LocationService::class.java).apply {
            action = "STOP_RECORDING"
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = buildNotificationBuilder(pendingIntent)
            .setContentText(getString(R.string.recording))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder.addAction(
                android.app.Notification.Action.Builder(
                    0, getString(R.string.stop), stopPendingIntent
                ).build()
            )
        } else {
            notificationBuilder.addAction(0, getString(R.string.stop), stopPendingIntent)
        }

        return notificationBuilder.build()
    }

    fun updateNotification() {
        val intent = createLaunchIntent()
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, LocationService::class.java).apply {
            action = "STOP_RECORDING"
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = buildNotificationBuilder(pendingIntent)
            .setContentText(String.format(getString(R.string.notification_speed), currentSpeed))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder.addAction(
                android.app.Notification.Action.Builder(
                    0, getString(R.string.stop), stopPendingIntent
                ).build()
            )
        } else {
            notificationBuilder.addAction(0, getString(R.string.stop), stopPendingIntent)
        }

        val bigText = String.format(
            getString(R.string.notification_summary),
            currentSpeed, maxSpeed, avgSpeed, totalDistance
        )
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder.setStyle(android.app.Notification.BigTextStyle().bigText(bigText))
        } else {
            notificationBuilder.setStyle(android.app.Notification.BigTextStyle().bigText(bigText))
        }

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun buildNotificationBuilder(pendingIntent: PendingIntent): Notification.Builder {
        val notificationTitle = if (isSensorMode) getString(R.string.sensor_speed_measuring) else getString(R.string.gps_speed_measuring)
        val notificationBuilder = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle(notificationTitle)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)

        if (Build.VERSION.SDK_INT >= 36) {
            notificationBuilder.setPriority(Notification.PRIORITY_HIGH)
            
            try {
                notificationBuilder.javaClass.getMethod(
                    "setRequestPromotedOngoing", Boolean::class.javaPrimitiveType
                ).invoke(notificationBuilder, true)
            } catch (e: Exception) {
            }

            try {
                notificationBuilder.javaClass.getMethod(
                    "setShortCriticalText", String::class.java
                ).invoke(notificationBuilder, "${String.format("%.0f", currentSpeed)} km/h")
            } catch (e: Exception) {
            }
        }

        return notificationBuilder
    }

    private fun createLaunchIntent(): Intent {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        return intent ?: Intent(this, MainActivity::class.java)
    }

    fun getPathPoints(): List<LocationPoint> {
        return pathPoints.toList()
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        isRunning = false
        isSensorMode = false
        try {
            locationManager.removeUpdates(this)
        } catch (e: Exception) {
        }
        onServiceStopped?.invoke()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}