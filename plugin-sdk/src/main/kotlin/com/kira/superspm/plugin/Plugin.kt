package com.kira.superspm.plugin

import com.kira.superspm.plugin.data.AnalysisResult
import com.kira.superspm.plugin.data.SpeedData

interface Plugin {
    val name: String
    val version: String
    val author: String
    val description: String
    val type: String get() = "NATIVE"

    fun onSpeedUpdate(data: SpeedData): AnalysisResult
    fun onStart() {}
    fun onStop() {}
    fun isEnabled(): Boolean
    fun setEnabled(enabled: Boolean)
    fun getConfig(): Map<String, String>
    fun setConfig(config: Map<String, String>)
}