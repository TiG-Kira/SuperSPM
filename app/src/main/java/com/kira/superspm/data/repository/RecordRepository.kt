package com.kira.superspm.data.repository

import com.kira.superspm.data.database.LocationRecordDao
import com.kira.superspm.data.model.LocationRecord
import kotlinx.coroutines.flow.Flow

class RecordRepository(private val dao: LocationRecordDao) {
    val allRecords: Flow<List<LocationRecord>> = dao.getAllRecords()

    suspend fun getRecordById(id: Long): LocationRecord? {
        return dao.getRecordById(id)
    }

    suspend fun insertRecord(record: LocationRecord) {
        dao.insertRecord(record)
    }

    suspend fun updateRecord(record: LocationRecord) {
        dao.updateRecord(record)
    }

    suspend fun deleteRecord(record: LocationRecord) {
        dao.deleteRecord(record)
    }

    suspend fun deleteAllRecords() {
        dao.deleteAllRecords()
    }
}