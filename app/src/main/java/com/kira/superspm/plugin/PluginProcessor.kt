package com.kira.superspm.plugin

interface PluginProcessor {
    fun getName(): String
    fun getVersion(): String
    fun getDescription(): String
    fun getAuthor(): String
    fun getType(): String = "NATIVE"

    fun onSpeedUpdate(data: SpeedData): AnalysisResult
    fun onStart()
    fun onStop()
    fun isEnabled(): Boolean
    fun setEnabled(enabled: Boolean)
    fun getConfig(): Map<String, String>
    fun setConfig(config: Map<String, String>)
}

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

data class AnalysisResult(
    val title: String,
    val description: String,
    val icon: String,
    val advice: String,
    val metrics: Map<String, String>
)
