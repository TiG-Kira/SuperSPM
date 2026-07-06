package com.kira.superspm.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
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
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Extension
import top.yukonga.miuix.kmp.theme.MiuixTheme
import org.koin.androidx.compose.getViewModel

@Composable
fun LabSettingsScreen(
    isDark: Boolean = false,
    navController: NavHostController
) {
    val viewModel: SettingsViewModel = getViewModel()

    val scrollBehavior = MiuixScrollBehavior()
    val backgroundColor = getPageBackgroundColor(isDark)

    Scaffold(
        topBar = {
            TopAppBar(
                title = "实验室",
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
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "欢迎进入实验功能设置页",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MiuixTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = "本页面的功能为实验性质功能，功能上可能随版本变化有变动或调整，也有可能会在将来版本正式上线或移除。本页面的功能具有不稳定性，如果遇到 Bug，请进入 GitHub 提 Issue 反馈，感谢您的使用。",
                            style = TextStyle(
                                fontSize = 13.sp,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                            ),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            item {
                Text(
                    text = "测速",
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
                        description = "打开后可在主页面选择传感器计速方式"
                    )
                }
            }

            item {
                Text(
                    text = "扩展",
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
                        .clickable { navController.navigate("settings/lab/plugins") }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Extension,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = "插件",
                                    style = TextStyle(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MiuixTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = "导入和管理第三方插件",
                                    style = TextStyle(
                                        fontSize = 13.sp,
                                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                    )
                                )
                            }
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

            item {
                Spacer(modifier = Modifier.height(120.dp))
            }
        }
    }
}
