package com.kira.superspm.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PluginConfig(
    val name: String,
    val version: String,
    val author: String,
    val description: String,
    val type: PluginType = PluginType.WEB_UI,
    val entry: String = "index.html",
    val permissions: List<String> = emptyList(),
    val requiresRestart: Boolean = false
)

@Serializable
enum class PluginType {
    WEB_UI,
    NATIVE
}
