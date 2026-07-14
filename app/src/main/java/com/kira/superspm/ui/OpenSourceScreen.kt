package com.kira.superspm.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.kira.superspm.R
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import top.yukonga.miuix.kmp.theme.MiuixTheme

import androidx.compose.ui.draw.clip

@Composable
fun OpenSourceScreen(isDark: Boolean, onBack: () -> Unit) {
    val context = LocalContext.current
    val scrollBehavior = MiuixScrollBehavior()
    val backgroundColor = getPageBackgroundColor(isDark)

    Scaffold(
        topBar = {
            TopAppBar(
                title = context.getString(R.string.open_source_projects),
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
                .background(backgroundColor)
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = PaddingValues(top = paddingValues.calculateTopPadding(), bottom = 120.dp)
        ) {
            item {
                OpenSourceCard(
                    title = "MiuiX",
                    description = context.getString(R.string.miuix_desc),
                    url = "https://github.com/compose-miuix-ui/miuix",
                    context = context
                )
            }

            item {
                OpenSourceCard(
                    title = "HyperCeiler",
                    description = context.getString(R.string.hyperceiler_desc),
                    url = "https://github.com/ReChronoRain/HyperCeiler",
                    context = context
                )
            }

            item {
                OpenSourceCard(
                    title = "Jetpack Compose",
                    description = context.getString(R.string.jetpack_compose_desc),
                    url = "https://developer.android.com/jetpack/compose",
                    context = context
                )
            }

            item {
                OpenSourceCard(
                    title = "Room",
                    description = context.getString(R.string.room_desc),
                    url = "https://developer.android.com/jetpack/androidx/releases/room",
                    context = context
                )
            }

            item {
                OpenSourceCard(
                    title = "Koin",
                    description = context.getString(R.string.koin_desc),
                    url = "https://insert-koin.io/",
                    context = context
                )
            }

            item {
                OpenSourceCard(
                    title = "Coil",
                    description = context.getString(R.string.coil_desc),
                    url = "https://coil-kt.github.io/coil/",
                    context = context
                )
            }

            item {
                OpenSourceCard(
                    title = "Timber",
                    description = context.getString(R.string.timber_desc),
                    url = "https://github.com/JakeWharton/timber",
                    context = context
                )
            }

            item {
                OpenSourceCard(
                    title = "Kotlin Serialization",
                    description = context.getString(R.string.kotlin_serialization_desc),
                    url = "https://github.com/Kotlin/kotlinx.serialization",
                    context = context
                )
            }

            item {
                OpenSourceCard(
                    title = "Kotlinx Datetime",
                    description = context.getString(R.string.kotlin_datetime_desc),
                    url = "https://github.com/Kotlin/kotlinx-datetime",
                    context = context
                )
            }

            item {
                OpenSourceCard(
                    title = "Google Play Services Location",
                    description = context.getString(R.string.google_location_desc),
                    url = "https://developers.google.com/android/reference/com/google/android/gms/location/package-summary",
                    context = context
                )
            }
        }
    }
}

@Composable
fun OpenSourceCard(
    title: String,
    description: String,
    url: String,
    context: android.content.Context
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MiuixTheme.colorScheme.onSurface
                ),
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = description,
                style = TextStyle(
                    fontSize = 13.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = context.getString(R.string.click_visit_project),
                style = TextStyle(
                    fontSize = 13.sp,
                    color = MiuixTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .clickable {
                        val intent = android.content.Intent(
                            android.content.Intent.ACTION_VIEW,
                            android.net.Uri.parse(url)
                        )
                        context.startActivity(intent)
                    }
            )
        }
    }
}
