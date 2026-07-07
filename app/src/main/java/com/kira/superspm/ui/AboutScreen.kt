package com.kira.superspm.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.kira.superspm.R
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
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
import com.kira.superspm.utils.UpdateChecker
import com.kira.superspm.utils.ApkDownloader

@Composable
fun AboutScreen(isDark: Boolean, hasUpdate: Boolean = false, onBack: () -> Unit, onOpenSourceClick: () -> Unit) {
    val context = LocalContext.current
    val scrollBehavior = MiuixScrollBehavior()
    val scope = rememberCoroutineScope()

    var gradientOffset by remember { mutableFloatStateOf(0f) }
    var checkingUpdate by remember { mutableStateOf(false) }
    var updateResult by remember { mutableStateOf<UpdateChecker.UpdateResult?>(null) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var downloadingApk by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0) }
    var downloadedBytes by remember { mutableStateOf(0L) }
    var totalBytes by remember { mutableStateOf(0L) }
    var pendingInstallVersion by remember { mutableStateOf<String?>(null) }

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

    val backgroundColor = getPageBackgroundColor(isDark)

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
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                contentPadding = PaddingValues(
                    top = paddingValues.calculateTopPadding(),
                    bottom = 80.dp
                ),
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
                                .background(Color(0xFFFF69B4)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                contentDescription = "Logo",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(60.dp)
                            )
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
                            .clip(RoundedCornerShape(20.dp))
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
                            .clip(RoundedCornerShape(20.dp))
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
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {
                                if (!checkingUpdate) {
                                    checkingUpdate = true
                                    scope.launch {
                                        updateResult = UpdateChecker.checkForUpdates(getVersionName(context))
                                        showUpdateDialog = true
                                        checkingUpdate = false
                                    }
                                }
                            }
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
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (checkingUpdate) {
                                    androidx.compose.material3.CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = MiuixTheme.colorScheme.primary
                                    )
                                }
                                Column {
                                    Text(
                                        text = "检查更新",
                                        style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    )
                                    if (hasUpdate) {
                                        Text(
                                            text = "有新版本",
                                            style = TextStyle(
                                                fontSize = 13.sp,
                                                color = MiuixTheme.colorScheme.error
                                            )
                                        )
                                    }
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

    if (showUpdateDialog && updateResult != null) {
        if (pendingInstallVersion != null && ApkDownloader.hasInstallPermission(context)) {
            val apkFile = ApkDownloader.getDownloadedApkFile(context, pendingInstallVersion!!)
            if (apkFile.exists()) {
                LaunchedEffect(Unit) {
                    ApkDownloader.installApk(context, apkFile)
                    pendingInstallVersion = null
                    showUpdateDialog = false
                }
            } else {
                pendingInstallVersion = null
            }
        }

        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showUpdateDialog = false }
        ) {
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (updateResult!!.hasUpdate) "发现新版本" else "已是最新版本",
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MiuixTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = if (updateResult!!.hasUpdate) {
                            "当前版本: v${updateResult!!.currentVersion}\n最新版本: v${updateResult!!.latestVersion}"
                        } else {
                            "当前版本: v${updateResult!!.currentVersion} 已是最新"
                        },
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                        ),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    if (updateResult!!.hasUpdate && updateResult!!.releaseNotes.isNotBlank()) {
                        Text(
                            text = "更新日志",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MiuixTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        MarkdownText(
                            text = updateResult!!.releaseNotes,
                            modifier = Modifier
                                .padding(bottom = 16.dp)
                                .heightIn(max = 200.dp)
                                .verticalScroll(androidx.compose.foundation.rememberScrollState())
                        )
                    }
                    if (downloadingApk) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "下载中...",
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                    )
                                )
                                Text(
                                    text = "$downloadProgress%",
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MiuixTheme.colorScheme.primary
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            androidx.compose.foundation.layout.Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(MiuixTheme.colorScheme.surfaceVariant)
                            ) {
                                androidx.compose.foundation.layout.Box(
                                    modifier = Modifier
                                        .fillMaxWidth(downloadProgress / 100f)
                                        .height(6.dp)
                                        .background(MiuixTheme.colorScheme.primary)
                                )
                            }
                            if (totalBytes > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${formatUpdateFileSize(downloadedBytes)} / ${formatUpdateFileSize(totalBytes)}",
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                    )
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (updateResult!!.hasUpdate) Arrangement.spacedBy(8.dp) else Arrangement.Center
                    ) {
                        if (updateResult!!.hasUpdate) {
                            Button(
                                onClick = { showUpdateDialog = false },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    color = MiuixTheme.colorScheme.surfaceVariant
                                ),
                                enabled = !downloadingApk
                            ) {
                                Text(text = "稍后", fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    val intent = android.content.Intent(
                                        android.content.Intent.ACTION_VIEW,
                                        android.net.Uri.parse(updateResult!!.releaseUrl)
                                    )
                                    context.startActivity(intent)
                                    showUpdateDialog = false
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    color = MiuixTheme.colorScheme.surfaceVariant
                                ),
                                enabled = !downloadingApk
                            ) {
                                Text(text = "手动", fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    downloadingApk = true
                                    downloadProgress = 0
                                    downloadedBytes = 0L
                                    totalBytes = 0L
                                    scope.launch {
                                        val result = ApkDownloader.downloadAndInstall(
                                            context,
                                            updateResult!!.apkDownloadUrl,
                                            updateResult!!.latestVersion
                                        ) { progress, downloaded, total ->
                                            downloadProgress = progress
                                            downloadedBytes = downloaded
                                            totalBytes = total
                                        }
                                        downloadingApk = false
                                        if (result.isFailure) {
                                            val intent = android.content.Intent(
                                                android.content.Intent.ACTION_VIEW,
                                                android.net.Uri.parse(updateResult!!.releaseUrl)
                                            )
                                            context.startActivity(intent)
                                        } else {
                                            if (!ApkDownloader.hasInstallPermission(context)) {
                                                pendingInstallVersion = updateResult!!.latestVersion
                                            }
                                        }
                                        if (ApkDownloader.hasInstallPermission(context)) {
                                            showUpdateDialog = false
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    color = MiuixTheme.colorScheme.primary
                                ),
                                enabled = !downloadingApk
                            ) {
                                Text(text = "下载", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        } else {
                            Button(
                                onClick = { showUpdateDialog = false },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    color = MiuixTheme.colorScheme.primary
                                )
                            ) {
                                Text(text = "确定", fontWeight = FontWeight.Bold, color = Color.White)
                            }
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

@Composable
fun MarkdownText(text: String, modifier: Modifier = Modifier) {
    val lines = text.lines()
    Column(modifier = modifier) {
        lines.forEach { line ->
            when {
                line.startsWith("### ") -> {
                    Text(
                        text = line.removePrefix("### "),
                        style = TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MiuixTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                    )
                }
                line.startsWith("## ") -> {
                    Text(
                        text = line.removePrefix("## "),
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MiuixTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
                line.startsWith("# ") -> {
                    Text(
                        text = line.removePrefix("# "),
                        style = TextStyle(
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MiuixTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
                line.startsWith("- ") || line.startsWith("* ") -> {
                    Row(
                        modifier = Modifier.padding(vertical = 1.dp)
                    ) {
                        Text(
                            text = "• ",
                            style = TextStyle(
                                fontSize = 13.sp,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                            )
                        )
                        Text(
                            text = line.substring(2),
                            style = TextStyle(
                                fontSize = 13.sp,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                            )
                        )
                    }
                }
                line.isBlank() -> {
                    Spacer(modifier = Modifier.height(4.dp))
                }
                else -> {
                    Text(
                        text = line,
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

private fun formatUpdateFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${(bytes / 1024.0).toInt()} KB"
        bytes < 1024 * 1024 * 1024 -> "${(bytes / (1024.0 * 1024.0)).let { String.format("%.1f", it) }} MB"
        else -> "${(bytes / (1024.0 * 1024.0 * 1024.0)).let { String.format("%.2f", it) }} GB"
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
