package com.kira.superspm.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.kira.superspm.data.model.LocationRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationRecordDao {
    @Query("SELECT * FROM location_records ORDER BY startTime DESC")
    fun getAllRecords(): Flow<List<LocationRecord>>

    @Query("SELECT * FROM location_records WHERE id = :id")
    suspend fun getRecordById(id: Long): LocationRecord?

    @Insert
    suspend fun insertRecord(record: LocationRecord)

    @Update
    suspend fun updateRecord(record: LocationRecord)

    @Delete
    suspend fun deleteRecord(record: LocationRecord)
}