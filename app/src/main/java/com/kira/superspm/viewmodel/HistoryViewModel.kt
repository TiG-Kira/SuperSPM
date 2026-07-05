package com.kira.superspm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kira.superspm.data.model.LocationRecord
import com.kira.superspm.data.repository.RecordRepository
import kotlinx.coroutines.launch

class HistoryViewModel(private val repository: RecordRepository) : ViewModel() {
    val records = repository.allRecords

    fun deleteRecord(record: LocationRecord) {
        viewModelScope.launch {
            repository.deleteRecord(record)
        }
    }

    fun renameRecord(record: LocationRecord, newName: String) {
        viewModelScope.launch {
            repository.updateRecord(record.copy(name = newName))
        }
    }

    fun deleteAllRecords() {
        viewModelScope.launch {
            repository.deleteAllRecords()
        }
    }
}