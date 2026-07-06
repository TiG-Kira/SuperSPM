package com.kira.superspm.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Circle
import com.kira.superspm.R
import com.kira.superspm.data.store.SpeedUnit
import com.kira.superspm.service.LocationService
import com.kira.superspm.service.AccelerometerService
import com.kira.superspm.viewmodel.SettingsViewModel
import com.kira.superspm.viewmodel.SpeedometerViewModel
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import top.yukonga.miuix.kmp.theme.MiuixTheme
import org.koin.androidx.compose.getViewModel
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import android.location.Geocoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun SpeedometerScreen(
    onRecordSaved: () -> Unit = {},
    hasLocationPermission: Boolean = true,
    onPermissionRequest: () -> Unit = {},
    isDark: Boolean = false
) {
    val viewModel: SpeedometerViewModel = getViewModel()
    val settingsViewModel: SettingsViewModel = getViewModel()
    val context = LocalContext.current

    val currentSpeed = viewModel.currentSpeed
    val maxSpeed = viewModel.maxSpeed
    val avgSpeed = viewModel.avgSpeed
    val totalDistance = viewModel.totalDistance
    val status = viewModel.status
    val dataPoints = viewModel.dataPoints
    val currentLatitude = viewModel.currentLatitude
    val currentLongitude = viewModel.currentLongitude
    val currentAccuracy = viewModel.currentAccuracy
    var gpsAccuracy by remember { mutableStateOf<Float?>(null) }

    var secondsUntilRefresh by remember { mutableStateOf(0) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var addressText by remember { mutableStateOf("") }
    
    var accX by remember { mutableStateOf(0.0) }
    var accY by remember { mutableStateOf(0.0) }
    var accZ by remember { mutableStateOf(0.0) }
    var accEstimatedSpeed by remember { mutableStateOf(0.0) }
    
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        LocationService.onLocationUpdate = { lat, lon, speed, accuracy ->
            viewModel.updateLocation(lat, lon, speed, accuracy)
            coroutineScope.launch {
                addressText = getAddress(context, lat, lon)
            }
        }

        LocationService.onSpeedUpdate = { speed ->
            viewModel.updateSpeed(speed)
        }

        LocationService.onGpsSignalUpdate = { accuracy ->
            gpsAccuracy = accuracy
        }

        LocationService.onServiceStopped = {
            viewModel.reset()
        }

        LocationService.onError = { message ->
            errorMessage = message
            showErrorDialog = true
        }
    }

    LaunchedEffect(status) {
        if (status != SpeedometerViewModel.RecordingStatus.NOT_STARTED &&
            !(settingsViewModel.powerSaving && settingsViewModel.accelerometerEnabled)) {
            LocationService.requestSingleUpdate(context)
        }
    }

    LaunchedEffect(settingsViewModel.accelerometerEnabled) {
        if (settingsViewModel.accelerometerEnabled && status != SpeedometerViewModel.RecordingStatus.NOT_STARTED) {
            AccelerometerService.start(context)
            AccelerometerService.onAccelerationUpdate = { x, y, z ->
                accX = x
                accY = y
                accZ = z
            }
            AccelerometerService.onSpeedEstimateUpdate = { speed ->
                accEstimatedSpeed = speed
            }
        } else {
            AccelerometerService.stop()
        }
    }

    DisposableEffect(Unit) {
        if (LocationService.isRunning() && LocationService.isRecording()) {
            viewModel.restoreFromService(
                LocationService.getCurrentSpeed(),
                LocationService.getMaxSpeed(),
                LocationService.getAvgSpeed(),
                LocationService.getTotalDistance(),
                LocationService.getDataPoints()
            )
        } else if (status != SpeedometerViewModel.RecordingStatus.NOT_STARTED) {
            viewModel.reset()
        }
        onDispose { }
    }

    LaunchedEffect(status, settingsViewModel.refreshTime) {
        if (settingsViewModel.refreshTime == 0) {
            secondsUntilRefresh = 0
        } else {
            while (status != SpeedometerViewModel.RecordingStatus.NOT_STARTED) {
                secondsUntilRefresh = settingsViewModel.refreshTime
                for (i in 0 until settingsViewModel.refreshTime) {
                    if (status == SpeedometerViewModel.RecordingStatus.NOT_STARTED) break
                    secondsUntilRefresh--
                    kotlinx.coroutines.delay(1000)
                }
            }
            secondsUntilRefresh = 0
        }
    }

    val scrollBehavior = MiuixScrollBehavior()
    val backgroundColor = getPageBackgroundColor(isDark)

    Scaffold(
        topBar = {
            TopAppBar(
                title = context.getString(R.string.title_speedometer),
                scrollBehavior = scrollBehavior,
                color = backgroundColor,
                actions = {
                    if (status == SpeedometerViewModel.RecordingStatus.NOT_STARTED) {
                        IconButton(
                            onClick = {
                                if (!hasLocationPermission) {
                                    showPermissionDialog = true
                                    return@IconButton
                                }
                                viewModel.startRecording()
                                if (!settingsViewModel.powerSaving || !settingsViewModel.accelerometerEnabled) {
                                    LocationService.updateSettings(
                                        settingsViewModel.powerSaving,
                                        settingsViewModel.refreshTime
                                    )
                                    val intent = android.content.Intent(context, LocationService::class.java)
                                    intent.putExtra("saveRecord", false)
                                    context.startForegroundService(intent)
                                }
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "开始",
                                tint = MiuixTheme.colorScheme.onSurface,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        if (!settingsViewModel.powerSaving) {
                            IconButton(
                                onClick = {
                                    if (!hasLocationPermission) {
                                        showPermissionDialog = true
                                        return@IconButton
                                    }
                                    LocationService.updateSettings(
                                        settingsViewModel.powerSaving,
                                        settingsViewModel.refreshTime
                                    )
                                    viewModel.startRecording(recordData = true)
                                    val intent = android.content.Intent(context, LocationService::class.java)
                                    intent.putExtra("saveRecord", true)
                                    context.startForegroundService(intent)
                                },
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Circle,
                                    contentDescription = "记录并开始",
                                    tint = MiuixTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    } else {
                        IconButton(
                            onClick = {
                                when (status) {
                                    SpeedometerViewModel.RecordingStatus.RECORDING -> {
                                        viewModel.pauseRecording()
                                    }
                                    SpeedometerViewModel.RecordingStatus.PAUSED -> {
                                        viewModel.resumeRecording()
                                        if (!settingsViewModel.powerSaving || !settingsViewModel.accelerometerEnabled) {
                                            context.startForegroundService(android.content.Intent(context, LocationService::class.java))
                                        }
                                    }
                                    else -> {}
                                }
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(
                                imageVector = if (status == SpeedometerViewModel.RecordingStatus.RECORDING) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = if (status == SpeedometerViewModel.RecordingStatus.RECORDING) "暂停" else "继续",
                                tint = MiuixTheme.colorScheme.onSurface,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        IconButton(
                            onClick = {
                                if (!settingsViewModel.powerSaving || !settingsViewModel.accelerometerEnabled) {
                                    val record = LocationService.finishRecording()
                                    if (record != null) {
                                        onRecordSaved()
                                    }
                                }
                                viewModel.reset()
                                context.stopService(android.content.Intent(context, LocationService::class.java))
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Stop,
                                contentDescription = "停止",
                                tint = MiuixTheme.colorScheme.onSurface,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                contentPadding = PaddingValues(top = paddingValues.calculateTopPadding())
            ) {

                item {
                    if (status == SpeedometerViewModel.RecordingStatus.NOT_STARTED) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "未开始",
                                    style = TextStyle(
                                        fontSize = 48.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MiuixTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = "选择一种模式来开始测速",
                                    style = TextStyle(
                                        fontSize = 16.sp,
                                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                    ),
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            SpeedometerGauge(currentSpeed)
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Text(
                                text = formatSpeed(currentSpeed, settingsViewModel.speedUnit),
                                style = TextStyle(
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MiuixTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = getSpeedUnitString(settingsViewModel.speedUnit),
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                )
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (status == SpeedometerViewModel.RecordingStatus.NOT_STARTED) {
                            GpsSignalCard(
                                accuracy = gpsAccuracy,
                                isDark = isDark,
                                powerSaving = settingsViewModel.powerSaving,
                                accelerometerEnabled = settingsViewModel.accelerometerEnabled,
                                isNotStarted = true
                            )
                        } else {
                            GpsSignalCard(
                                accuracy = gpsAccuracy,
                                isDark = isDark,
                                powerSaving = settingsViewModel.powerSaving,
                                accelerometerEnabled = settingsViewModel.accelerometerEnabled
                            )

                            Column(
                                modifier = Modifier.height(160.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Card(
                                    modifier = Modifier.fillMaxWidth().weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = context.getString(R.string.max_speed),
                                                style = TextStyle(
                                                    fontSize = 11.sp,
                                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                                )
                                            )
                                            Text(
                                                text = formatSpeed(maxSpeed, settingsViewModel.speedUnit),
                                                style = TextStyle(
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MiuixTheme.colorScheme.onSurface
                                                )
                                            )
                                            Text(
                                                text = getSpeedUnitString(settingsViewModel.speedUnit),
                                                style = TextStyle(
                                                    fontSize = 9.sp,
                                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                                )
                                            )
                                        }
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = context.getString(R.string.avg_speed),
                                                style = TextStyle(
                                                    fontSize = 11.sp,
                                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                                )
                                            )
                                            Text(
                                                text = formatSpeed(avgSpeed, settingsViewModel.speedUnit),
                                                style = TextStyle(
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MiuixTheme.colorScheme.onSurface
                                                )
                                            )
                                            Text(
                                                text = getSpeedUnitString(settingsViewModel.speedUnit),
                                                style = TextStyle(
                                                    fontSize = 9.sp,
                                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                                )
                                            )
                                        }
                                    }
                                }
                                Card(
                                    modifier = Modifier.fillMaxWidth().weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = context.getString(R.string.total_distance),
                                            style = TextStyle(
                                                fontSize = 11.sp,
                                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                            )
                                        )
                                        Text(
                                            text = formatDistance(totalDistance),
                                            style = TextStyle(
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MiuixTheme.colorScheme.onSurface
                                            )
                                        )
                                        Text(
                                            text = context.getString(R.string.km),
                                            style = TextStyle(
                                                fontSize = 9.sp,
                                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    if (status != SpeedometerViewModel.RecordingStatus.NOT_STARTED && 
                        settingsViewModel.accelerometerEnabled) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            colors = CardDefaults.defaultColors(color = if (isDark) Color(0xFF1A237E) else Color(0xFFE3F2FD))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "加速度计数据",
                                    style = TextStyle(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MiuixTheme.colorScheme.onSurface
                                    )
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "X",
                                            style = TextStyle(fontSize = 12.sp, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                                        )
                                        Text(
                                            text = String.format("%.2f", accX),
                                            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MiuixTheme.colorScheme.onSurface)
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "Y",
                                            style = TextStyle(fontSize = 12.sp, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                                        )
                                        Text(
                                            text = String.format("%.2f", accY),
                                            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MiuixTheme.colorScheme.onSurface)
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "Z",
                                            style = TextStyle(fontSize = 12.sp, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                                        )
                                        Text(
                                            text = String.format("%.2f", accZ),
                                            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MiuixTheme.colorScheme.onSurface)
                                        )
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "估算速度",
                                        style = TextStyle(fontSize = 12.sp, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                                    )
                                    Text(
                                        text = "${formatSpeed(accEstimatedSpeed, settingsViewModel.speedUnit)} ${getSpeedUnitString(settingsViewModel.speedUnit)}",
                                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MiuixTheme.colorScheme.onSurface)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    if (settingsViewModel.powerSaving) {
                    } else if (status == SpeedometerViewModel.RecordingStatus.NOT_STARTED) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            colors = CardDefaults.defaultColors(color = if (isDark) Color(0xFF1A1A1A) else Color(0xFFF0F0F0))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "开始测速后获取位置和经纬度",
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                    ),
                                    modifier = Modifier.align(Alignment.Center)
                                )
                                Icon(
                                    imageVector = Icons.Rounded.Warning,
                                    contentDescription = "提示",
                                    tint = MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .size(20.dp)
                                        .align(Alignment.BottomEnd)
                                )
                            }
                        }
                    } else {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                ) {
                                    Text(
                                        text = context.getString(R.string.current_location),
                                        style = TextStyle(fontWeight = FontWeight.Medium)
                                    )
                                    Icon(
                                        imageVector = Icons.Filled.Refresh,
                                        contentDescription = "刷新",
                                        tint = MiuixTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clickable {
                                                if (settingsViewModel.refreshTime > 0) {
                                                    secondsUntilRefresh = settingsViewModel.refreshTime
                                                }
                                                LocationService.requestSingleUpdate(context)
                                            }
                                    )
                                }

                                Text(
                                    text = addressText.ifEmpty { "获取地址中..." },
                                    style = TextStyle(color = MiuixTheme.colorScheme.onSurface)
                                )

                                if (settingsViewModel.refreshTime > 0) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = context.getString(R.string.seconds_until_refresh, secondsUntilRefresh),
                                            style = TextStyle(
                                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                                fontSize = 12.sp
                                            )
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = context.getString(R.string.latitude),
                                            style = TextStyle(
                                                fontSize = 12.sp,
                                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                            )
                                        )
                                        Text(
                                            text = currentLatitude?.toString() ?: "--",
                                            style = TextStyle(
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MiuixTheme.colorScheme.onSurface,
                                                textAlign = TextAlign.Start
                                            ),
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = context.getString(R.string.longitude),
                                            style = TextStyle(
                                                fontSize = 12.sp,
                                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                            )
                                        )
                                        Text(
                                            text = currentLongitude?.toString() ?: "--",
                                            style = TextStyle(
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MiuixTheme.colorScheme.onSurface,
                                                textAlign = TextAlign.End
                                            ),
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
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
    }

    if (showPermissionDialog) {
        Dialog(
            onDismissRequest = { showPermissionDialog = false }
        ) {
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = context.getString(R.string.permission_required),
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MiuixTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = context.getString(R.string.permission_description),
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                        ),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = { showPermissionDialog = false },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                color = MiuixTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = context.getString(R.string.cancel),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Button(
                            onClick = {
                                showPermissionDialog = false
                                val intent = android.content.Intent(
                                    android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    android.net.Uri.fromParts("package", context.packageName, null)
                                )
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                color = MiuixTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = context.getString(R.string.grant_permission),
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    if (showErrorDialog) {
        Dialog(
            onDismissRequest = { showErrorDialog = false }
        ) {
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = context.getString(R.string.error),
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MiuixTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = errorMessage,
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                        ),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Button(
                        onClick = { showErrorDialog = false },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            color = MiuixTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = context.getString(R.string.ok),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

private suspend fun getAddress(context: android.content.Context, latitude: Double, longitude: Double): String {
    return withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context)
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val sb = StringBuilder()
                if (!address.thoroughfare.isNullOrEmpty()) {
                    sb.append(address.thoroughfare)
                }
                if (!address.subThoroughfare.isNullOrEmpty()) {
                    if (sb.isNotEmpty()) sb.append(" ")
                    sb.append(address.subThoroughfare)
                }
                if (!address.subLocality.isNullOrEmpty()) {
                    if (sb.isNotEmpty()) sb.append(" ")
                    sb.append(address.subLocality)
                }
                sb.toString().ifEmpty { address.getAddressLine(0) ?: "" }
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }
}

@Composable
fun SpeedometerGauge(speed: Double) {
    val colorScheme = MiuixTheme.colorScheme
    Canvas(modifier = Modifier.size(280.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.width / 2 - 20.dp.toPx()
        val startAngle = 135f
        val sweepAngle = 270f

        drawArc(
            color = colorScheme.onSurfaceVariantSummary.copy(alpha = 0.2f),
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(20.dp.toPx(), 20.dp.toPx()),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
        )

        val speedRatio = minOf(speed / 120.0, 1.0)
        val activeAngle = speedRatio * sweepAngle

        drawArc(
            color = when {
                speedRatio < 0.3 -> Color(0xFFFF1744)
                speedRatio < 0.6 -> Color(0xFFFFAB00)
                else -> Color(0xFF0066FF)
            },
            startAngle = startAngle,
            sweepAngle = activeAngle.toFloat(),
            useCenter = false,
            topLeft = Offset(20.dp.toPx(), 20.dp.toPx()),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
        )

        for (i in 0..6) {
            val angle = startAngle + (i * sweepAngle / 6)
            val radian = Math.toRadians(angle.toDouble()).toFloat()
            val innerRadius = radius - 20.dp.toPx()
            val outerRadius = radius

            val cosValue = Math.cos(radian.toDouble())
            val sinValue = Math.sin(radian.toDouble())

            drawLine(
                color = colorScheme.onSurface,
                start = Offset(
                    (center.x + cosValue * innerRadius).toFloat(),
                    (center.y + sinValue * innerRadius).toFloat()
                ),
                end = Offset(
                    (center.x + cosValue * outerRadius).toFloat(),
                    (center.y + sinValue * outerRadius).toFloat()
                ),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        val pointerAngle = startAngle + activeAngle
        val pointerRadian = Math.toRadians(pointerAngle.toDouble()).toFloat()
        val pointerLength = radius - 30.dp.toPx()

        drawLine(
            color = Color(0xFFFF1744),
            start = center,
            end = Offset(
                (center.x + Math.cos(pointerRadian.toDouble()) * pointerLength).toFloat(),
                (center.y + Math.sin(pointerRadian.toDouble()) * pointerLength).toFloat()
            ),
            strokeWidth = 4.dp.toPx(),
            cap = StrokeCap.Round
        )

        drawCircle(
            color = Color(0xFFFF1744),
            center = center,
            radius = 8.dp.toPx()
        )

        drawCircle(
            color = Color.White,
            center = center,
            radius = 4.dp.toPx()
        )
    }
}

@Composable
fun GpsSignalCard(
    accuracy: Float?,
    isDark: Boolean = false,
    powerSaving: Boolean = false,
    accelerometerEnabled: Boolean = false,
    isNotStarted: Boolean = false
) {
    val cardColor: Color
    val iconColor: Color
    val title: String
    val summary: String
    val warning: String?

    if (powerSaving && accelerometerEnabled) {
        cardColor = if (isDark) Color(0xFF3D3514) else Color(0xFFFFF9C4)
        iconColor = Color(0xFFFDD835)
        title = "已打开省电模式"
        summary = "使用加速度计辅助测速"
        warning = "计算数据可能有实际偏差, 记录功能不可用"
    } else if (powerSaving) {
        val strength = when {
            accuracy == null -> 0
            accuracy < 10 -> 5
            accuracy < 20 -> 4
            accuracy < 30 -> 3
            accuracy < 50 -> 2
            else -> 1
        }
        val accuracyText = accuracy?.let { String.format("%.1f", it) + "m" } ?: "--"

        when (strength) {
            4, 5 -> {
                cardColor = if (isDark) Color(0xFF1A3825) else Color(0xFFDFFAE4)
                iconColor = Color(0xFF36D167)
                title = "GPS 正常"
                summary = "精度: $accuracyText | 信号 ${strength}级"
                warning = "已开启省电模式，采集间隔延长，记录功能不可用"
            }
            3 -> {
                cardColor = if (isDark) Color(0xFF3D3514) else Color(0xFFFFF9C4)
                iconColor = Color(0xFFFDD835)
                title = "GPS 信号较弱"
                summary = "精度: $accuracyText | 信号 ${strength}级"
                warning = "已开启省电模式，采集间隔延长，记录功能不可用"
            }
            else -> {
                cardColor = if (isDark) Color(0xFF3B1414) else Color(0xFFFFEBEE)
                iconColor = Color(0xFFFF5252)
                title = "GPS 信号差"
                summary = "精度: $accuracyText | 信号 ${strength}级"
                warning = "已开启省电模式，采集间隔延长，记录功能不可用"
            }
        }
    } else if (isNotStarted) {
        cardColor = if (isDark) Color(0xFF1A1A1A) else Color(0xFFF0F0F0)
        iconColor = if (isDark) Color(0xFF666666) else Color(0xFFCCCCCC)
        title = "GPS 未启动"
        summary = "开始测速后自动获取GPS信号"
        warning = null
    } else {
        val strength = when {
            accuracy == null -> 0
            accuracy < 10 -> 5
            accuracy < 20 -> 4
            accuracy < 30 -> 3
            accuracy < 50 -> 2
            else -> 1
        }
        val accuracyText = accuracy?.let { String.format("%.1f", it) + "m" } ?: "--"

        when (strength) {
            4, 5 -> {
                cardColor = if (isDark) Color(0xFF1A3825) else Color(0xFFDFFAE4)
                iconColor = Color(0xFF36D167)
                title = "GPS 正常"
                summary = "精度: $accuracyText | 信号 ${strength}级"
                warning = null
            }
            3 -> {
                cardColor = if (isDark) Color(0xFF3D3514) else Color(0xFFFFF9C4)
                iconColor = Color(0xFFFDD835)
                title = "GPS 信号较弱"
                summary = "精度: $accuracyText | 信号 ${strength}级"
                warning = null
            }
            else -> {
                cardColor = if (isDark) Color(0xFF3B1414) else Color(0xFFFFEBEE)
                iconColor = Color(0xFFFF5252)
                title = "GPS 信号差"
                summary = "精度: $accuracyText | 信号 ${strength}级"
                warning = "速度和位置测量可能不准确"
            }
        }
    }

    val textColor = if (isDark) Color.White else Color.Black

    Card(
        modifier = Modifier
            .then(if (isNotStarted) Modifier.fillMaxWidth() else Modifier.width(180.dp))
            .height(160.dp),
        colors = CardDefaults.defaultColors(color = cardColor)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(38.dp, 45.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Icon(
                    modifier = Modifier.size(120.dp),
                    imageVector = when {
                        powerSaving && accelerometerEnabled -> Icons.Rounded.Warning
                        powerSaving -> Icons.Rounded.Warning
                        isNotStarted -> Icons.Rounded.ErrorOutline
                        accuracy == null -> Icons.Rounded.ErrorOutline
                        accuracy < 30 -> Icons.Rounded.CheckCircleOutline
                        accuracy < 50 -> Icons.Rounded.Warning
                        else -> Icons.Rounded.ErrorOutline
                    },
                    tint = iconColor.copy(alpha = 0.8f),
                    contentDescription = null
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(all = 16.dp)
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = summary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor
                )
                warning?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = it,
                        fontSize = 12.sp,
                        color = textColor.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, unit: String, compact: Boolean = false) {
    Card(
        modifier = Modifier
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
                text = title,
                style = TextStyle(
                    fontSize = if (compact) 11.sp else 12.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
            )
            Text(
                text = value,
                style = TextStyle(
                    fontSize = if (compact) 18.sp else 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MiuixTheme.colorScheme.onSurface
                )
            )
            Text(
                text = unit,
                style = TextStyle(
                    fontSize = if (compact) 9.sp else 10.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
            )
        }
    }
}

@Composable
fun DividerRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontSize = 12.sp,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
            )
        )
        Text(
            text = value,
            style = TextStyle(
                fontSize = 12.sp,
                color = MiuixTheme.colorScheme.onSurface,
                textAlign = TextAlign.End
            )
        )
    }
}

private fun formatSpeed(speed: Double, unit: SpeedUnit): String {
    return when (unit) {
        SpeedUnit.KMH -> String.format("%.0f", speed)
        SpeedUnit.MS -> String.format("%.1f", speed / 3.6)
        SpeedUnit.MPH -> String.format("%.0f", speed * 0.621371)
    }
}

private fun formatDistance(distance: Double): String {
    return String.format("%.2f", distance)
}

private fun getSpeedUnitString(unit: SpeedUnit): String {
    return when (unit) {
        SpeedUnit.KMH -> "km/h"
        SpeedUnit.MS -> "m/s"
        SpeedUnit.MPH -> "mph"
    }
}
