package com.kira.superspm.plugin.impl

import com.kira.superspm.data.repository.RecordRepository
import com.kira.superspm.plugin.HistoryService
import com.kira.superspm.plugin.data.HistoryRecord
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.koin.java.KoinJavaComponent.inject

object HistoryServiceImpl : HistoryService {
    private val repository: RecordRepository by inject(RecordRepository::class.java)

    override fun getAllRecords(): List<HistoryRecord> {
        return try {
            val records = runBlocking { repository.allRecords.first() }
            val result = mutableListOf<HistoryRecord>()
            for (rec in records) {
                result.add(HistoryRecord(
                    id = rec.id,
                    name = rec.name,
                    startTime = rec.startTime.toEpochMilliseconds(),
                    endTime = rec.endTime?.toEpochMilliseconds(),
                    maxSpeed = rec.maxSpeed,
                    avgSpeed = rec.avgSpeed,
                    totalDistance = rec.totalDistance,
                    dataPoints = rec.dataPoints
                ))
            }
            result
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun getRecordById(id: Long): HistoryRecord? {
        return try {
            val record = runBlocking { repository.getRecordById(id) }
            record?.let {
                HistoryRecord(
                    id = it.id,
                    name = it.name,
                    startTime = it.startTime.toEpochMilliseconds(),
                    endTime = it.endTime?.toEpochMilliseconds(),
                    maxSpeed = it.maxSpeed,
                    avgSpeed = it.avgSpeed,
                    totalDistance = it.totalDistance,
                    dataPoints = it.dataPoints
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun getRecordsCount(): Int {
        return getAllRecords().size
    }
}