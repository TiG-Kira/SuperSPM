package com.kira.superspm.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kira.superspm.plugin.AnalysisResult
import com.kira.superspm.plugin.PluginProcessor
import com.kira.superspm.plugin.SpeedData
import com.kira.superspm.service.LocationService
import com.kira.superspm.utils.PluginManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun PluginNativePage(
    pluginDirName: String,
    pluginName: String,
    onBack: () -> Unit
) {
    val scrollBehavior = MiuixScrollBehavior()
    val backgroundColor = MiuixTheme.colorScheme.background
    val context = androidx.compose.ui.platform.LocalContext.current

    var processor by remember { mutableStateOf<PluginProcessor?>(null) }
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(pluginDirName) {
        processor = PluginManager.getProcessor(pluginDirName)
        loaded = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = pluginName,
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
                            tint = MiuixTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            if (loaded) {
                val p = processor
                if (p != null) {
                    NativePluginContent(processor = p)
                } else {
                    PluginErrorContent()
                }
            }
        }
    }
}

@Composable
fun NativePluginContent(processor: PluginProcessor) {
    val resultFlow = remember { MutableStateFlow<AnalysisResult?>(null) }
    val result by resultFlow.collectAsState()
    val scrollBehavior = MiuixScrollBehavior()

    LaunchedEffect(processor) {
        while (true) {
            val speedData = SpeedData(
                currentSpeed = LocationService.getCurrentSpeed(),
                maxSpeed = LocationService.getMaxSpeed(),
                avgSpeed = LocationService.getAvgSpeed(),
                totalDistance = LocationService.getTotalDistance(),
                isRecording = LocationService.isRecording(),
                isSensorMode = LocationService.isSensorMode(),
                latitude = LocationService.getCurrentLatitude(),
                longitude = LocationService.getCurrentLongitude(),
                accuracy = LocationService.getCurrentAccuracy(),
                sensorAcceleration = LocationService.getSensorAcceleration(),
                sensorVelocity = LocationService.getSensorVelocity(),
                recordingDuration = LocationService.getRecordingDuration(),
                dataPoints = LocationService.getDataPoints()
            )
            resultFlow.value = processor.onSpeedUpdate(speedData)
            delay(500)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = result?.icon ?: "🔍",
                        fontSize = 64.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = result?.title ?: "分析中...",
                        style = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MiuixTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = result?.description ?: "正在获取数据...",
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                        )
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
                        text = "实时数据",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MiuixTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        val speedKmh = LocationService.getCurrentSpeed() * 3.6
                        val avgKmh = LocationService.getAvgSpeed() * 3.6
                        val maxKmh = LocationService.getMaxSpeed() * 3.6

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = String.format("%.1f", speedKmh),
                                style = TextStyle(
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MiuixTheme.colorScheme.primary
                                )
                            )
                            Text(
                                text = "当前速度",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                )
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = String.format("%.1f", avgKmh),
                                style = TextStyle(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MiuixTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = "平均速度",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                )
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = String.format("%.1f", maxKmh),
                                style = TextStyle(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF5252)
                                )
                            )
                            Text(
                                text = "最高速度",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                )
                            )
                        }
                    }
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
                        text = "运动建议",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MiuixTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = result?.advice ?: "等待数据...",
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                        ),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }

        item {
            if (result?.metrics?.isNotEmpty() == true) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "分析指标",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MiuixTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        result?.metrics?.forEach { (key, value) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = mapMetricKey(key),
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                    )
                                )
                                Text(
                                    text = value,
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MiuixTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }
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
                        text = "参考标准",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MiuixTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    val standards = listOf(
                        "3-5 km/h → 慢走",
                        "5-7 km/h → 快走",
                        "7-10 km/h → 慢跑",
                        "10-14 km/h → 中速跑",
                        "14+ km/h → 快跑"
                    )
                    standards.forEach { standard ->
                        Text(
                            text = standard,
                            style = TextStyle(
                                fontSize = 13.sp,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                            ),
                            modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun PluginErrorContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = null,
            tint = Color(0xFFFF9800),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "插件加载失败",
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MiuixTheme.colorScheme.onSurface
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "无法加载插件的主类，请检查插件是否完整或尝试重新导入。",
            style = TextStyle(
                fontSize = 14.sp,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
            ),
            textAlign = TextAlign.Center
        )
    }
}

fun mapMetricKey(key: String): String {
    return when (key) {
        "speed" -> "速度"
        "stepFrequency" -> "步频"
        "steps" -> "步数"
        "duration" -> "时长"
        "distance" -> "里程"
        else -> key
    }
}
