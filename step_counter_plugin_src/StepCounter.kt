package com.kira.superspm.plugin

import com.kira.superspm.plugin.data.AnalysisResult
import com.kira.superspm.plugin.data.SpeedData

class StepCounter : BasePlugin(
    name = "步数统计",
    version = "1.0.0",
    author = "SuperSPM",
    description = "基于速度数据估算步数，分析运动状态"
) {

    private var steps = 0
    private var lastSpeed = 0.0
    private var isMoving = false
    private var stepCountStartTime = 0L
    private var totalPace = 0.0
    private var paceCount = 0

    override fun onSpeedUpdate(data: SpeedData): AnalysisResult {
        val speedKmh = data.currentSpeed * 3.6
        
        detectSteps(speedKmh)
        
        val pace = calculatePace(speedKmh)
        val distanceKm = data.totalDistance / 1000
        val calories = calculateCalories(steps)
        
        val (status, icon, desc) = getActivityStatus(speedKmh)
        val advice = generateAdvice(speedKmh, steps)

        val metrics = mapOf(
            "steps" to "$steps 步",
            "pace" to pace,
            "distance" to formatDistance(data.totalDistance),
            "calories" to String.format("%.1f kcal", calories),
            "duration" to formatDuration(data.recordingDuration)
        )

        return AnalysisResult(
            title = status,
            description = desc,
            icon = icon,
            advice = advice,
            metrics = metrics
        )
    }

    private fun detectSteps(speedKmh: Double) {
        val threshold = 0.5
        
        if (speedKmh >= threshold && !isMoving) {
            isMoving = true
            stepCountStartTime = System.currentTimeMillis()
        } else if (speedKmh < threshold && isMoving) {
            isMoving = false
        }
        
        if (isMoving && stepCountStartTime > 0) {
            val stepDurationMs = when {
                speedKmh < 3.0 -> 1200
                speedKmh < 5.0 -> 900
                speedKmh < 7.0 -> 700
                speedKmh < 10.0 -> 500
                else -> 400
            }
            
            val elapsed = System.currentTimeMillis() - stepCountStartTime
            val newSteps = (elapsed / stepDurationMs).toInt()
            if (newSteps > steps) {
                steps = newSteps
            }
        }
    }

    private fun calculatePace(speedKmh: Double): String {
        if (speedKmh <= 0.5) return "--'--''"
        
        val paceMinPerKm = 60.0 / speedKmh
        val minutes = paceMinPerKm.toInt()
        val seconds = ((paceMinPerKm - minutes) * 60).toInt()
        
        return String.format("%d'%02d''", minutes, seconds)
    }

    private fun calculateCalories(steps: Int): Double {
        return steps * 0.04
    }

    private fun getActivityStatus(speedKmh: Double): Triple<String, String, String> {
        return when {
            speedKmh < 0.5 -> Triple("静止", "⏸️", "当前处于静止状态")
            speedKmh < 3.0 -> Triple("慢走", "🚶", "悠闲散步中")
            speedKmh < 5.0 -> Triple("快走", "🚶‍♂️", "快速行走中")
            speedKmh < 7.0 -> Triple("慢跑", "🏃", "慢跑运动中")
            speedKmh < 10.0 -> Triple("跑步", "🏃‍♂️", "中速跑步中")
            speedKmh < 14.0 -> Triple("快跑", "💨", "快速跑步中")
            else -> Triple("冲刺", "⚡", "高速冲刺中")
        }
    }

    private fun generateAdvice(speedKmh: Double, steps: Int): String {
        return when {
            steps < 100 -> "开始运动吧！每天走10000步有益健康"
            steps < 3000 -> "继续加油！已完成每日目标的${(steps/100).toInt()}%"
            steps < 5000 -> "不错！已完成一半目标，保持节奏"
            steps < 8000 -> "很好！距离目标越来越近了"
            steps < 10000 -> "太棒了！马上完成每日目标"
            steps < 15000 -> "优秀！已超额完成目标"
            else -> "卓越！你是运动达人！"
        }
    }

    override fun onStart() {
        super.onStart()
        steps = 0
        lastSpeed = 0.0
        isMoving = false
        stepCountStartTime = 0L
        totalPace = 0.0
        paceCount = 0
    }

    override fun onStop() {
        super.onStop()
        steps = 0
        lastSpeed = 0.0
        isMoving = false
        stepCountStartTime = 0L
    }
}