package com.kira.superspm.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kira.superspm.data.model.LocationPoint
import com.kira.superspm.data.model.LocationRecord
import com.kira.superspm.data.repository.RecordRepository
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class DetailViewModel(private val repository: RecordRepository) : ViewModel() {
    var record by mutableStateOf<LocationRecord?>(null)
        private set
    var pathPoints by mutableStateOf<List<LocationPoint>>(emptyList())
        private set

    fun loadRecord(id: Long) {
        viewModelScope.launch {
            record = repository.getRecordById(id)
            record?.let {
                pathPoints = try {
                    Json.decodeFromString(it.pathData)
                } catch (_: Exception) {
                    emptyList()
                }
            }
        }
    }

    fun getSpeedData(): List<Pair<Long, Double>> {
        return pathPoints.map { it.timestamp to it.speed }
    }
}