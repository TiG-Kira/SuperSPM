package com.kira.superspm.plugin

import com.kira.superspm.plugin.data.AnalysisResult
import com.kira.superspm.plugin.data.SpeedData

class HeartRateMonitor : BasePlugin(
    name = "心率监测（运动估算）",
    version = "1.0.0",
    author = "SuperSPM",
    description = "基于运动强度估算心率，提供健康建议"
) {

    private var heartRateHistory = mutableListOf<Int>()
    private var caloriesBurned = 0.0
    private var lastDuration = 0L

    override fun onSpeedUpdate(data: SpeedData): AnalysisResult {
        val speedKmh = data.currentSpeed * 3.6
        val estimatedHeartRate = estimateHeartRate(speedKmh)
        
        heartRateHistory.add(estimatedHeartRate)
        if (heartRateHistory.size > 60) {
            heartRateHistory.removeAt(0)
        }
        
        val avgHeartRate = heartRateHistory.average().toInt()
        
        val durationDelta = if (lastDuration > 0) data.recordingDuration - lastDuration else 0
        caloriesBurned += calculateCalories(estimatedHeartRate, durationDelta)
        lastDuration = data.recordingDuration
        
        val (status, icon, desc) = getHeartRateStatus(estimatedHeartRate)
        val advice = generateHeartRateAdvice(estimatedHeartRate, speedKmh)

        val metrics = mapOf(
            "heartRate" to "$estimatedHeartRate BPM",
            "avgHeartRate" to "$avgHeartRate BPM",
            "calories" to String.format("%.1f", caloriesBurned),
            "duration" to formatDuration(data.recordingDuration),
            "distance" to formatDistance(data.totalDistance)
        )

        return AnalysisResult(
            title = status,
            description = desc,
            icon = icon,
            advice = advice,
            metrics = metrics
        )
    }

    private fun estimateHeartRate(speedKmh: Double): Int {
        if (speedKmh < 0.5) return 65
        
        val baseHeartRate = 65
        val speedFactor = when {
            speedKmh < 3.0 -> 0.15
            speedKmh < 5.0 -> 0.35
            speedKmh < 7.0 -> 0.55
            speedKmh < 10.0 -> 0.75
            speedKmh < 14.0 -> 0.90
            else -> 1.0
        }
        
        val maxHeartRate = 220 - 30
        return (baseHeartRate + (maxHeartRate - baseHeartRate) * speedFactor).toInt()
    }

    private fun calculateCalories(heartRate: Int, durationMs: Long): Double {
        if (heartRate < 70 || durationMs <= 0) return 0.0
        
        val met = when {
            heartRate < 90 -> 3.0
            heartRate < 110 -> 5.0
            heartRate < 130 -> 7.0
            heartRate < 150 -> 9.0
            heartRate < 170 -> 11.0
            else -> 13.0
        }
        
        return met * 70 * (durationMs / 3600000.0)
    }

    private fun getHeartRateStatus(heartRate: Int): Triple<String, String, String> {
        return when {
            heartRate < 60 -> Triple("心率过缓", "🟢", "心率偏低，建议适当活动")
            heartRate < 75 -> Triple("静息心率", "🟢", "心率正常，处于休息状态")
            heartRate < 90 -> Triple("轻度运动", "🟡", "轻度运动状态，心率适中")
            heartRate < 110 -> Triple("中度运动", "🟡", "中度运动状态，效果良好")
            heartRate < 130 -> Triple("剧烈运动", "🔴", "剧烈运动状态，注意呼吸")
            heartRate < 150 -> Triple("高强度运动", "🔴", "高强度运动，注意监测")
            else -> Triple("极限运动", "⚫", "极限心率，请适当休息")
        }
    }

    private fun generateHeartRateAdvice(heartRate: Int, speedKmh: Double): String {
        return when {
            heartRate < 60 -> "当前心率较低，建议起身活动一下"
            heartRate < 75 -> "心率正常，适合日常活动"
            heartRate < 90 -> "轻度运动，保持这个节奏"
            heartRate < 110 -> "中度运动，心率保持在理想范围"
            heartRate < 130 -> "剧烈运动状态，注意保持呼吸节奏"
            heartRate < 150 -> "高强度运动，请关注身体感受，适时休息"
            else -> "心率已接近极限，建议减速或暂停休息"
        }
    }

    override fun onStart() {
        super.onStart()
        heartRateHistory.clear()
        caloriesBurned = 0.0
        lastDuration = 0L
    }

    override fun onStop() {
        super.onStop()
        heartRateHistory.clear()
        caloriesBurned = 0.0
        lastDuration = 0L
    }
}