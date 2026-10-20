package com.kira.superspm.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kira.superspm.data.repository.RecordRepository
import com.kira.superspm.R
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

enum class SpeedMode {
    GPS, SENSOR
}

class SpeedometerViewModel(
    private val repository: RecordRepository,
    private val context: Context
) : ViewModel() {
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
    var selectedMode by mutableStateOf(SpeedMode.GPS)
        private set

    enum class RecordingStatus {
        NOT_STARTED, RECORDING, PAUSED
    }

    fun changeSpeedMode(mode: SpeedMode) {
        selectedMode = mode
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

    /**
     * 位置刷新回调（服务按刷新间隔节流后下发）。
     *
     * 只负责"当前在哪"，不再碰里程 / 均速 / 最高速——这些是 [mirrorStats] 的活。
     * 旧实现在这里按节流后的点重算里程，采样率与服务的逐点累加完全不同，
     * 屏幕上的里程和存库的里程必然对不上。
     */
    fun updateLocation(latitude: Double, longitude: Double, speed: Double, accuracy: Float) {
        currentLatitude = latitude
        currentLongitude = longitude
        currentAccuracy = accuracy
        currentSpeed = speed
    }

    /**
     * 速度刷新回调。同样只更新瞬时速度，统计口径统一由服务回传。
     */
    fun updateSpeed(speed: Double) {
        currentSpeed = speed
    }

    /**
     * 镜像服务的统计结果。服务是里程与速度统计的唯一数据源，UI 不参与计算，
     * 保证"屏幕上看到的"与"存进数据库的"是同一个数。
     */
    fun mirrorStats(
        maxSpeed: Double,
        avgSpeed: Double,
        totalDistance: Double,
        dataPoints: Int
    ) {
        this.maxSpeed = maxSpeed
        this.avgSpeed = avgSpeed
        this.totalDistance = totalDistance
        this.dataPoints = dataPoints
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