package com.kira.superspm.ui

import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.navigation.NavHostController
import com.kira.superspm.ui.components.SearchBar
import com.kira.superspm.viewmodel.SettingsViewModel
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Extension
import top.yukonga.miuix.kmp.theme.MiuixTheme
import androidx.compose.ui.platform.LocalContext
import com.kira.superspm.R
import androidx.compose.ui.graphics.vector.ImageVector
import org.koin.androidx.compose.getViewModel

@Composable
fun SettingsScreen(
    isDark: Boolean = false,
    navController: NavHostController,
    hasUpdate: Boolean = false,
    showRedDot: Boolean = false,
    onRedDotConsumed: () -> Unit = {}
) {
    val viewModel: SettingsViewModel = getViewModel()
    val context = LocalContext.current

    val scrollBehavior = MiuixScrollBehavior()
    val backgroundColor = getPageBackgroundColor(isDark)

    var searchQuery by remember { mutableStateOf("") }

    val settingsItems = listOf(
        Triple(Icons.Filled.Palette, context.getString(R.string.appearance_display), context.getString(R.string.appearance_subtitle)) to { navController.navigate("settings/appearance") },
        Triple(Icons.Filled.Speed, context.getString(R.string.speed_method_logic), context.getString(R.string.location_subtitle)) to { navController.navigate("settings/location") },
        Triple(Icons.Filled.History, context.getString(R.string.history_management), context.getString(R.string.history_subtitle)) to { navController.navigate("settings/history") },
        Triple(Icons.Filled.Extension, context.getString(R.string.plugin_management), context.getString(R.string.plugin_subtitle)) to { navController.navigate("settings/plugins") },
        Triple(Icons.Filled.Info, context.getString(R.string.about), if (hasUpdate) context.getString(R.string.new_version_found) else context.getString(R.string.about_subtitle)) to {
            onRedDotConsumed()
            navController.navigate("about")
        }
    )

    val filteredItems = settingsItems.filter {
        val (icon, title, subtitle) = it.first
        searchQuery.isEmpty() || title.contains(searchQuery, ignoreCase = true) || subtitle.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = context.getString(R.string.title_settings),
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
            item {
                SearchBar(hint = context.getString(R.string.search_settings), onSearch = { searchQuery = it })
            }

            if (filteredItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = context.getString(R.string.no_matching_settings),
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                            )
                        )
                    }
                }
            } else {
                filteredItems.forEachIndexed { index, item ->
                    val (icon, title, subtitle) = item.first
                    val onClick = item.second
                    item {
                        SettingNavItem(
                            icon = icon,
                            title = title,
                            subtitle = subtitle,
                            hasRedDot = index == 4 && hasUpdate && showRedDot,
                            onClick = onClick,
                            isUpdateItem = index == 4 && hasUpdate
                        )
                    }
                }
            }

            item {
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(120.dp))
            }
        }
    }
}

@Composable
fun SettingNavItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    hasRedDot: Boolean = false,
    onClick: () -> Unit,
    isUpdateItem: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MiuixTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Column(
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Text(
                        text = title,
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MiuixTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = subtitle,
                        style = TextStyle(
                            fontSize = 13.sp,
                            color = if (isUpdateItem) MiuixTheme.colorScheme.error else MiuixTheme.colorScheme.onSurfaceVariantSummary
                        ),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (hasRedDot) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color(0xFFFF1744), CircleShape)
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))
                }
                Icon(
                    imageVector = Icons.Filled.ArrowRight,
                    contentDescription = "arrow",
                    tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
