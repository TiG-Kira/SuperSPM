package com.kira.superspm.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.Scaffold
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import com.kira.superspm.utils.UpdateChecker
import kotlinx.coroutines.launch

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
    val baseRoute = currentRoute?.split("/")?.firstOrNull() ?: currentRoute

    val backgroundColor = getPageBackgroundColor(isDark)
    val scope = rememberCoroutineScope()

    var hasUpdate by remember { mutableStateOf(false) }
    var showRedDot by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        scope.launch {
            val context = navController.context
            val currentVersion = try {
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                packageInfo?.versionName ?: "1.0.0"
            } catch (e: Exception) {
                "1.0.0"
            }
            val result = UpdateChecker.checkForUpdates(currentVersion)
            hasUpdate = result.hasUpdate
            showRedDot = result.hasUpdate
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
        NavHost(
            navController = navController,
            startDestination = "speedometer",
            modifier = Modifier.fillMaxSize()
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
                SettingsScreen(
                    isDark = isDark,
                    navController = navController,
                    hasUpdate = hasUpdate,
                    showRedDot = showRedDot,
                    onRedDotConsumed = { showRedDot = false }
                )
            }
            composable("settings/appearance") {
                AppearanceSettingsScreen(
                    isDark = isDark,
                    navController = navController
                )
            }
            composable("settings/location") {
                LocationSettingsScreen(
                    isDark = isDark,
                    navController = navController
                )
            }
            composable("settings/history") {
                HistorySettingsScreen(
                    isDark = isDark,
                    navController = navController
                )
            }
            composable("settings/lab") {
                LabSettingsScreen(
                    isDark = isDark,
                    navController = navController
                )
            }
            composable("about") {
                AboutScreen(
                    isDark = isDark,
                    hasUpdate = hasUpdate,
                    onBack = { 
                        navController.navigate("settings") {
                            popUpTo("settings") { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onOpenSourceClick = { navController.navigate("openSource") }
                )
            }
            composable("openSource") {
                OpenSourceScreen(isDark = isDark) {
                    navController.navigate("about") {
                        popUpTo("about") { inclusive = false }
                        launchSingleTop = true
                    }
                }
            }
            composable("detail/{recordId}") { backStackEntry ->
                val recordId = backStackEntry.arguments?.getString("recordId")?.toLongOrNull() ?: 0L
                DetailScreen(recordId = recordId, onBack = {
                    navController.navigate("history") {
                        popUpTo("history") { inclusive = false }
                        launchSingleTop = true
                    }
                }, isDark = isDark)
            }
        }

        if (baseRoute in listOf("speedometer", "history", "settings", "detail", "about", "openSource")) {
            NavigationBar(
                modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
            ) {
                val parentRoute = when (currentRoute) {
                    "detail" -> "history"
                    "about" -> "settings"
                    "openSource" -> "settings"
                    else -> baseRoute
                }

                NavigationBarItem(
                    selected = parentRoute == "speedometer",
                    onClick = { navController.navigate("speedometer") { launchSingleTop = true; restoreState = true } },
                    icon = Icons.Filled.Speed,
                    label = "码表"
                )
                NavigationBarItem(
                    selected = parentRoute == "history",
                    onClick = { navController.navigate("history") { launchSingleTop = true; restoreState = true } },
                    icon = Icons.Filled.History,
                    label = "历史"
                )
                NavigationBarItem(
                    selected = parentRoute == "settings",
                    onClick = { navController.navigate("settings") { launchSingleTop = true; restoreState = true } },
                    icon = Icons.Filled.Settings,
                    label = "设置"
                )
            }
        }
    }
}
