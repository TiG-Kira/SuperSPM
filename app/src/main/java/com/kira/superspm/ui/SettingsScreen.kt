package com.kira.superspm.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.navigation.NavHostController
import com.kira.superspm.data.store.SpeedUnit
import com.kira.superspm.viewmodel.SettingsViewModel
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowRight
import top.yukonga.miuix.kmp.theme.MiuixTheme
import org.koin.androidx.compose.getViewModel
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.ui.platform.LocalContext

@Composable
fun SettingsScreen(isDark: Boolean = false, navController: NavHostController) {
    val viewModel: SettingsViewModel = getViewModel()
    val context = LocalContext.current

    var showRefreshTimeDialog by remember { mutableStateOf(false) }
    var showSpeedUnitDialog by remember { mutableStateOf(false) }

    val scrollBehavior = MiuixScrollBehavior()
    val backgroundColor = getPageBackgroundColor(isDark)

    Scaffold(
        topBar = {
            TopAppBar(
                title = "设置",
                scrollBehavior = scrollBehavior,
                color = backgroundColor
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = PaddingValues(top = paddingValues.calculateTopPadding())
        ) {

        item {
            Text(
                text = "外观",
                style = TextStyle(
                    fontSize = 14.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                ),
                modifier = Modifier.padding(top = 16.dp, start = 16.dp, bottom = 8.dp)
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                SettingSwitchItem(
                    title = "暗色模式",
                    checked = viewModel.effectiveDarkMode,
                    onCheckedChange = { viewModel.updateDarkMode(it) },
                    enabled = !viewModel.followSystem && !viewModel.powerSaving
                )

                SettingSwitchItem(
                    title = "跟随系统",
                    checked = viewModel.followSystem,
                    onCheckedChange = { viewModel.updateFollowSystem(it) },
                    enabled = !viewModel.powerSaving
                )

                SettingClickableItem(
                    title = "速度单位",
                    value = when (viewModel.speedUnit) {
                        SpeedUnit.KMH -> "km/h"
                        SpeedUnit.MS -> "m/s"
                        SpeedUnit.MPH -> "mph"
                    },
                    onClick = { showSpeedUnitDialog = true }
                )
            }
        }

        item {
            Text(
                text = "位置",
                style = TextStyle(
                    fontSize = 14.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                ),
                modifier = Modifier.padding(top = 16.dp, start = 16.dp, bottom = 8.dp)
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                SettingClickableItem(
                    title = "自定义位置刷新时间",
                    value = "${viewModel.refreshTime} 秒",
                    onClick = { showRefreshTimeDialog = true }
                )

                SettingSwitchItem(
                    title = "省电模式",
                    checked = viewModel.powerSaving,
                    onCheckedChange = { viewModel.updatePowerSaving(it) },
                    description = if (viewModel.powerSaving) "暗色模式自动启用，刷新间隔延长" else null
                )
            }
        }

        item {
            Text(
                text = "关于",
                style = TextStyle(
                    fontSize = 14.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                ),
                modifier = Modifier.padding(top = 16.dp, start = 16.dp, bottom = 8.dp)
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                SettingClickableItem(
                    title = "关于",
                    value = "v${getVersionName(context)}",
                    onClick = { navController.navigate("about") }
                )
            }
        }

        item {
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(80.dp))
        }
        }
    }

    if (showRefreshTimeDialog) {
        DialogOverlay {
            AnimatedDialogContent {
                Card(
                    modifier = Modifier
                        .widthIn(max = 280.dp)
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "设置刷新时间",
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MiuixTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        listOf(30, 60, 120, 300).forEach { time ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                                    .clickable {
                                        viewModel.updateRefreshTime(time)
                                        showRefreshTimeDialog = false
                                    },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$time 秒",
                                    style = TextStyle(
                                        fontSize = 16.sp,
                                        color = MiuixTheme.colorScheme.onSurface
                                    )
                                )
                                if (viewModel.refreshTime == time) {
                                    Text(
                                        text = "✓",
                                        style = TextStyle(
                                            fontSize = 18.sp,
                                            color = MiuixTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = { showRefreshTimeDialog = false },
                                colors = ButtonDefaults.buttonColors(
                                    color = MiuixTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Text(text = "取消")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSpeedUnitDialog) {
        DialogOverlay {
            AnimatedDialogContent {
                Card(
                    modifier = Modifier
                        .widthIn(max = 280.dp)
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "选择速度单位",
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MiuixTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        listOf(SpeedUnit.KMH, SpeedUnit.MS, SpeedUnit.MPH).forEach { unit ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                                    .clickable {
                                        viewModel.updateSpeedUnit(unit)
                                        showSpeedUnitDialog = false
                                    },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = when (unit) {
                                        SpeedUnit.KMH -> "km/h"
                                        SpeedUnit.MS -> "m/s"
                                        SpeedUnit.MPH -> "mph"
                                    },
                                    style = TextStyle(
                                        fontSize = 16.sp,
                                        color = MiuixTheme.colorScheme.onSurface
                                    )
                                )
                                if (viewModel.speedUnit == unit) {
                                    Text(
                                        text = "✓",
                                        style = TextStyle(
                                            fontSize = 18.sp,
                                            color = MiuixTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = { showSpeedUnitDialog = false },
                                colors = ButtonDefaults.buttonColors(
                                    color = MiuixTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Text(text = "取消")
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getVersionName(context: Context): String {
    return try {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        packageInfo.versionName ?: "1.0.0"
    } catch (e: PackageManager.NameNotFoundException) {
        "1.0.0"
    }
}

@Composable
fun SettingSwitchItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    description: String? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MiuixTheme.colorScheme.onSurface
                    )
                )
                description?.let {
                    Text(
                        text = it,
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                        ),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled
            )
        }
        
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.1f))
        )
    }
}

@Composable
fun SettingClickableItem(
    title: String,
    value: String = "",
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .clickable { onClick() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MiuixTheme.colorScheme.onSurface
            )
        )
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (value.isNotEmpty()) {
                Text(
                    text = value,
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                    ),
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            Icon(
                imageVector = Icons.Filled.ArrowRight,
                contentDescription = "箭头",
                tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}