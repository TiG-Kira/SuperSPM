package com.kira.superspm.ui

import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.foundation.shape.CircleShape
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
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Extension
import top.yukonga.miuix.kmp.theme.MiuixTheme
import org.koin.androidx.compose.getViewModel
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun SettingsScreen(
    isDark: Boolean = false,
    navController: NavHostController,
    hasUpdate: Boolean = false,
    showRedDot: Boolean = false,
    onRedDotConsumed: () -> Unit = {}
) {
    val viewModel: SettingsViewModel = getViewModel()

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
                SettingNavItem(
                    icon = Icons.Filled.Palette,
                    title = "外观",
                    subtitle = "暗色模式、速度单位显示",
                    onClick = { navController.navigate("settings/appearance") }
                )
            }

            item {
                SettingNavItem(
                    icon = Icons.Filled.Speed,
                    title = "测速方式和逻辑",
                    subtitle = "位置刷新、省电模式、传感器计速",
                    onClick = { navController.navigate("settings/location") }
                )
            }

            item {
                SettingNavItem(
                    icon = Icons.Filled.History,
                    title = "历史记录",
                    subtitle = "记录管理、数据统计",
                    onClick = { navController.navigate("settings/history") }
                )
            }

            item {
                SettingNavItem(
                    icon = Icons.Filled.Extension,
                    title = "插件管理",
                    subtitle = "导入、启用、删除插件",
                    onClick = { navController.navigate("settings/plugins") }
                )
            }

            item {
                SettingNavItem(
                    icon = Icons.Filled.Info,
                    title = "关于",
                    subtitle = if (hasUpdate) "发现新版本" else "应用信息、开源项目",
                    hasRedDot = hasUpdate && showRedDot,
                    onClick = {
                        onRedDotConsumed()
                        navController.navigate("about")
                    }
                )
            }

            item {
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(120.dp))
            }
        }
    }
}

@Composable
fun SettingNavItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    hasRedDot: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MiuixTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Column(
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Text(
                        text = title,
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MiuixTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = subtitle,
                        style = TextStyle(
                            fontSize = 13.sp,
                            color = if (subtitle == "发现新版本") MiuixTheme.colorScheme.error else MiuixTheme.colorScheme.onSurfaceVariantSummary
                        ),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (hasRedDot) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color(0xFFFF1744), CircleShape)
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))
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
}
