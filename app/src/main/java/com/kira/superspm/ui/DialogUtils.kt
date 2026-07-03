package com.kira.superspm.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color

@Composable
fun DialogOverlay(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .alpha(animateFloatAsState(targetValue = 1f, tween(300)).value)
                .clickable { }
        )
        content()
    }
}

@Composable
fun AnimatedDialogContent(content: @Composable () -> Unit) {
    val scale by animateFloatAsState(targetValue = 1f, tween(300, easing = EaseOutBack))
    val alpha by animateFloatAsState(targetValue = 1f, tween(300))
    
    AnimatedVisibility(visible = true) {
        Box(
            modifier = Modifier
                .scale(scale)
                .alpha(alpha)
        ) {
            content()
        }
    }
}