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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.kira.superspm.R
import com.kira.superspm.plugin.Plugin
import com.kira.superspm.plugin.data.AnalysisResult
import com.kira.superspm.plugin.data.SpeedData
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
import android.content.Context

@Composable
fun PluginNativePage(
    pluginDirName: String,
    pluginName: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollBehavior = MiuixScrollBehavior()
    val backgroundColor = MiuixTheme.colorScheme.background

    var processor by remember { mutableStateOf<Plugin?>(null) }
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(pluginDirName) {
        processor = PluginManager.getProcessor(pluginDirName)
        loaded = true
    }

    val resultFlow = remember { MutableStateFlow<AnalysisResult?>(null) }
    val result by resultFlow.collectAsState()

    val speedDataFlow = remember { MutableStateFlow<SpeedData?>(null) }
    val speedData by speedDataFlow.collectAsState()

    LaunchedEffect(processor) {
        if (processor == null) return@LaunchedEffect
        while (true) {
            val currentData = SpeedData(
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
            speedDataFlow.value = currentData
            resultFlow.value = processor!!.onSpeedUpdate(currentData)
            delay(500)
        }
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
                            .size(40.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .clickable { onBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = context.getString(R.string.back),
                            tint = MiuixTheme.colorScheme.onSurface
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
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                top = paddingValues.calculateTopPadding(),
                bottom = 120.dp
            )
        ) {
            if (!loaded) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = context.getString(R.string.loading))
                    }
                }
            } else {
                val p = processor
                if (p != null) {
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
                                    text = result?.title ?: context.getString(R.string.analyzing),
                                    style = TextStyle(
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MiuixTheme.colorScheme.onSurface
                                    )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = result?.description ?: context.getString(R.string.getting_data),
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
                                    text = context.getString(R.string.real_time_data),
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
                                    val speedKmh = speedData?.currentSpeed ?: 0.0
                                    val avgKmh = speedData?.avgSpeed ?: 0.0
                                    val maxKmh = speedData?.maxSpeed ?: 0.0

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
                                            text = context.getString(R.string.current_speed),
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
                                            text = context.getString(R.string.avg_speed),
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
                                            text = context.getString(R.string.max_speed),
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
                                    text = context.getString(R.string.sport_suggestions),
                                    style = TextStyle(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MiuixTheme.colorScheme.onSurface
                                    ),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Text(
                                    text = result?.advice ?: context.getString(R.string.waiting_data),
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                    ),
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }

                    val currentResult = result
                    if (currentResult != null && currentResult.metrics.isNotEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = context.getString(R.string.analysis_metrics),
                                        style = TextStyle(
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MiuixTheme.colorScheme.onSurface
                                        ),
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )
                                    val metricsList = currentResult.metrics.toList()
                                    metricsList.forEach { (key, value) ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = mapMetricKey(key, context),
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
                                    text = context.getString(R.string.reference_standards),
                                    style = TextStyle(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MiuixTheme.colorScheme.onSurface
                                    ),
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                val standards = listOf(
                                    "3-5 km/h → ${context.getString(R.string.slow_walk)}",
                                    "5-7 km/h → ${context.getString(R.string.fast_walk)}",
                                    "7-10 km/h → ${context.getString(R.string.slow_jog)}",
                                    "10-14 km/h → ${context.getString(R.string.medium_run)}",
                                    "14+ km/h → ${context.getString(R.string.fast_run)}"
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
                } else {
                    item {
                        PluginErrorContent(context)
                    }
                }
            }
        }
    }
}

@Composable
fun PluginErrorContent(context: Context) {
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
            text = context.getString(R.string.plugin_load_failed),
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MiuixTheme.colorScheme.onSurface
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = context.getString(R.string.plugin_load_error_desc),
            style = TextStyle(
                fontSize = 14.sp,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
            ),
            textAlign = TextAlign.Center
        )
    }
}

fun mapMetricKey(key: String, context: Context): String {
    return when (key) {
        "speed" -> context.getString(R.string.max_speed)
        "stepFrequency" -> context.getString(R.string.step_frequency)
        "steps" -> context.getString(R.string.steps)
        "pace" -> context.getString(R.string.pace)
        "duration" -> context.getString(R.string.duration)
        "distance" -> context.getString(R.string.distance)
        "heartRate" -> context.getString(R.string.heart_rate)
        "avgHeartRate" -> context.getString(R.string.avg_heart_rate)
        "calories" -> context.getString(R.string.calories)
        "totalDistance" -> context.getString(R.string.total_distance)
        "maxSpeed" -> context.getString(R.string.max_speed)
        "avgSpeed" -> context.getString(R.string.avg_speed)
        "recordCount" -> context.getString(R.string.record_count)
        "currentSpeed" -> context.getString(R.string.current_speed)
        "todayDistance" -> context.getString(R.string.today_distance)
        else -> key
    }
}