package com.example.assing4_1

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TrackerModelView {
    // private mutable flow holding our logs
    private val _logEntries = MutableStateFlow<List<LogEntry>>(emptyList())
    // public immutable flow that the UI can observe
    val logEntries = _logEntries.asStateFlow()
    val showSnackbarOnTransition = MutableStateFlow(true)

    // method to add new log event to list of log entries
    fun addLog(event: Lifecycle.Event) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val color = getEventColor(event)
        val newLog = LogEntry(timestamp, event, color)
        _logEntries.value = _logEntries.value + newLog // prepend to list so most recent log on top
    }

    // helper function to map each event to a color
    private fun getEventColor(event: Lifecycle.Event): Color {
        return when (event) {
            Lifecycle.Event.ON_CREATE -> Color(0xFF4CAF50) // Green
            Lifecycle.Event.ON_START -> Color(0xFF2196F3) // Blue
            Lifecycle.Event.ON_RESUME -> Color(0xFF00BCD4) // Cyan
            Lifecycle.Event.ON_PAUSE -> Color(0xFFFF9800) // Orange
            Lifecycle.Event.ON_STOP -> Color(0xFFF44336) // Red
            Lifecycle.Event.ON_DESTROY -> Color(0xFF616161) // Grey
            Lifecycle.Event.ON_ANY -> Color(0xFF9C27B0) // Purple
        }
    }
}