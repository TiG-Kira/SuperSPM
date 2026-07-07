package com.kira.superspm.plugin.data

data class AnalysisResult(
    val title: String,
    val description: String,
    val icon: String,
    val advice: String,
    val metrics: Map<String, String> = emptyMap()
)
