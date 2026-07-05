package com.kira.superspm.ui.theme

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
    val mode = if (darkMode) ColorSchemeMode.Dark else ColorSchemeMode.Light
    MiuixTheme(
        controller = ThemeController(colorSchemeMode = mode, isDark = darkMode),
        content = content
    )
}
