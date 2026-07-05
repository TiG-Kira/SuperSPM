package com.kira.superspm.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun OpenSourceScreen(isDark: Boolean, onBack: () -> Unit) {
    val context = LocalContext.current
    val scrollBehavior = MiuixScrollBehavior()
    val backgroundColor = getPageBackgroundColor(isDark)

    Scaffold(
        topBar = {
            TopAppBar(
                title = "使用的开源项目",
                scrollBehavior = scrollBehavior,
                color = backgroundColor,
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .clickable { onBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = MiuixTheme.colorScheme.onSurface,
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
            contentPadding = PaddingValues(top = paddingValues.calculateTopPadding(), bottom = 120.dp)
        ) {
            item {
                OpenSourceCard(
                    title = "MiuiX",
                    description = "Compose MiuiX UI库",
                    url = "https://github.com/compose-miuix-ui/miuix",
                    context = context
                )
            }

            item {
                OpenSourceCard(
                    title = "HyperCeiler",
                    description = "项目部分UI排版与设置参考",
                    url = "https://github.com/ReChronoRain/HyperCeiler",
                    context = context
                )
            }

            item {
                OpenSourceCard(
                    title = "Jetpack Compose",
                    description = "Android Jetpack Compose UI框架",
                    url = "https://developer.android.com/jetpack/compose",
                    context = context
                )
            }

            item {
                OpenSourceCard(
                    title = "Room",
                    description = "Android Room 数据库",
                    url = "https://developer.android.com/jetpack/androidx/releases/room",
                    context = context
                )
            }

            item {
                OpenSourceCard(
                    title = "Koin",
                    description = "依赖注入框架",
                    url = "https://insert-koin.io/",
                    context = context
                )
            }

            item {
                OpenSourceCard(
                    title = "Coil",
                    description = "图像加载库",
                    url = "https://coil-kt.github.io/coil/",
                    context = context
                )
            }

            item {
                OpenSourceCard(
                    title = "Timber",
                    description = "日志库",
                    url = "https://github.com/JakeWharton/timber",
                    context = context
                )
            }

            item {
                OpenSourceCard(
                    title = "Kotlin Serialization",
                    description = "Kotlin 序列化库",
                    url = "https://github.com/Kotlin/kotlinx.serialization",
                    context = context
                )
            }

            item {
                OpenSourceCard(
                    title = "Kotlinx Datetime",
                    description = "Kotlin 日期时间库",
                    url = "https://github.com/Kotlin/kotlinx-datetime",
                    context = context
                )
            }

            item {
                OpenSourceCard(
                    title = "Google Play Services Location",
                    description = "Google 位置服务，针对可以使用 Google 位置的机型，测速计使用此服务定位当前位置",
                    url = "https://developers.google.com/android/reference/com/google/android/gms/location/package-summary",
                    context = context
                )
            }
        }
    }
}

@Composable
fun OpenSourceCard(
    title: String,
    description: String,
    url: String,
    context: android.content.Context
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MiuixTheme.colorScheme.onSurface
                ),
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = description,
                style = TextStyle(
                    fontSize = 13.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "点击进入项目主页",
                style = TextStyle(
                    fontSize = 13.sp,
                    color = MiuixTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .clickable {
                        val intent = android.content.Intent(
                            android.content.Intent.ACTION_VIEW,
                            android.net.Uri.parse(url)
                        )
                        context.startActivity(intent)
                    }
            )
        }
    }
}
