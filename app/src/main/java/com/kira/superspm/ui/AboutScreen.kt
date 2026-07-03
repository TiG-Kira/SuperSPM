package com.kira.superspm.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowRight
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AboutScreen(isDark: Boolean, onBack: () -> Unit, onOpenSourceClick: () -> Unit) {
    val context = LocalContext.current
    val scrollBehavior = MiuixScrollBehavior()
    val scope = rememberCoroutineScope()

    var gradientOffset by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        scope.launch {
            while (true) {
                gradientOffset += 0.002f
                if (gradientOffset > 1f) gradientOffset = 0f
                delay(16)
            }
        }
    }

    val gradientColors = if (isDark) {
        listOf(
            Color(0xFF2d1b4e),
            Color(0xFF1a1a2e),
            Color(0xFF0f3460),
            Color(0xFF16213e)
        )
    } else {
        listOf(
            Color(0xFFfce7f3),
            Color(0xFFe0e7ff),
            Color(0xFFc7d2fe),
            Color(0xFFfbcfe8)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = gradientColors,
                    startY = gradientOffset * 2000f,
                    endY = gradientOffset * 2000f + 1000f
                )
            )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = "关于",
                    scrollBehavior = scrollBehavior,
                    color = Color.Transparent,
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
                    .padding(top = paddingValues.calculateTopPadding()),
                verticalArrangement = Arrangement.Top
            ) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Spacer(modifier = Modifier.height(40.dp))
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF6600)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "S",
                                    style = TextStyle(
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "SuperSPM",
                            style = TextStyle(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color.Black
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = getVersionName(context),
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = if (isDark) Color.Gray else Color.DarkGray
                            )
                        )
                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            InfoRow(
                                title = "设备型号",
                                value = android.os.Build.MODEL
                            )
                            InfoRow(
                                title = "Android 版本",
                                value = android.os.Build.VERSION.RELEASE
                            )
                            InfoRow(
                                title = "内核版本",
                                value = android.os.Build.DISPLAY
                            )
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clickable {
                                val intent = android.content.Intent(
                                    android.content.Intent.ACTION_VIEW,
                                    android.net.Uri.parse("https://github.com/tig-kira")
                                )
                                context.startActivity(intent)
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                AsyncImage(
                                    model = "https://avatars.githubusercontent.com/u/297331574",
                                    contentDescription = "开发者头像",
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                )
                                Column(
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "极犽",
                                        style = TextStyle(
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MiuixTheme.colorScheme.onSurface
                                        )
                                    )
                                    Text(
                                        text = "@TiG-Kira",
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
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clickable(onClick = onOpenSourceClick)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "使用的开源项目",
                                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            )
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
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            InfoRow(
                                title = "SuperSPM 版本",
                                value = getVersionName(context)
                            )
                            Text(
                                text = "SuperSPM 项目使用了 Trae 辅助生成了部分 AI 代码。",
                                style = TextStyle(
                                    fontSize = 13.sp,
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                ),
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = "用户界面采用 MiuiX 设计，详情请查看使用的开源项目。此应用遵循 AGPL-3.0 许可以及 MIT 许可。",
                                style = TextStyle(
                                    fontSize = 13.sp,
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(title: String, value: String) {
    Column(
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = value,
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MiuixTheme.colorScheme.onSurface
            )
        )
        Text(
            text = title,
            style = TextStyle(
                fontSize = 12.sp,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
            ),
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

private fun getVersionName(context: android.content.Context): String {
    return try {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        packageInfo?.versionName ?: "1.0.0"
    } catch (e: android.content.pm.PackageManager.NameNotFoundException) {
        "1.0.0"
    }
}
