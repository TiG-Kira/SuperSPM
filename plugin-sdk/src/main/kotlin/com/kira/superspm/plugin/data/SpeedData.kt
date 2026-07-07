package com.kira.superspm.plugin.data

data class SpeedData(
    val currentSpeed: Double,
    val maxSpeed: Double,
    val avgSpeed: Double,
    val totalDistance: Double,
    val isRecording: Boolean,
    val isSensorMode: Boolean,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val sensorAcceleration: Double,
    val sensorVelocity: Double,
    val recordingDuration: Long,
    val dataPoints: Int
)
