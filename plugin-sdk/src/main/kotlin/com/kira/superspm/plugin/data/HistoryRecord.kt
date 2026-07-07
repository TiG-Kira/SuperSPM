package com.kira.superspm.plugin.data

data class HistoryRecord(
    val id: Long,
    val name: String,
    val startTime: Long,
    val endTime: Long?,
    val maxSpeed: Double,
    val avgSpeed: Double,
    val totalDistance: Double,
    val dataPoints: Int
)