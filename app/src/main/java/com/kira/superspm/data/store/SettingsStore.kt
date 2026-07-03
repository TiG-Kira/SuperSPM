package com.kira.superspm.data.store

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object SettingsKeys {
    val DARK_MODE = booleanPreferencesKey("dark_mode")
    val FOLLOW_SYSTEM = booleanPreferencesKey("follow_system")
    val SPEED_UNIT = stringPreferencesKey("speed_unit")
    val REFRESH_TIME = intPreferencesKey("refresh_time")
    val POWER_SAVING = booleanPreferencesKey("power_saving")
}

enum class SpeedUnit {
    KMH, MS, MPH
}