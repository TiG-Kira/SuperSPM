package com.kira.superspm.ui

import android.os.Build
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
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kira.superspm.data.store.NavBarStyle
import com.kira.superspm.ui.glass.LiquidGlassNavigationBarItem
import com.kira.superspm.ui.glass.LiquidGlassNavigationBarWithIndicator
import com.kira.superspm.ui.glass.NavStyle
import com.kira.superspm.ui.glass.computeNavDimensions
import com.kira.superspm.viewmodel.SettingsViewModel
import org.koin.androidx.compose.getViewModel
import top.yukonga.miuix.kmp.blur.layerBackdrop as miuixLayerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop as rememberMiuixLayerBackdrop
import top.yukonga.miuix.kmp.glass.GlassNavigationBar
import top.yukonga.miuix.kmp.glass.GlassNavigationItem
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

    // ---- 悬浮玻璃底栏（对齐 KiTerminal-UX）----
    // 玻璃/液态都依赖 RuntimeShader，Android 13（API 33）起才可用；低版本一律走经典底栏。
    val settingsViewModel: SettingsViewModel = getViewModel()
    val navBarStyle = settingsViewModel.navBarStyle
    val glassSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    val useGlassNav = glassSupported && navBarStyle == NavBarStyle.GLASS
    val useLiquidNav = glassSupported && navBarStyle == NavBarStyle.LIQUID
    // 取景层：页面内容先被捕获进 backdrop，导航栏再折射它。仅在对应样式下创建，
    // 避免低版本设备加载 blur/shader 相关类。
    val liquidBackdrop = if (useLiquidNav) rememberLayerBackdrop() else null
    val glassNavBackdrop = if (useGlassNav) rememberMiuixLayerBackdrop() else null

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
        // 页面内容层：玻璃底栏靠它提供折射素材，因此单独包一层挂取景修饰符
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    when {
                        useLiquidNav && liquidBackdrop != null -> Modifier.layerBackdrop(liquidBackdrop)
                        useGlassNav && glassNavBackdrop != null -> Modifier.miuixLayerBackdrop(glassNavBackdrop)
                        else -> Modifier
                    }
                )
        ) {
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
        }

        if (baseRoute in listOf("speedometer", "plugins", "history", "settings", "detail", "about", "openSource", "plugin_web", "plugin_native")) {
            val tabRoutes = listOf("speedometer", "plugins", "history", "settings")
            val tabIcons = listOf(
                Icons.Filled.Speed,
                Icons.Filled.Extension,
                Icons.Filled.History,
                Icons.Filled.Settings
            )
            val tabLabels = listOf(
                context.getString(R.string.tab_speedometer),
                context.getString(R.string.tab_plugins),
                context.getString(R.string.tab_history),
                context.getString(R.string.tab_settings)
            )
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
            val selectedIndex = tabRoutes.indexOf(parentRoute).coerceAtLeast(0)

            fun navigateTo(route: String) {
                navController.navigate(route) { launchSingleTop = true; restoreState = true }
            }

            when {
                useLiquidNav && liquidBackdrop != null -> {
                    val dims = computeNavDimensions(tabRoutes.size, NavStyle.GLASS)
                    LiquidGlassNavigationBarWithIndicator(
                        selectedIndex = selectedIndex,
                        itemCount = tabRoutes.size,
                        backdrop = liquidBackdrop,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                        onIndexChange = { navigateTo(tabRoutes[it]) }
                    ) { onPositioned ->
                        tabRoutes.forEachIndexed { index, route ->
                            LiquidGlassNavigationBarItem(
                                icon = tabIcons[index],
                                label = tabLabels[index],
                                selected = selectedIndex == index,
                                onClick = { navigateTo(route) },
                                dims = dims,
                                index = index,
                                onPositioned = onPositioned
                            )
                        }
                    }
                }

                useGlassNav && glassNavBackdrop != null -> {
                    GlassNavigationBar(
                        items = tabRoutes.mapIndexed { index, _ ->
                            GlassNavigationItem(icon = tabIcons[index], label = tabLabels[index])
                        },
                        selectedIndex = selectedIndex,
                        onSelect = { navigateTo(tabRoutes[it]) },
                        backdrop = glassNavBackdrop,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(start = 24.dp, end = 24.dp, bottom = 16.dp)
                    )
                }

                else -> {
                    NavigationBar(
                        modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
                    ) {
                        NavigationBarItem(
                            selected = selectedIndex == 0,
                            onClick = { navigateTo("speedometer") },
                            icon = tabIcons[0],
                            label = tabLabels[0]
                        )
                        NavigationBarItem(
                            selected = selectedIndex == 1,
                            onClick = { navigateTo("plugins") },
                            icon = tabIcons[1],
                            label = tabLabels[1]
                        )
                        NavigationBarItem(
                            selected = selectedIndex == 2,
                            onClick = { navigateTo("history") },
                            icon = tabIcons[2],
                            label = tabLabels[2]
                        )
                        NavigationBarItem(
                            selected = selectedIndex == 3,
                            onClick = { navigateTo("settings") },
                            icon = tabIcons[3],
                            label = tabLabels[3]
                        )
                    }
                }
            }
        }
    }
}
