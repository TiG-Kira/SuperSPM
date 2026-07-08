package com.kira.superspm.ui

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.kira.superspm.data.model.PluginConfig
import com.kira.superspm.service.LocationService
import com.kira.superspm.utils.PluginManager
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PluginWebPage(
    pluginDirName: String,
    pluginName: String,
    onBack: () -> Unit
) {
    val scrollBehavior = MiuixScrollBehavior()
    val backgroundColor = MiuixTheme.colorScheme.background
    val context = androidx.compose.ui.platform.LocalContext.current

    var webView by remember { mutableStateOf<WebView?>(null) }
    var isRunning by remember { mutableStateOf(true) }

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
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            AndroidView(
                factory = { ctx ->
                    val wv = WebView(ctx)
                    wv.settings.javaScriptEnabled = true
                    wv.settings.domStorageEnabled = true
                    wv.settings.allowFileAccess = true
                    wv.settings.allowContentAccess = true
                    wv.webViewClient = WebViewClient()
                    wv.setBackgroundColor(android.graphics.Color.TRANSPARENT)

                    wv.addJavascriptInterface(SpeedDataBridge(), "SpeedData")

                    val pluginDir = PluginManager.getPluginDir(ctx, pluginDirName)
                    if (pluginDir != null) {
                        val configFile = java.io.File(pluginDir, "plugin.json")
                        val config = if (configFile.exists()) {
                            try { Json.decodeFromString<PluginConfig>(configFile.readText()) } catch (e: Exception) { null }
                        } else null

                        if (config != null) {
                            wv.addJavascriptInterface(PluginConfigBridge(config), "PluginConfig")
                        }

                        val entryFile = java.io.File(pluginDir, config?.entry ?: "index.html")
                        if (entryFile.exists()) {
                            wv.loadUrl("file://${entryFile.absolutePath}")
                        } else {
                            wv.loadData(
                                "<html><body><h1>Error</h1><p>入口文件 ${config?.entry ?: "index.html"} 未找到</p></body></html>",
                                "text/html",
                                "UTF-8"
                            )
                        }
                    }

                    webView = wv
                    wv
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    // 速度数据推送
    LaunchedEffect(isRunning) {
        while (isRunning) {
            webView?.let { wv ->
                if (!wv.isAttachedToWindow) return@let
                val speed = LocationService.getCurrentSpeed()
                val max = LocationService.getMaxSpeed()
                val avg = LocationService.getAvgSpeed()
                val dist = LocationService.getTotalDistance()
                val recording = LocationService.isRecording()
                val sensor = LocationService.isSensorMode()
                val lat = LocationService.getCurrentLatitude()
                val lon = LocationService.getCurrentLongitude()
                val acc = LocationService.getCurrentAccuracy()
                val sensorAccel = LocationService.getSensorAcceleration()
                val sensorVel = LocationService.getSensorVelocity()
                val duration = LocationService.getRecordingDuration()
                val points = LocationService.getDataPoints()

                wv.evaluateJavascript(
                    """
                    if (window.onSpeedUpdate) {
                        window.onSpeedUpdate({
                            currentSpeed: $speed,
                            maxSpeed: $max,
                            avgSpeed: $avg,
                            totalDistance: $dist,
                            isRecording: $recording,
                            isSensorMode: $sensor,
                            latitude: $lat,
                            longitude: $lon,
                            accuracy: $acc,
                            sensorAcceleration: $sensorAccel,
                            sensorVelocity: $sensorVel,
                            recordingDuration: $duration,
                            dataPoints: $points
                        });
                    }
                    """.trimIndent(),
                    null
                )
            }
            delay(500)
        }
    }

    // 清理 WebView
    DisposableEffect(Unit) {
        onDispose {
            isRunning = false
            webView?.let { wv ->
                wv.stopLoading()
                wv.removeJavascriptInterface("SpeedData")
                wv.removeJavascriptInterface("PluginConfig")
                wv.destroy()
            }
        }
    }
}

private class SpeedDataBridge {
    @android.webkit.JavascriptInterface
    fun getCurrentSpeed(): Double = LocationService.getCurrentSpeed()

    @android.webkit.JavascriptInterface
    fun getMaxSpeed(): Double = LocationService.getMaxSpeed()

    @android.webkit.JavascriptInterface
    fun getAvgSpeed(): Double = LocationService.getAvgSpeed()

    @android.webkit.JavascriptInterface
    fun getTotalDistance(): Double = LocationService.getTotalDistance()

    @android.webkit.JavascriptInterface
    fun isRecording(): Boolean = LocationService.isRecording()

    @android.webkit.JavascriptInterface
    fun isSensorMode(): Boolean = LocationService.isSensorMode()

    @android.webkit.JavascriptInterface
    fun getLatitude(): Double = LocationService.getCurrentLatitude()

    @android.webkit.JavascriptInterface
    fun getLongitude(): Double = LocationService.getCurrentLongitude()

    @android.webkit.JavascriptInterface
    fun getAccuracy(): Float = LocationService.getCurrentAccuracy()

    @android.webkit.JavascriptInterface
    fun getSensorAcceleration(): Double = LocationService.getSensorAcceleration()

    @android.webkit.JavascriptInterface
    fun getSensorVelocity(): Double = LocationService.getSensorVelocity()

    @android.webkit.JavascriptInterface
    fun getRecordingDuration(): Long = LocationService.getRecordingDuration()

    @android.webkit.JavascriptInterface
    fun getDataPoints(): Int = LocationService.getDataPoints()

    @android.webkit.JavascriptInterface
    fun getTimestamp(): Long = System.currentTimeMillis()
}

private class PluginConfigBridge(private val config: PluginConfig) {
    @android.webkit.JavascriptInterface
    fun getName(): String = config.name

    @android.webkit.JavascriptInterface
    fun getVersion(): String = config.version

    @android.webkit.JavascriptInterface
    fun getAuthor(): String = config.author

    @android.webkit.JavascriptInterface
    fun getDescription(): String = config.description

    @android.webkit.JavascriptInterface
    fun getType(): String = config.type.name

    @android.webkit.JavascriptInterface
    fun getEntry(): String = config.entry
}
