package com.example.assing4_1

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.Lifecycle

data class LogEntry(
    val timestamp: String,
    val event: Lifecycle.Event,
    val color: Color,
)
