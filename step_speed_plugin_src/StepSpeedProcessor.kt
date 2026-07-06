package com.kira.superspm.plugin

class StepSpeedProcessor : PluginProcessor {
    private var enabled = true
    private var stepCount = 0
    private var lastSpeed = 0.0
    private var totalSteps = 0
    private var startTime = 0L

    override fun getName(): String = "步速测量"

    override fun getVersion(): String = "1.0.0"

    override fun getDescription(): String = "利用传感器数据测量步速和跑步速度，提供运动建议"

    override fun getAuthor(): String = "SuperSPM"

    override fun onSpeedUpdate(data: SpeedData): AnalysisResult {
        if (startTime == 0L) startTime = System.currentTimeMillis()

        val speedKmh = data.currentSpeed * 3.6

        // 检测步数变化
        val speedDiff = Math.abs(speedKmh - lastSpeed)
        if (speedDiff > 0.5 && speedKmh > 1.0) {
            stepCount++
            totalSteps++
        }
        lastSpeed = speedKmh

        // 检测活动模式
        val (mode, icon, desc) = detectActivityMode(speedKmh)

        // 计算步频（步/分钟）
        val stepFrequency = calculateStepFrequency(speedKmh)

        // 估算步数
        val estimatedSteps = estimateSteps(data)

        // 生成建议
        val advice = generateAdvice(speedKmh, mode)

        val metrics = mapOf(
            "speed" to String.format("%.1f", speedKmh),
            "stepFrequency" to String.format("%.0f", stepFrequency),
            "steps" to estimatedSteps.toString(),
            "duration" to formatDuration(data.recordingDuration),
            "distance" to formatDistance(data.totalDistance)
        )

        return AnalysisResult(
            title = mode,
            description = desc,
            icon = icon,
            advice = advice,
            metrics = metrics
        )
    }

    private fun detectActivityMode(speedKmh: Double): Triple<String, String, String> {
        return when {
            speedKmh < 0.5 -> Triple("静止", "⏸️", "当前处于静止状态")
            speedKmh < 3.0 -> Triple("慢走", "🚶", "您正在慢走，节奏轻松")
            speedKmh < 5.0 -> Triple("正常步行", "🚶", "正常步行速度，适合日常活动")
            speedKmh < 7.0 -> Triple("快走", "🚶‍♂️", "快走状态，有较好的锻炼效果")
            speedKmh < 10.0 -> Triple("慢跑", "🏃", "慢跑状态，保持节奏注意呼吸")
            speedKmh < 14.0 -> Triple("中速跑", "🏃", "中速跑步，注意控制节奏")
            speedKmh < 18.0 -> Triple("快跑", "🏃‍♂️", "快速奔跑，注意安全")
            else -> Triple("极速跑", "💨", "极速运动，请量力而行")
        }
    }

    private fun calculateStepFrequency(speedKmh: Double): Double {
        return when {
            speedKmh < 0.5 -> 0.0
            speedKmh < 3.0 -> speedKmh * 20.0
            speedKmh < 5.0 -> speedKmh * 24.0
            speedKmh < 7.0 -> speedKmh * 26.0
            speedKmh < 10.0 -> speedKmh * 28.0
            speedKmh < 14.0 -> speedKmh * 30.0
            speedKmh < 18.0 -> speedKmh * 32.0
            else -> speedKmh * 34.0
        }
    }

    private fun estimateSteps(data: SpeedData): Int {
        val durationMinutes = data.recordingDuration / 60000.0
        val speedKmh = data.currentSpeed * 3.6
        val stepFreq = calculateStepFrequency(speedKmh)
        return (durationMinutes * stepFreq).toInt()
    }

    private fun generateAdvice(speedKmh: Double, mode: String): String {
        return when {
            speedKmh < 0.5 -> "当前静止，可以开始活动了"
            speedKmh < 3.0 -> "慢走有益健康，建议每天步行30分钟以上"
            speedKmh < 5.0 -> "正常步行速度，适合日常通勤和活动"
            speedKmh < 7.0 -> "快走是很好的有氧运动，保持下去！"
            speedKmh < 10.0 -> "慢跑状态，注意保持呼吸节奏，建议鼻吸口呼"
            speedKmh < 14.0 -> "中速跑步，注意控制心率在适当范围"
            speedKmh < 18.0 -> "快速奔跑，注意安全，避免过度疲劳"
            else -> "极速运动，请注意安全，适时减速休息"
        }
    }

    private fun formatDuration(ms: Long): String {
        val seconds = ms / 1000
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", minutes, secs)
    }

    private fun formatDistance(meters: Double): String {
        return if (meters < 1000) {
            String.format("%.0f m", meters)
        } else {
            String.format("%.2f km", meters / 1000)
        }
    }

    override fun onStart() {
        startTime = System.currentTimeMillis()
        stepCount = 0
        totalSteps = 0
        lastSpeed = 0.0
    }

    override fun onStop() {
        stepCount = 0
    }

    override fun isEnabled(): Boolean = enabled

    override fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    override fun getConfig(): Map<String, String> {
        return mapOf("enabled" to enabled.toString())
    }

    override fun setConfig(config: Map<String, String>) {
        config["enabled"]?.let { this.enabled = it.toBoolean() }
    }
}
