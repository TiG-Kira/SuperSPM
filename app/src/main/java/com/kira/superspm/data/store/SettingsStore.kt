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
    val NAV_BAR_STYLE = intPreferencesKey("nav_bar_style")
}

enum class SpeedUnit {
    KMH, MS, MPH
}

/**
 * 底部导航栏样式（对齐 KiTerminal-UX）。
 *
 * GLASS / LIQUID 都依赖 RuntimeShader（Android 13 / API 33 起才有），
 * 低版本设备一律按 CLASSIC 渲染，即使设置里存的是玻璃也自动降级。
 */
enum class NavBarStyle {
    /** 经典 miuix NavigationBar，贴底、无悬浮 */
    CLASSIC,

    /** miuix-glass 悬浮玻璃导航栏 */
    GLASS,

    /** 液态玻璃导航栏（kyant0 backdrop + Capsule 折射指示器） */
    LIQUID
}