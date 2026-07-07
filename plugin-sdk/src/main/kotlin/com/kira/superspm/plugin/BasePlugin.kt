package com.kira.superspm.plugin

import com.kira.superspm.plugin.data.AnalysisResult
import com.kira.superspm.plugin.data.SpeedData

abstract class BasePlugin(
    override val name: String,
    override val version: String,
    override val author: String,
    override val description: String
) : Plugin {

    private var _enabled = true
    private var startTime = 0L
    private val configMap = mutableMapOf<String, String>()

    protected fun getElapsedTime(): Long {
        if (startTime == 0L) startTime = System.currentTimeMillis()
        return System.currentTimeMillis() - startTime
    }

    protected fun formatDuration(ms: Long): String {
        val seconds = ms / 1000
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", minutes, secs)
    }

    protected fun formatDistance(meters: Double): String {
        return if (meters < 1000) {
            String.format("%.0f m", meters)
        } else {
            String.format("%.2f km", meters / 1000)
        }
    }

    protected fun formatSpeed(mps: Double): String {
        return String.format("%.1f", mps * 3.6)
    }

    override fun onStart() {
        startTime = System.currentTimeMillis()
        _enabled = true
    }

    override fun onStop() {
        startTime = 0L
    }

    override fun isEnabled(): Boolean = _enabled

    override fun setEnabled(enabled: Boolean) {
        this._enabled = enabled
    }

    override fun getConfig(): Map<String, String> {
        return configMap.toMap()
    }

    override fun setConfig(config: Map<String, String>) {
        configMap.clear()
        configMap.putAll(config)
    }
}