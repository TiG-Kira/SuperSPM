package com.kira.superspm.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import top.yukonga.miuix.kmp.theme.MiuixTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.foundation.background

@Composable
fun getPageBackgroundColor(isDark: Boolean): Color {
    return if (isDark) {
        Color(0xFF000000)
    } else {
        Color(0xFFF7F7F7)
    }
}

@Composable
fun AppNavHost(
    hasLocationPermission: Boolean,
    onPermissionRequest: () -> Unit,
    isDark: Boolean
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val backgroundColor = getPageBackgroundColor(isDark)

    Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
        NavHost(
            navController = navController,
            startDestination = "speedometer",
            modifier = Modifier.fillMaxSize().padding(bottom = 80.dp)
        ) {
            composable("speedometer") {
                SpeedometerScreen(
                    hasLocationPermission = hasLocationPermission,
                    onPermissionRequest = onPermissionRequest,
                    isDark = isDark
                )
            }
            composable("history") {
                HistoryScreen(recordClick = { recordId ->
                    navController.navigate("detail/$recordId")
                }, isDark = isDark)
            }
            composable("settings") {
                SettingsScreen(isDark = isDark, navController = navController)
            }
            composable("about") {
                AboutScreen(
                    isDark = isDark,
                    onBack = { navController.popBackStack() },
                    onOpenSourceClick = { navController.navigate("openSource") }
                )
            }
            composable("openSource") {
                OpenSourceScreen(isDark = isDark) {
                    navController.popBackStack()
                }
            }
            composable("detail/{recordId}") { backStackEntry ->
                val recordId = backStackEntry.arguments?.getString("recordId")?.toLongOrNull() ?: 0L
                DetailScreen(recordId = recordId, onBack = {
                    navController.popBackStack()
                }, isDark = isDark)
            }
        }

        if (currentRoute in listOf("speedometer", "history", "settings")) {
            NavigationBar(
                modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
                containerColor = backgroundColor,
                contentColor = MiuixTheme.colorScheme.onSurface
            ) {
                NavigationBarItem(
                    selected = currentRoute == "speedometer",
                    onClick = { navController.navigate("speedometer") { launchSingleTop = true; restoreState = true } },
                    icon = { SpeedometerIcon(color = if (currentRoute == "speedometer") MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurfaceVariantSummary) },
                    label = { androidx.compose.material3.Text("码表") },
                    colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                        selectedIconColor = MiuixTheme.colorScheme.primary,
                        selectedTextColor = MiuixTheme.colorScheme.primary,
                        indicatorColor = if (isDark) Color(0xFF000000) else Color(0xFFf7f7f7),
                        unselectedIconColor = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        unselectedTextColor = MiuixTheme.colorScheme.onSurfaceVariantSummary
                    )
                )
                NavigationBarItem(
                    selected = currentRoute == "history",
                    onClick = { navController.navigate("history") { launchSingleTop = true; restoreState = true } },
                    icon = { HistoryIcon(color = if (currentRoute == "history") MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurfaceVariantSummary) },
                    label = { androidx.compose.material3.Text("历史") },
                    colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                        selectedIconColor = MiuixTheme.colorScheme.primary,
                        selectedTextColor = MiuixTheme.colorScheme.primary,
                        indicatorColor = if (isDark) Color(0xFF000000) else Color(0xFFf7f7f7),
                        unselectedIconColor = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        unselectedTextColor = MiuixTheme.colorScheme.onSurfaceVariantSummary
                    )
                )
                NavigationBarItem(
                    selected = currentRoute == "settings",
                    onClick = { navController.navigate("settings") { launchSingleTop = true; restoreState = true } },
                    icon = { SettingsIcon(color = if (currentRoute == "settings") MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurfaceVariantSummary) },
                    label = { androidx.compose.material3.Text("设置") },
                    colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                        selectedIconColor = MiuixTheme.colorScheme.primary,
                        selectedTextColor = MiuixTheme.colorScheme.primary,
                        indicatorColor = if (isDark) Color(0xFF000000) else Color(0xFFf7f7f7),
                        unselectedIconColor = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        unselectedTextColor = MiuixTheme.colorScheme.onSurfaceVariantSummary
                    )
                )
            }
        }
    }
}

@Composable
fun SpeedometerIcon(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val size = size.width
        val centerX = size / 2
        val centerY = size / 2
        val radius = size / 2 - 4f
        
        drawCircle(color = color, radius = radius, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.5f), center = androidx.compose.ui.geometry.Offset(centerX, centerY))
        drawLine(color = color, start = androidx.compose.ui.geometry.Offset(centerX, centerY - radius + 6f), end = androidx.compose.ui.geometry.Offset(centerX, centerY - radius + 2f), strokeWidth = 3.5f)
        drawLine(color = color, start = androidx.compose.ui.geometry.Offset(centerX, centerY + radius - 6f), end = androidx.compose.ui.geometry.Offset(centerX, centerY + radius - 2f), strokeWidth = 3.5f)
        drawLine(color = color, start = androidx.compose.ui.geometry.Offset(centerX - radius + 6f, centerY), end = androidx.compose.ui.geometry.Offset(centerX - radius + 2f, centerY), strokeWidth = 3.5f)
        drawLine(color = color, start = androidx.compose.ui.geometry.Offset(centerX + radius - 6f, centerY), end = androidx.compose.ui.geometry.Offset(centerX + radius - 2f, centerY), strokeWidth = 3.5f)
        drawCircle(color = color, radius = 4f, center = androidx.compose.ui.geometry.Offset(centerX, centerY))
    }
}

@Composable
fun HistoryIcon(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val size = size.width
        val centerX = size / 2
        val centerY = size / 2
        val radius = size / 2 - 4f
        
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(4f, 4f),
            size = androidx.compose.ui.geometry.Size(size - 8f, size - 8f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.5f)
        )
        drawLine(color = color, start = androidx.compose.ui.geometry.Offset(centerX, centerY - radius + 4f), end = androidx.compose.ui.geometry.Offset(centerX, centerY + 2f), strokeWidth = 3.5f)
        drawLine(color = color, start = androidx.compose.ui.geometry.Offset(centerX, centerY + 2f), end = androidx.compose.ui.geometry.Offset(centerX + 6f, centerY + 8f), strokeWidth = 3.5f)
    }
}

@Composable
fun SettingsIcon(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val size = size.width
        val centerX = size / 2
        val centerY = size / 2
        val innerRadius = size / 3
        val outerRadius = size / 2 - 2f
        
        drawCircle(color = color, radius = innerRadius, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.5f), center = androidx.compose.ui.geometry.Offset(centerX, centerY))
        for (i in 0..7) {
            val angle = (i * 45f).toDouble() * Math.PI / 180.0
            val x1 = (centerX + innerRadius * kotlin.math.cos(angle)).toFloat()
            val y1 = (centerY + innerRadius * kotlin.math.sin(angle)).toFloat()
            val x2 = (centerX + outerRadius * kotlin.math.cos(angle)).toFloat()
            val y2 = (centerY + outerRadius * kotlin.math.sin(angle)).toFloat()
            drawLine(color = color, start = androidx.compose.ui.geometry.Offset(x1, y1), end = androidx.compose.ui.geometry.Offset(x2, y2), strokeWidth = 3.5f)
        }
    }
}