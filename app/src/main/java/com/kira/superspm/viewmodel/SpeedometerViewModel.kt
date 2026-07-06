package com.kira.superspm.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kira.superspm.data.model.LocationPoint
import com.kira.superspm.data.model.LocationRecord
import com.kira.superspm.data.repository.RecordRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SpeedometerViewModel(private val repository: RecordRepository) : ViewModel() {
    var currentSpeed by mutableStateOf(0.0)
        private set
    var maxSpeed by mutableStateOf(0.0)
        private set
    var avgSpeed by mutableStateOf(0.0)
        private set
    var totalDistance by mutableStateOf(0.0)
        private set
    var dataPoints by mutableStateOf(0)
        private set
    var status by mutableStateOf(RecordingStatus.NOT_STARTED)
        private set
    var isRecording by mutableStateOf(false)
        private set
    var startTime by mutableStateOf<Instant?>(null)
        private set
    var currentLatitude by mutableStateOf<Double?>(null)
        private set
    var currentLongitude by mutableStateOf<Double?>(null)
        private set
    var currentAccuracy by mutableStateOf<Float?>(null)
        private set

    private val pathPoints = mutableListOf<LocationPoint>()
    private var lastPoint: LocationPoint? = null

    enum class RecordingStatus {
        NOT_STARTED, RECORDING, PAUSED
    }

    fun startRecording(recordData: Boolean = false) {
        status = RecordingStatus.RECORDING
        isRecording = recordData
        startTime = Clock.System.now()
        currentSpeed = 0.0
        maxSpeed = 0.0
        avgSpeed = 0.0
        totalDistance = 0.0
        dataPoints = 0
        pathPoints.clear()
        lastPoint = null
    }

    fun pauseRecording() {
        if (status == RecordingStatus.RECORDING) {
            status = RecordingStatus.PAUSED
        }
    }

    fun resumeRecording() {
        if (status == RecordingStatus.PAUSED) {
            status = RecordingStatus.RECORDING
        }
    }

    suspend fun stopRecording(): LocationRecord? {
        val currentStartTime = startTime ?: return null
        status = RecordingStatus.NOT_STARTED
        val endTime = Clock.System.now()
        val pathData = Json.encodeToString(pathPoints)

        val record = LocationRecord(
            name = generateDefaultName(),
            startTime = currentStartTime,
            endTime = endTime,
            maxSpeed = maxSpeed,
            avgSpeed = avgSpeed,
            totalDistance = totalDistance,
            dataPoints = dataPoints,
            pathData = pathData
        )

        repository.insertRecord(record)

        startTime = null
        pathPoints.clear()
        lastPoint = null

        return record
    }

    private suspend fun generateDefaultName(): String {
        val records = repository.allRecords.first()
        val count = records.size + 1
        return "记录 $count"
    }

    fun updateLocation(latitude: Double, longitude: Double, speed: Double, accuracy: Float) {
        currentLatitude = latitude
        currentLongitude = longitude
        currentAccuracy = accuracy

        if (status == RecordingStatus.RECORDING) {
            currentSpeed = speed
            maxSpeed = maxOf(maxSpeed, speed)
            dataPoints++
            avgSpeed = ((avgSpeed * (dataPoints - 1)) + speed) / dataPoints

            val currentPoint = LocationPoint(latitude, longitude, speed, System.currentTimeMillis())
            if (lastPoint != null) {
                totalDistance += calculateDistance(lastPoint!!, currentPoint)
            }
            pathPoints.add(currentPoint)
            lastPoint = currentPoint
        } else {
            currentSpeed = speed
        }
    }

    fun updateSpeed(speed: Double) {
        currentSpeed = speed
        if (status == RecordingStatus.RECORDING) {
            maxSpeed = maxOf(maxSpeed, speed)
            dataPoints++
            avgSpeed = ((avgSpeed * (dataPoints - 1)) + speed) / dataPoints
        }
    }

    fun addDistance(distance: Double) {
        if (status == RecordingStatus.RECORDING) {
            totalDistance += distance
        }
    }

    private fun calculateDistance(point1: LocationPoint, point2: LocationPoint): Double {
        val R = 6371000.0
        val lat1 = Math.toRadians(point1.latitude)
        val lon1 = Math.toRadians(point1.longitude)
        val lat2 = Math.toRadians(point2.latitude)
        val lon2 = Math.toRadians(point2.longitude)

        val dLat = lat2 - lat1
        val dLon = lon2 - lon1

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return R * c / 1000.0
    }

    fun reset() {
        status = RecordingStatus.NOT_STARTED
        isRecording = false
        currentSpeed = 0.0
        maxSpeed = 0.0
        avgSpeed = 0.0
        totalDistance = 0.0
        dataPoints = 0
        startTime = null
        pathPoints.clear()
        lastPoint = null
    }

    fun finishRecording() {
        isRecording = false
    }

    fun restoreFromService(
        currentSpeed: Double,
        maxSpeed: Double,
        avgSpeed: Double,
        totalDistance: Double,
        dataPoints: Int
    ) {
        status = RecordingStatus.RECORDING
        this.currentSpeed = currentSpeed
        this.maxSpeed = maxSpeed
        this.avgSpeed = avgSpeed
        this.totalDistance = totalDistance
        this.dataPoints = dataPoints
        this.isRecording = true
    }
}