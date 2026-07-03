package com.kira.superspm.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Entity(tableName = "location_records")
data class LocationRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val startTime: Instant,
    val endTime: Instant?,
    val maxSpeed: Double,
    val avgSpeed: Double,
    val totalDistance: Double,
    val dataPoints: Int,
    val pathData: String
)

@Serializable
data class LocationPoint(
    val latitude: Double,
    val longitude: Double,
    val speed: Double,
    val timestamp: Long
)

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? {
        return value?.let { Instant.fromEpochMilliseconds(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Instant?): Long? {
        return date?.toEpochMilliseconds()
    }
}