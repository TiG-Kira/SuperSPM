package com.kira.superspm.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.background
import com.kira.superspm.data.model.LocationRecord
import com.kira.superspm.data.store.SpeedUnit
import com.kira.superspm.viewmodel.HistoryViewModel
import com.kira.superspm.viewmodel.SettingsViewModel
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowRight
import top.yukonga.miuix.kmp.theme.MiuixTheme
import org.koin.androidx.compose.getViewModel
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun HistoryScreen(
    recordClick: (Long) -> Unit,
    isDark: Boolean = false
) {
    val viewModel: HistoryViewModel = getViewModel()
    val settingsViewModel: SettingsViewModel = getViewModel()
    val records by viewModel.records.collectAsStateWithLifecycle(emptyList())

    var editingId by remember { mutableStateOf<Long?>(null) }
    var editingName by remember { mutableStateOf("") }
    var deletingRecord by remember { mutableStateOf<LocationRecord?>(null) }

    val scrollBehavior = MiuixScrollBehavior()
    val backgroundColor = getPageBackgroundColor(isDark)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        if (records.isEmpty()) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = "历史记录",
                        color = backgroundColor
                    )
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundColor)
                        .padding(top = paddingValues.calculateTopPadding()),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "暂无记录",
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                        )
                    )
                }
            }
        } else {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = "历史记录",
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
                    items(records) { record ->
                        RecordItem(
                            record = record,
                            speedUnit = settingsViewModel.speedUnit,
                            onItemClick = { recordClick(record.id) },
                            onEditClick = {
                                editingId = record.id
                                editingName = record.name
                            },
                            onDeleteClick = { deletingRecord = record }
                        )
                    }

                    item {
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(80.dp))
                    }
                }
            }
        }

        if (editingId != null) {
            DialogOverlay {
                AnimatedDialogContent {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 280.dp)
                            .padding(16.dp)
                            .align(Alignment.Center)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "重命名记录",
                                style = TextStyle(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            TextField(
                                value = editingName,
                                onValueChange = { editingName = it },
                                label = "输入名称"
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = { editingId = null },
                                    modifier = Modifier.padding(end = 8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        color = MiuixTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Text(text = "取消")
                                }
                                Button(
                                    onClick = {
                                        records.find { it.id == editingId }?.let {
                                            viewModel.renameRecord(it, editingName)
                                        }
                                        editingId = null
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        color = MiuixTheme.colorScheme.primary
                                    )
                                ) {
                                    Text(text = "保存")
                                }
                            }
                        }
                    }
                }
            }
        }

        if (deletingRecord != null) {
            DialogOverlay {
                AnimatedDialogContent {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 280.dp)
                            .padding(16.dp)
                            .align(Alignment.Center)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "删除记录",
                                style = TextStyle(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            Text(
                                text = "确定删除此记录？",
                                style = TextStyle(color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = { deletingRecord = null },
                                    modifier = Modifier.padding(end = 8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        color = MiuixTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Text(text = "取消")
                                }
                                Button(
                                    onClick = {
                                        deletingRecord?.let { viewModel.deleteRecord(it) }
                                        deletingRecord = null
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        color = MiuixTheme.colorScheme.error
                                    )
                                ) {
                                    Text(text = "删除")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecordItem(
    record: LocationRecord,
    speedUnit: SpeedUnit,
    onItemClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onItemClick),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = record.name,
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MiuixTheme.colorScheme.onSurface
                    )
                )
                Icon(
                    imageVector = Icons.Filled.ArrowRight,
                    contentDescription = "箭头",
                    tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(
                text = formatDateTime(record.startTime),
                style = TextStyle(
                    fontSize = 12.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                ),
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = buildSummary(record, speedUnit),
                style = TextStyle(
                    fontSize = 13.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                ),
                modifier = Modifier.padding(top = 4.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = onEditClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(text = "编辑名称")
                }
                Button(
                    onClick = onDeleteClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.error.copy(alpha = 0.1f)
                    )
                ) {
                    Text(text = "删除")
                }
            }
        }
    }
}

private fun buildSummary(record: LocationRecord, unit: SpeedUnit): String {
    val unitStr = when (unit) {
        SpeedUnit.KMH -> "km/h"
        SpeedUnit.MS -> "m/s"
        SpeedUnit.MPH -> "mph"
    }
    val maxSpeed = when (unit) {
        SpeedUnit.KMH -> record.maxSpeed
        SpeedUnit.MS -> record.maxSpeed / 3.6
        SpeedUnit.MPH -> record.maxSpeed * 0.621371
    }
    val avgSpeed = when (unit) {
        SpeedUnit.KMH -> record.avgSpeed
        SpeedUnit.MS -> record.avgSpeed / 3.6
        SpeedUnit.MPH -> record.avgSpeed * 0.621371
    }

    return "最高 ${String.format("%.0f", maxSpeed)} $unitStr · 平均 ${String.format("%.0f", avgSpeed)} $unitStr · ${String.format("%.2f", record.totalDistance)} km · ${record.dataPoints} 个点"
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