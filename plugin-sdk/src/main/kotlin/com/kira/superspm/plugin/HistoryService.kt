package com.kira.superspm.plugin

import com.kira.superspm.plugin.data.HistoryRecord

interface HistoryService {
    fun getAllRecords(): List<HistoryRecord>
    fun getRecordById(id: Long): HistoryRecord?
    fun getRecordsCount(): Int
}