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
import androidx.compose.ui.unit.IntOffset
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
import androidx.compose.material.icons.filled.Extension
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.compose.ui.platform.LocalContext
import com.kira.superspm.R
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
    val context = LocalContext.current
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val baseRoute = currentRoute?.split("/")?.firstOrNull() ?: currentRoute

    val backgroundColor = getPageBackgroundColor(isDark)
    val scope = rememberCoroutineScope()

    var hasUpdate by remember { mutableStateOf(false) }
    var showRedDot by remember { mutableStateOf(false) }

    val mainRoutes = setOf("speedometer", "plugins", "history", "settings")

    fun enterTransition(initialRoute: String?, targetRoute: String?): EnterTransition {
        val isMainToSub = initialRoute in mainRoutes && targetRoute !in mainRoutes
        val isMainToMain = initialRoute in mainRoutes && targetRoute in mainRoutes
        return when {
            isMainToSub -> slideInHorizontally(
                animationSpec = tween(300, easing = androidx.compose.animation.core.EaseOutQuad),
                initialOffsetX = { it }
            ) + fadeIn(animationSpec = tween(300))
            isMainToMain -> slideInHorizontally(
                animationSpec = tween(150, easing = androidx.compose.animation.core.EaseOutQuad),
                initialOffsetX = { if (targetRoute != null && mainRoutes.indexOf(targetRoute) > mainRoutes.indexOf(initialRoute)) it else -it }
            )
            else -> EnterTransition.None
        }
    }

    fun exitTransition(initialRoute: String?, targetRoute: String?): ExitTransition {
        val isMainToSub = initialRoute in mainRoutes && targetRoute !in mainRoutes
        val isMainToMain = initialRoute in mainRoutes && targetRoute in mainRoutes
        return when {
            isMainToSub -> fadeOut(animationSpec = tween(200))
            isMainToMain -> slideOutHorizontally(
                animationSpec = tween(150, easing = androidx.compose.animation.core.EaseOutQuad),
                targetOffsetX = { if (targetRoute != null && mainRoutes.indexOf(targetRoute) > mainRoutes.indexOf(initialRoute)) -it else it }
            )
            else -> ExitTransition.None
        }
    }

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
            modifier = Modifier.fillMaxSize(),
            enterTransition = { enterTransition(initialState?.destination?.route, targetState?.destination?.route) },
            exitTransition = { exitTransition(initialState?.destination?.route, targetState?.destination?.route) },
            popEnterTransition = {
                slideInHorizontally(
                    animationSpec = tween(250, easing = androidx.compose.animation.core.EaseOutQuad),
                    initialOffsetX = { -it }
                ) + fadeIn(animationSpec = tween(250))
            },
            popExitTransition = {
                slideOutHorizontally(
                    animationSpec = tween(250, easing = androidx.compose.animation.core.EaseOutQuad),
                    targetOffsetX = { it }
                ) + fadeOut(animationSpec = tween(250))
            }
        ) {
            composable("speedometer") {
                SpeedometerScreen(
                    hasLocationPermission = hasLocationPermission,
                    onPermissionRequest = onPermissionRequest,
                    isDark = isDark
                )
            }
            composable("plugins") {
                PluginHomeScreen(
                    isDark = isDark,
                    navController = navController
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
            composable("settings/plugins") {
                PluginScreen(
                    isDark = isDark,
                    navController = navController
                )
            }
            composable("settings/plugins/templates") {
                PluginTemplateScreen(
                    isDark = isDark,
                    navController = navController
                )
            }
            composable("plugin_web/{dirName}/{pluginName}") { backStackEntry ->
                val dirName = backStackEntry.arguments?.getString("dirName") ?: ""
                val pluginName = backStackEntry.arguments?.getString("pluginName") ?: context.getString(R.string.plugin)
                PluginWebPage(
                    pluginDirName = dirName,
                    pluginName = pluginName,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("plugin_native/{dirName}/{pluginName}") { backStackEntry ->
                val dirName = backStackEntry.arguments?.getString("dirName") ?: ""
                val pluginName = backStackEntry.arguments?.getString("pluginName") ?: context.getString(R.string.plugin)
                PluginNativePage(
                    pluginDirName = dirName,
                    pluginName = pluginName,
                    onBack = { navController.popBackStack() }
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

        if (baseRoute in listOf("speedometer", "plugins", "history", "settings", "detail", "about", "openSource", "plugin_web", "plugin_native")) {
            NavigationBar(
                modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
            ) {
                val parentRoute = when (baseRoute) {
                    "detail" -> "history"
                    "about" -> "settings"
                    "openSource" -> "settings"
                    "plugin_web" -> "plugins"
                    "plugin_native" -> "plugins"
                    "settings" -> "settings"
                    "plugins" -> "plugins"
                    else -> baseRoute
                }

                NavigationBarItem(
                    selected = parentRoute == "speedometer",
                    onClick = { navController.navigate("speedometer") { launchSingleTop = true; restoreState = true } },
                    icon = Icons.Filled.Speed,
                    label = context.getString(R.string.tab_speedometer)
                )
                NavigationBarItem(
                    selected = parentRoute == "plugins",
                    onClick = { navController.navigate("plugins") { launchSingleTop = true; restoreState = true } },
                    icon = Icons.Filled.Extension,
                    label = context.getString(R.string.tab_plugins)
                )
                NavigationBarItem(
                    selected = parentRoute == "history",
                    onClick = { navController.navigate("history") { launchSingleTop = true; restoreState = true } },
                    icon = Icons.Filled.History,
                    label = context.getString(R.string.tab_history)
                )
                NavigationBarItem(
                    selected = parentRoute == "settings",
                    onClick = { navController.navigate("settings") { launchSingleTop = true; restoreState = true } },
                    icon = Icons.Filled.Settings,
                    label = context.getString(R.string.tab_settings)
                )
            }
        }
    }
}
