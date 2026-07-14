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
import androidx.compose.ui.input.pointer.pointerInput
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
import androidx.compose.animation.animateColorAsState
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
import top.yukonga.miuix.kmp.basic.TabRowWithContour
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
import com.kira.superspm.viewmodel.SpeedMode
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
    var displayAccuracy by remember { mutableStateOf<Float?>(null) }
    var lastAccuracyChangeTime by remember { mutableStateOf(0L) }
    val ACCURACY_DEBOUNCE_MS = 2000L

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

    var lastAccUpdateTime by remember { mutableStateOf(0L) }

    val selectedMode = viewModel.selectedMode

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
            val currentTime = System.currentTimeMillis()
            if (displayAccuracy == null) {
                displayAccuracy = accuracy
                lastAccuracyChangeTime = currentTime
            } else {
                val currentStrength = when {
                    accuracy == null -> 0
                    accuracy < 10 -> 5
                    accuracy < 20 -> 4
                    accuracy < 30 -> 3
                    accuracy < 50 -> 2
                    else -> 1
                }
                val displayStrength = when {
                    displayAccuracy == null -> 0
                    displayAccuracy!! < 10 -> 5
                    displayAccuracy!! < 20 -> 4
                    displayAccuracy!! < 30 -> 3
                    displayAccuracy!! < 50 -> 2
                    else -> 1
                }
                if (currentStrength != displayStrength) {
                    if (currentTime - lastAccuracyChangeTime >= ACCURACY_DEBOUNCE_MS) {
                        displayAccuracy = accuracy
                        lastAccuracyChangeTime = currentTime
                    }
                } else {
                    lastAccuracyChangeTime = currentTime
                    displayAccuracy = accuracy
                }
            }
        }

        LocationService.onServiceStopped = {
            viewModel.reset()
        }

        LocationService.onError = { message ->
            errorMessage = message
            showErrorDialog = true
        }
    }

    LaunchedEffect(status, selectedMode) {
        if (status != SpeedometerViewModel.RecordingStatus.NOT_STARTED && selectedMode == SpeedMode.GPS) {
            LocationService.requestSingleUpdate(context)
        }
    }

    LaunchedEffect(status, selectedMode) {
        if (selectedMode == SpeedMode.SENSOR && status != SpeedometerViewModel.RecordingStatus.NOT_STARTED) {
            AccelerometerService.start(context)
            AccelerometerService.onAccelerationUpdate = { x, y, z ->
                accX = x
                accY = y
                accZ = z
            }
            AccelerometerService.onSpeedEstimateUpdate = lambda@{ speed ->
                accEstimatedSpeed = speed

                val currentTime = System.currentTimeMillis()
                if (lastAccUpdateTime == 0L) {
                    lastAccUpdateTime = currentTime
                    return@lambda
                }
                val deltaTime = (currentTime - lastAccUpdateTime) / 1000.0
                lastAccUpdateTime = currentTime

                viewModel.updateSpeed(speed)
                val distance = speed / 3.6 * deltaTime / 1000.0
                viewModel.addDistance(distance)
                LocationService.updateWithSensorData(speed, distance)
            }
            AccelerometerService.onError = { message ->
                errorMessage = message
                showErrorDialog = true
            }
        } else {
            AccelerometerService.stop()
            lastAccUpdateTime = 0L
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
            if (LocationService.isSensorMode()) {
                viewModel.changeSpeedMode(SpeedMode.SENSOR)
            }
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
                                if (selectedMode == SpeedMode.GPS && !hasLocationPermission) {
                                    showPermissionDialog = true
                                    return@IconButton
                                }
                                viewModel.startRecording()
                                LocationService.updateSettings(
                                    settingsViewModel.powerSaving,
                                    settingsViewModel.refreshTime
                                )
                                val intent = android.content.Intent(context, LocationService::class.java)
                                intent.putExtra("saveRecord", false)
                                intent.putExtra("sensorMode", selectedMode == SpeedMode.SENSOR)
                                context.startForegroundService(intent)
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = context.getString(R.string.start),
                                tint = MiuixTheme.colorScheme.onSurface,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        IconButton(
                            onClick = {
                                if (selectedMode == SpeedMode.GPS && !hasLocationPermission) {
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
                                intent.putExtra("sensorMode", selectedMode == SpeedMode.SENSOR)
                                context.startForegroundService(intent)
                            },
                            modifier = Modifier.padding(end = 8.dp),
                            enabled = selectedMode == SpeedMode.SENSOR || !settingsViewModel.powerSaving
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Circle,
                                contentDescription = context.getString(R.string.record_and_start),
                                tint = MiuixTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
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
                                        val intent = android.content.Intent(context, LocationService::class.java)
                                        intent.putExtra("sensorMode", selectedMode == SpeedMode.SENSOR)
                                        context.startForegroundService(intent)
                                    }
                                    else -> {}
                                }
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(
                                imageVector = if (status == SpeedometerViewModel.RecordingStatus.RECORDING) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = if (status == SpeedometerViewModel.RecordingStatus.RECORDING) context.getString(R.string.pause) else context.getString(R.string.resume),
                                tint = MiuixTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        IconButton(
                            onClick = {
                                val record = LocationService.finishRecording()
                                if (record != null) {
                                    onRecordSaved()
                                }
                                viewModel.reset()
                                context.stopService(android.content.Intent(context, LocationService::class.java))
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Stop,
                                contentDescription = context.getString(R.string.stop),
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
                    if (settingsViewModel.accelerometerEnabled) {
                        val tabs = listOf(context.getString(R.string.speedometer_tab_gps), context.getString(R.string.speedometer_tab_sensor))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .then(if (status != SpeedometerViewModel.RecordingStatus.NOT_STARTED) Modifier.pointerInput(Unit) {} else Modifier)
                        ) {
                            TabRowWithContour(
                                tabs = tabs,
                                selectedTabIndex = if (selectedMode == SpeedMode.GPS) 0 else 1,
                                onTabSelected = { index ->
                                    if (status == SpeedometerViewModel.RecordingStatus.NOT_STARTED) {
                                        viewModel.changeSpeedMode(if (index == 0) SpeedMode.GPS else SpeedMode.SENSOR)
                                    }
                                },
                                modifier = Modifier.align(Alignment.CenterStart)
                            )
                        }
                    }

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
                                    text = context.getString(R.string.not_started),
                                    style = TextStyle(
                                        fontSize = 48.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MiuixTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = context.getString(R.string.select_mode_to_start),
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
                    if (selectedMode == SpeedMode.SENSOR && status != SpeedometerViewModel.RecordingStatus.NOT_STARTED) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Card(
                                modifier = Modifier
                                    .width(180.dp)
                                    .height(160.dp),
                                colors = CardDefaults.defaultColors(color = if (isDark) Color(0xFF1A3825) else Color(0xFFDFFAE4))
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
                                            imageVector = Icons.Rounded.CheckCircleOutline,
                                            tint = Color(0xFF36D167).copy(alpha = 0.8f),
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
                                            text = context.getString(R.string.use_sensor),
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isDark) Color.White else Color.Black
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            modifier = Modifier.fillMaxWidth(),
                                            text = context.getString(R.string.no_sensor_gps),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (isDark) Color.White else Color.Black
                                        )
                                    }
                                }
                            }

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
                    } else if (selectedMode == SpeedMode.GPS) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (status == SpeedometerViewModel.RecordingStatus.NOT_STARTED) {
                                GpsSignalCard(
                                    accuracy = displayAccuracy,
                                    isDark = isDark,
                                    powerSaving = settingsViewModel.powerSaving,
                                    accelerometerEnabled = false,
                                    isNotStarted = true,
                                    context = context
                                )
                            } else {
                                GpsSignalCard(
                                    accuracy = displayAccuracy,
                                    isDark = isDark,
                                    powerSaving = settingsViewModel.powerSaving,
                                    accelerometerEnabled = false,
                                    context = context
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
                }

                item {
                    if (selectedMode == SpeedMode.SENSOR) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            colors = CardDefaults.defaultColors(color = if (isDark) Color(0xFF1A1A1A) else Color(0xFFF0F0F0))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = context.getString(R.string.welcome_sensor_speed),
                                    style = TextStyle(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MiuixTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = context.getString(R.string.sensor_speed_desc),
                                    style = TextStyle(
                                        fontSize = 13.sp,
                                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                    ),
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    if (selectedMode == SpeedMode.GPS) {
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
                                    text = context.getString(R.string.get_location_after_start),
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                    ),
                                    modifier = Modifier.align(Alignment.Center)
                                )
                                Icon(
                                    imageVector = Icons.Rounded.Warning,
                                    contentDescription = context.getString(R.string.hint),
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
                                        contentDescription = context.getString(R.string.refresh),
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
                                    text = addressText.ifEmpty { context.getString(R.string.getting_address) },
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
    val animatedSpeed = androidx.compose.animation.core.animateFloatAsState(
        targetValue = speed.toFloat(),
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 300, easing = androidx.compose.animation.core.EaseOutQuad)
    ).value

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

        val speedRatio = minOf(animatedSpeed / 120.0f, 1.0f)
        val activeAngle = speedRatio * sweepAngle

        drawArc(
            color = when {
                speedRatio < 0.3f -> Color(0xFFFF1744)
                speedRatio < 0.6f -> Color(0xFFFFAB00)
                else -> Color(0xFF0066FF)
            },
            startAngle = startAngle,
            sweepAngle = activeAngle,
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
    isNotStarted: Boolean = false,
    context: android.content.Context
) {
    val cardColor: Color
    val iconColor: Color
    val title: String
    val summary: String
    val warning: String?

    if (powerSaving && accelerometerEnabled) {
        cardColor = if (isDark) Color(0xFF3D3514) else Color(0xFFFFF9C4)
        iconColor = Color(0xFFFDD835)
        title = context.getString(R.string.power_saving_enabled)
        summary = context.getString(R.string.power_saving_sensor)
        warning = context.getString(R.string.power_saving_warning)
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
                title = context.getString(R.string.gps_normal)
                summary = "${context.getString(R.string.accuracy)}: $accuracyText | ${context.getString(R.string.signal)} ${strength}${context.getString(R.string.signal_level)}"
                warning = context.getString(R.string.power_saving_gps_warning)
            }
            3 -> {
                cardColor = if (isDark) Color(0xFF3D3514) else Color(0xFFFFF9C4)
                iconColor = Color(0xFFFDD835)
                title = context.getString(R.string.gps_weak)
                summary = "${context.getString(R.string.accuracy)}: $accuracyText | ${context.getString(R.string.signal)} ${strength}${context.getString(R.string.signal_level)}"
                warning = context.getString(R.string.power_saving_gps_warning)
            }
            else -> {
                cardColor = if (isDark) Color(0xFF3B1414) else Color(0xFFFFEBEE)
                iconColor = Color(0xFFFF5252)
                title = context.getString(R.string.gps_poor)
                summary = "${context.getString(R.string.accuracy)}: $accuracyText | ${context.getString(R.string.signal)} ${strength}${context.getString(R.string.signal_level)}"
                warning = context.getString(R.string.power_saving_gps_warning)
            }
        }
    } else if (isNotStarted) {
        cardColor = if (isDark) Color(0xFF1A1A1A) else Color(0xFFF0F0F0)
        iconColor = if (isDark) Color(0xFF666666) else Color(0xFFCCCCCC)
        title = context.getString(R.string.gps_not_started)
        summary = context.getString(R.string.gps_not_started_desc)
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
                title = context.getString(R.string.gps_normal)
                summary = "${context.getString(R.string.accuracy)}: $accuracyText | ${context.getString(R.string.signal)} ${strength}${context.getString(R.string.signal_level)}"
                warning = null
            }
            3 -> {
                cardColor = if (isDark) Color(0xFF3D3514) else Color(0xFFFFF9C4)
                iconColor = Color(0xFFFDD835)
                title = context.getString(R.string.gps_weak)
                summary = "${context.getString(R.string.accuracy)}: $accuracyText | ${context.getString(R.string.signal)} ${strength}${context.getString(R.string.signal_level)}"
                warning = null
            }
            else -> {
                cardColor = if (isDark) Color(0xFF3B1414) else Color(0xFFFFEBEE)
                iconColor = Color(0xFFFF5252)
                title = context.getString(R.string.gps_poor)
                summary = "${context.getString(R.string.accuracy)}: $accuracyText | ${context.getString(R.string.signal)} ${strength}${context.getString(R.string.signal_level)}"
                warning = context.getString(R.string.gps_warning)
            }
        }
    }

    val textColor = if (isDark) Color.White else Color.Black

    val animatedCardColor = androidx.compose.animation.animateColorAsState(
        targetValue = cardColor,
        animationSpec = androidx.compose.animation.core.tween(500)
    )

    Card(
        modifier = Modifier
            .then(if (isNotStarted) Modifier.fillMaxWidth() else Modifier.width(180.dp))
            .height(160.dp),
        colors = CardDefaults.defaultColors(color = animatedCardColor.value)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(38.dp, 45.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Icon(
                    modifier = Modifier.size(120.dp).alpha(0.8f),
                    imageVector = when {
                        powerSaving && accelerometerEnabled -> Icons.Rounded.Warning
                        powerSaving -> Icons.Rounded.Warning
                        isNotStarted -> Icons.Rounded.ErrorOutline
                        accuracy == null -> Icons.Rounded.ErrorOutline
                        accuracy < 30 -> Icons.Rounded.CheckCircleOutline
                        accuracy < 50 -> Icons.Rounded.Warning
                        else -> Icons.Rounded.ErrorOutline
                    },
                    tint = iconColor,
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

