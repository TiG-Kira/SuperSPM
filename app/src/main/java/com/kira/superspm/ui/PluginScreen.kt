package com.kira.superspm.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Extension
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.kira.superspm.utils.PluginManager
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun PluginScreen(
    isDark: Boolean = false,
    navController: NavHostController
) {
    val context = LocalContext.current
    val scrollBehavior = MiuixScrollBehavior()
    val backgroundColor = getPageBackgroundColor(isDark)
    val plugins by PluginManager.plugins.collectAsState()
    var showRestartDialog by remember { mutableStateOf(false) }
    var importMessage by remember { mutableStateOf<String?>(null) }

    val fileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val fileName = queryFileName(context, uri)
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val result = PluginManager.importPlugin(context, inputStream, fileName)
                    if (result.isSuccess) {
                        val plugin = result.getOrNull()
                        if (plugin?.config?.requiresRestart == true) {
                            showRestartDialog = true
                        }
                        importMessage = "插件 ${plugin?.config?.name} 导入成功"
                    } else {
                        importMessage = "导入失败: ${result.exceptionOrNull()?.message}"
                    }
                }
            } catch (e: Exception) {
                importMessage = "导入失败: ${e.message}"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = "插件",
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
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clickable { fileLauncher.launch("*/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "导入",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MiuixTheme.colorScheme.primary
                            )
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
                            text = "插件说明",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MiuixTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = "插件允许开发者扩展 SuperSPM 的功能。插件以 .zip 压缩包形式导入，包含 plugin.json 配置文件和资源文件。\n\n" +
                                "插件类型：\n" +
                                "• WebUI 插件：使用 HTML/CSS/JS 开发，通过 WebView 运行，可调用 SpeedData 接口获取测速数据\n" +
                                "• Native 插件：使用内置 Kotlin/Java 库开发\n\n" +
                                "限制：\n" +
                                "• 插件运行在沙盒环境中，仅能访问 plugin.json 中声明的权限\n" +
                                "• WebUI 插件可使用的接口：SpeedData（速度、距离、传感器数据）\n" +
                                "• 插件不可修改应用核心逻辑、不可访问用户历史记录\n" +
                                "• 插件导入后立即生效，标记为需要重启的插件需手动重启应用",
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
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "plugin.json 配置模板",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MiuixTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = """{
  "name": "插件名称",
  "version": "1.0.0",
  "author": "开发者名称",
  "description": "插件描述",
  "type": "WEB_UI",
  "entry": "index.html",
  "permissions": ["speed_data"],
  "requiresRestart": false
}""",
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            ),
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .fillMaxWidth()
                                .background(
                                    if (isDark) Color(0xFF1A1A1A) else Color(0xFFF5F5F5),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp)
                        )
                    }
                }
            }

            if (plugins.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Extension,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "暂无插件",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "点击右上角\"导入\"按钮导入插件",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                )
                            )
                        }
                    }
                }
            } else {
                item {
                    Text(
                        text = "已安装插件 (${plugins.size})",
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                        ),
                        modifier = Modifier.padding(top = 16.dp, start = 16.dp, bottom = 8.dp)
                    )
                }

                items(plugins) { plugin ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(enabled = plugin.enabled) {
                                        val route = if (plugin.config.type == com.kira.superspm.data.model.PluginType.NATIVE) {
                                            "plugin_native/${plugin.dirName}/${plugin.config.name}"
                                        } else {
                                            "plugin_web/${plugin.dirName}/${plugin.config.name}"
                                        }
                                        navController.navigate(route)
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (plugin.enabled) MiuixTheme.colorScheme.primary
                                            else MiuixTheme.colorScheme.onSurfaceVariantSummary
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = plugin.config.name.firstOrNull()?.toString() ?: "P",
                                        style = TextStyle(
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = plugin.config.name,
                                        style = TextStyle(
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MiuixTheme.colorScheme.onSurface
                                        )
                                    )
                                    Text(
                                        text = "v${plugin.config.version} • ${plugin.config.author}",
                                        style = TextStyle(
                                            fontSize = 12.sp,
                                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                        )
                                    )
                                    if (plugin.config.description.isNotBlank()) {
                                        Text(
                                            text = plugin.config.description,
                                            style = TextStyle(
                                                fontSize = 12.sp,
                                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                            ),
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                    if (plugin.config.type == com.kira.superspm.data.model.PluginType.WEB_UI) {
                                        Text(
                                            text = "WebUI",
                                            style = TextStyle(
                                                fontSize = 10.sp,
                                                color = MiuixTheme.colorScheme.primary
                                            ),
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                }
                            }
                            Switch(
                                checked = plugin.enabled,
                                onCheckedChange = {
                                    PluginManager.setPluginEnabled(context, plugin.dirName, it)
                                    if (it && plugin.config.requiresRestart) {
                                        showRestartDialog = true
                                    }
                                }
                            )
                            Box(
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        PluginManager.deletePlugin(context, plugin.dirName)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "删除",
                                    tint = Color(0xFFFF5252),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(120.dp))
            }
        }
    }

    if (showRestartDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showRestartDialog = false }) {
            Card {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "需要重启应用",
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MiuixTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Text(
                        text = "该插件需要重启应用才能完全生效。是否立即重启？",
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                        ),
                        modifier = Modifier.padding(bottom = 20.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        top.yukonga.miuix.kmp.basic.Button(
                            onClick = { showRestartDialog = false },
                            modifier = Modifier.weight(1f),
                            colors = top.yukonga.miuix.kmp.basic.ButtonDefaults.buttonColors(
                                color = MiuixTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(text = "稍后", fontWeight = FontWeight.Bold)
                        }
                        top.yukonga.miuix.kmp.basic.Button(
                            onClick = {
                                showRestartDialog = false
                                val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                                intent?.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                                android.os.Process.killProcess(android.os.Process.myPid())
                            },
                            modifier = Modifier.weight(1f),
                            colors = top.yukonga.miuix.kmp.basic.ButtonDefaults.buttonColors(
                                color = MiuixTheme.colorScheme.primary
                            )
                        ) {
                            Text(text = "立即重启", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }

    importMessage?.let { message ->
        androidx.compose.ui.window.Dialog(onDismissRequest = { importMessage = null }) {
            Card {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = message,
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = MiuixTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    top.yukonga.miuix.kmp.basic.Button(
                        onClick = { importMessage = null },
                        modifier = Modifier.fillMaxWidth(),
                        colors = top.yukonga.miuix.kmp.basic.ButtonDefaults.buttonColors(
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

fun queryFileName(context: android.content.Context, uri: android.net.Uri): String {
    var fileName = "plugin.zip"
    try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    val name = cursor.getString(nameIndex)
                    if (name != null) fileName = name
                }
            }
        }
    } catch (e: Exception) {
        // fallback: use lastPathSegment
        uri.lastPathSegment?.let { fileName = it }
    }
    return fileName
}
