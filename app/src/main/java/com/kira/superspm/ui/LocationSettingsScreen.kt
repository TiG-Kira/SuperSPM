package com.kira.superspm.ui

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.navigation.NavHostController
import com.kira.superspm.viewmodel.SettingsViewModel
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Speed
import top.yukonga.miuix.kmp.theme.MiuixTheme
import org.koin.androidx.compose.getViewModel

@Composable
fun LocationSettingsScreen(
    isDark: Boolean = false,
    navController: NavHostController
) {
    val viewModel: SettingsViewModel = getViewModel()

    var showRefreshTimeDialog by remember { mutableStateOf(false) }

    val scrollBehavior = MiuixScrollBehavior()
    val backgroundColor = getPageBackgroundColor(isDark)

    Scaffold(
        topBar = {
            TopAppBar(
                title = "测速方式和逻辑",
                scrollBehavior = scrollBehavior,
                color = backgroundColor,
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .clickable { navController.popBackStack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = if (isDark) Color.White else Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
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
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Speed,
                            contentDescription = null,
                            tint = MiuixTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                        Column(
                            modifier = Modifier.padding(start = 16.dp)
                        ) {
                            Text(
                                text = "本页说明",
                                style = TextStyle(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MiuixTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = "本页面可对测速方式和逻辑进行修改。包括位置刷新时间设置、省电模式开关、传感器计速开关。",
                                style = TextStyle(
                                    fontSize = 13.sp,
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                ),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "位置刷新",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                    ),
                    modifier = Modifier.padding(top = 16.dp, start = 16.dp, bottom = 8.dp)
                )
            }

            item {
                SettingClickableItem(
                    title = "自定义位置刷新时间",
                    value = if (viewModel.refreshTime == 0) "实时" else "${viewModel.refreshTime} 秒",
                    onClick = { showRefreshTimeDialog = true }
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    SettingSwitchItem(
                        title = "省电模式",
                        checked = viewModel.powerSaving,
                        onCheckedChange = { viewModel.updatePowerSaving(it) },
                        description = when {
                            viewModel.accelerometerEnabled -> "省电模式只能在 GPS 模式应用，打开后，将对 GPS 采集方式与间隔作出限制，对传感器采集方式无效"
                            viewModel.powerSaving -> "暗色模式将自动启用（重启后生效），GPS 位置与速度采集刷新间隔延长"
                            else -> null
                        }
                    )
                }
            }

            item {
                Text(
                    text = "传感器测速",
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
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    SettingSwitchItem(
                        title = "启用传感器计速",
                        checked = viewModel.accelerometerEnabled,
                        onCheckedChange = { viewModel.updateAccelerometerEnabled(it) },
                        description = "启用选择传感器计速方式，开启后，可在主页中可选择使用传感器测速"
                    )
                }
            }

            item {
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(120.dp))
            }
        }
    }

    if (showRefreshTimeDialog) {
        Dialog(
            onDismissRequest = { showRefreshTimeDialog = false }
        ) {
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "设置位置刷新时间",
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MiuixTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Column(
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        listOf(0, 30, 60, 120, 300).forEach { time ->
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
                                    text = if (time == 0) "实时" else "$time 秒",
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
                    }
                    Button(
                        onClick = { showRefreshTimeDialog = false },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            color = MiuixTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(text = "取消", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
