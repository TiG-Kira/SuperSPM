package com.kira.superspm.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.kira.superspm.data.store.SpeedUnit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsViewModel(private val dataStore: DataStore<Preferences>) : ViewModel() {
    var darkMode by mutableStateOf(false)
        private set
    var followSystem by mutableStateOf(true)
        private set
    var speedUnit by mutableStateOf(SpeedUnit.KMH)
        private set
    var refreshTime by mutableStateOf(120)
        private set
    var powerSaving by mutableStateOf(false)
        private set
    var accelerometerEnabled by mutableStateOf(false)
        private set

    val effectiveDarkMode: Boolean
        get() = if (powerSaving) true else darkMode

    val effectiveRefreshTime: Int
        get() = if (powerSaving) refreshTime * 2 else refreshTime

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val prefs = dataStore.data.first()
            darkMode = prefs[booleanPreferencesKey("dark_mode")] ?: false
            followSystem = prefs[booleanPreferencesKey("follow_system")] ?: true
            speedUnit = SpeedUnit.valueOf(prefs[stringPreferencesKey("speed_unit")] ?: "KMH")
            refreshTime = prefs[intPreferencesKey("refresh_time")] ?: 120
            powerSaving = prefs[booleanPreferencesKey("power_saving")] ?: false
            accelerometerEnabled = prefs[booleanPreferencesKey("accelerometer_enabled")] ?: false
        }
    }

    fun updateDarkMode(value: Boolean) {
        darkMode = value
        followSystem = false
        viewModelScope.launch {
            dataStore.edit {
                it[booleanPreferencesKey("dark_mode")] = value
                it[booleanPreferencesKey("follow_system")] = false
            }
        }
    }

    fun updateFollowSystem(value: Boolean) {
        followSystem = value
        viewModelScope.launch {
            dataStore.edit { it[booleanPreferencesKey("follow_system")] = value }
        }
    }

    fun syncSystemDarkMode(systemDark: Boolean) {
        if (darkMode != systemDark) {
            darkMode = systemDark
        }
    }

    fun updateSpeedUnit(value: SpeedUnit) {
        speedUnit = value
        viewModelScope.launch {
            dataStore.edit { it[stringPreferencesKey("speed_unit")] = value.name }
        }
    }

    fun updateRefreshTime(value: Int) {
        refreshTime = value
        viewModelScope.launch {
            dataStore.edit { it[intPreferencesKey("refresh_time")] = value }
        }
    }

    fun updatePowerSaving(value: Boolean) {
        powerSaving = value
        if (value) {
            darkMode = true
            viewModelScope.launch {
                dataStore.edit {
                    it[booleanPreferencesKey("power_saving")] = true
                    it[booleanPreferencesKey("dark_mode")] = true
                }
            }
        } else {
            viewModelScope.launch {
                dataStore.edit { it[booleanPreferencesKey("power_saving")] = false }
            }
        }
    }

    fun updateAccelerometerEnabled(value: Boolean) {
        accelerometerEnabled = value
        viewModelScope.launch {
            dataStore.edit { it[booleanPreferencesKey("accelerometer_enabled")] = value }
        }
    }
}