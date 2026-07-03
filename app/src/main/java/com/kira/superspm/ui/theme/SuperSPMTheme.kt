package com.kira.superspm.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController

@Composable
fun SuperSPMTheme(
    darkMode: Boolean = false,
    followSystem: Boolean = true,
    content: @Composable () -> Unit
) {
    val isDark = if (followSystem) {
        isSystemInDarkTheme()
    } else {
        darkMode
    }
    val mode = if (isDark) ColorSchemeMode.Dark else ColorSchemeMode.Light
    MiuixTheme(
        controller = ThemeController(colorSchemeMode = mode),
        content = content
    )
}
