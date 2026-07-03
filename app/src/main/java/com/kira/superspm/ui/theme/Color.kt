package com.kira.superspm.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val PrimaryColor = Color(0xFF0066FF)
val SuccessColor = Color(0xFF00C853)
val WarningColor = Color(0xFFFFAB00)
val ErrorColor = Color(0xFFFF1744)

@Composable
fun getBackgroundColor(): Color =
    if (isSystemInDarkTheme()) Color(0xFF1A1A1A) else Color(0xFFF5F5F5)

@Composable
fun getSurfaceColor(): Color =
    if (isSystemInDarkTheme()) Color(0xFF242424) else Color(0xFFFFFFFF)

@Composable
fun getTextPrimaryColor(): Color =
    if (isSystemInDarkTheme()) Color(0xFFFFFFFF) else Color(0xFF1A1A1A)

@Composable
fun getTextSecondaryColor(): Color =
    if (isSystemInDarkTheme()) Color(0xFFAAAAAA) else Color(0xFF666666)