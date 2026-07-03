package com.kira.superspm.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import com.kira.superspm.data.model.LocationPoint
import com.kira.superspm.data.store.SpeedUnit
import com.kira.superspm.viewmodel.DetailViewModel
import com.kira.superspm.viewmodel.SettingsViewModel
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import top.yukonga.miuix.kmp.theme.MiuixTheme
import org.koin.androidx.compose.getViewModel
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DetailScreen(
    recordId: Long,
    onBack: () -> Unit,
    isDark: Boolean = false
) {
    val viewModel: DetailViewModel = getViewModel()
    val settingsViewModel: SettingsViewModel = getViewModel()

    LaunchedEffect(recordId) {
        viewModel.loadRecord(recordId)
    }

    val record = viewModel.record

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(getPageBackgroundColor(isDark))
    ) {
        item {
            Row(
                modifier = Modifier
                    .wrapContentWidth()
                    .padding(top = 56.dp, start = 16.dp, end = 16.dp, bottom = 0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 32.dp, height = 56.dp)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = if (isDark) Color.White else Color.Black,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        stickyHeader {
            TopAppBar(
                title = record?.name ?: "详情",
                navigationIcon = {}
            )
        }

        record?.let {
            item {
                Text(
                    text = formatDateTime(it.startTime),
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                    ),
                    modifier = Modifier.padding(16.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    DetailStatCard(
                        value = formatSpeed(it.maxSpeed, settingsViewModel.speedUnit),
                        label = "最高",
                        unit = getSpeedUnitString(settingsViewModel.speedUnit)
                    )
                    DetailStatCard(
                        value = formatSpeed(it.avgSpeed, settingsViewModel.speedUnit),
                        label = "平均",
                        unit = getSpeedUnitString(settingsViewModel.speedUnit)
                    )
                    DetailStatCard(
                        value = String.format("%.2f", it.totalDistance),
                        label = "总里程",
                        unit = "km"
                    )
                    DetailStatCard(
                        value = it.dataPoints.toString(),
                        label = "数据点",
                        unit = ""
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "速度变化曲线",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        SpeedChart(viewModel.pathPoints, settingsViewModel.speedUnit)
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "运动轨迹",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        TrackMap(viewModel.pathPoints)
                    }
                }
            }
        }
    }
}

@Composable
fun DetailStatCard(value: String, label: String, unit: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MiuixTheme.colorScheme.onSurface
                )
            )
            Text(
                text = label,
                style = TextStyle(
                    fontSize = 10.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
            )
            if (unit.isNotEmpty()) {
                Text(
                    text = unit,
                    style = TextStyle(
                        fontSize = 10.sp,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                    )
                )
            }
        }
    }
}

@Composable
fun SpeedChart(points: List<LocationPoint>, unit: SpeedUnit) {
    val primaryColor = MiuixTheme.colorScheme.primary
    val onSurfaceVariantSummary = MiuixTheme.colorScheme.onSurfaceVariantSummary

    Canvas(modifier = Modifier.fillMaxWidth().height(150.dp)) {
        if (points.isEmpty()) return@Canvas

        val width = size.width
        val height = size.height
        val padding = 20.dp.toPx()

        val maxSpeed = points.maxOf { it.speed } * 1.2
        val minSpeed = 0.0

        val xScale = (width - padding * 2) / (points.size - 1).toFloat()
        val yScale = (height - padding * 2) / (maxSpeed - minSpeed).toFloat()

        val path = Path()
        var firstPoint = true

        points.forEachIndexed { index, point ->
            val x = padding + index * xScale
            val y = height - padding - ((point.speed - minSpeed) * yScale).toFloat()

            if (firstPoint) {
                path.moveTo(x, y)
                firstPoint = false
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = primaryColor,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
        )

        for (i in 0..4) {
            val y = padding + (height - padding * 2) * i / 4f
            drawLine(
                color = onSurfaceVariantSummary.copy(alpha = 0.3f),
                start = Offset(padding, y),
                end = Offset(width - padding, y),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}

@Composable
fun TrackMap(points: List<LocationPoint>) {
    val primaryColor = MiuixTheme.colorScheme.primary

    Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (points.isEmpty()) return@Canvas

            val width = size.width
            val height = size.height
            val padding = 20.dp.toPx()

            val minLat = points.minOf { it.latitude }
            val maxLat = points.maxOf { it.latitude }
            val minLon = points.minOf { it.longitude }
            val maxLon = points.maxOf { it.longitude }

            val latRange = maxLat - minLat
            val lonRange = maxLon - minLon
            val scale = minOf((width - padding * 2) / lonRange.toFloat(), (height - padding * 2) / latRange.toFloat())

            val path = Path()
            var firstPoint = true

            points.forEachIndexed { index, point ->
                val x = padding + ((point.longitude - minLon) * scale).toFloat()
                val y = height - padding - ((point.latitude - minLat) * scale).toFloat()

                if (firstPoint) {
                    path.moveTo(x, y)
                    firstPoint = false
                    drawCircle(color = Color(0xFF00C853), center = Offset(x, y), radius = 6.dp.toPx())
                } else {
                    path.lineTo(x, y)
                    if (index == points.lastIndex) {
                        drawCircle(color = Color(0xFFFF1744), center = Offset(x, y), radius = 6.dp.toPx())
                    }
                }
            }

            drawPath(
                path = path,
                color = primaryColor,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
            )
        }

        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(modifier = Modifier.size(12.dp)) {
                    drawCircle(Color(0xFF00C853))
                }
                Text(text = "低速", style = TextStyle(fontSize = 10.sp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(modifier = Modifier.size(12.dp)) {
                    drawCircle(Color(0xFFFFAB00))
                }
                Text(text = "中速", style = TextStyle(fontSize = 10.sp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(modifier = Modifier.size(12.dp)) {
                    drawCircle(Color(0xFFFF1744))
                }
                Text(text = "高速", style = TextStyle(fontSize = 10.sp))
            }
        }
    }
}

private fun formatSpeed(speed: Double, unit: SpeedUnit): String {
    return when (unit) {
        SpeedUnit.KMH -> String.format("%.0f", speed)
        SpeedUnit.MS -> String.format("%.1f", speed / 3.6)
        SpeedUnit.MPH -> String.format("%.0f", speed * 0.621371)
    }
}

private fun getSpeedUnitString(unit: SpeedUnit): String {
    return when (unit) {
        SpeedUnit.KMH -> "km/h"
        SpeedUnit.MS -> "m/s"
        SpeedUnit.MPH -> "mph"
    }
}

private fun formatDateTime(instant: Instant): String {
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return String.format(
        "%04d/%02d/%02d %02d:%02d:%02d",
        localDateTime.year,
        localDateTime.monthNumber,
        localDateTime.dayOfMonth,
        localDateTime.hour,
        localDateTime.minute,
        localDateTime.second
    )
}

