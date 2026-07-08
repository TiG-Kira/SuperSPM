package com.kira.superspm.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.kira.superspm.ui.theme.SuperSPMTheme
import com.kira.superspm.viewmodel.SettingsViewModel
import org.koin.androidx.compose.getViewModel
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import org.koin.android.ext.android.inject
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    private val hasLocationPermissionState = mutableStateOf(false)
    private val hasBackgroundLocationPermissionState = mutableStateOf(false)
    private val hasNotificationPermissionState = mutableStateOf(false)
    private val systemDarkModeState = mutableStateOf(false)
    private val dataStore: DataStore<Preferences> by inject()

    private val requestLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            val backgroundGranted = permissions[Manifest.permission.ACCESS_BACKGROUND_LOCATION] == true
            val notificationGranted = permissions[Manifest.permission.POST_NOTIFICATIONS] == true
            
            hasLocationPermissionState.value = fineGranted || coarseGranted
            hasBackgroundLocationPermissionState.value = backgroundGranted
            hasNotificationPermissionState.value = notificationGranted
        }

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            hasNotificationPermissionState.value = granted
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applySavedLanguage()
        checkPermissions()
        updateSystemDarkMode()
        requestPermissionsIfNeeded()
        setContent {
            MainApp()
        }
    }

    private fun applySavedLanguage() {
        runBlocking {
            val prefs = dataStore.data.first()
            val language = prefs[stringPreferencesKey("app_language")] ?: "zh"
            val locale = if (language == "en") java.util.Locale.ENGLISH else java.util.Locale.CHINESE
            val config = resources.configuration.apply {
                setLocale(locale)
            }
            resources.updateConfiguration(config, resources.displayMetrics)
        }
    }

    override fun onResume() {
        super.onResume()
        checkPermissions()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateSystemDarkMode()
    }

    private fun updateSystemDarkMode() {
        systemDarkModeState.value = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }

    private fun checkPermissions() {
        hasLocationPermissionState.value = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

        hasBackgroundLocationPermissionState.value = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        hasNotificationPermissionState.value = ContextCompat.checkSelfPermission(
            this, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissionsIfNeeded() {
        val permissionsToRequest = mutableListOf<String>()
        
        if (!hasLocationPermissionState.value) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (!hasBackgroundLocationPermissionState.value) {
            permissionsToRequest.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        if (!hasNotificationPermissionState.value && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            requestLocationPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    fun requestPermissions() {
        requestLocationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        )
    }

    @Composable
    fun MainApp() {
        val settingsViewModel: SettingsViewModel = getViewModel()
        
        val darkMode by settingsViewModel::darkMode
        val followSystem by settingsViewModel::followSystem
        val powerSaving by settingsViewModel::powerSaving
        val systemDarkMode by remember { systemDarkModeState }

        val hasLocationPermission by remember { hasLocationPermissionState }

        val isDark = if (followSystem) {
            systemDarkMode
        } else {
            powerSaving || darkMode
        }

        LaunchedEffect(followSystem, systemDarkMode) {
            if (followSystem) {
                settingsViewModel.syncSystemDarkMode(systemDarkMode)
            }
        }

        LaunchedEffect(isDark) {
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            insetsController.isAppearanceLightStatusBars = !isDark
            insetsController.isAppearanceLightNavigationBars = !isDark
        }

        SuperSPMTheme(
            darkMode = isDark,
            followSystem = followSystem
        ) {
            AppNavHost(
                hasLocationPermission = hasLocationPermission,
                onPermissionRequest = { requestPermissions() },
                isDark = isDark
            )
        }
    }

    companion object {
        fun hasLocationPermission(context: Context): Boolean {
            return ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
        }
    }
}