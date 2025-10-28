package com.example.assing4_1

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.assing4_1.ui.theme.Assing4_1Theme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    val TAG = "ActivityState"
    private val viewModel: TrackerModelView by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "[Activity] ==> onCreate: The Activity is being created.")
        enableEdgeToEdge()
        setContent {
            Assing4_1Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(), // Make the surface fill the entire screen.
                    color = MaterialTheme.colorScheme.background
                ) {
                    LifeTrackerDemoScreen(viewModel)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "[Activity] ==> onStart: The Activity is becoming visible.")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "[Activity] ==> onResume: The Activity is interactive.")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "[Activity] ==> onPause: The Activity is going into the background.")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "[Activity] ==> onStop: The Activity is no longer visible.")
    }

    override fun onDestroy() {
        Log.d(TAG, "[Activity] ==> onDestroy: The Activity is being destroyed.")
        super.onDestroy()
        // Note: super.onDestroy() is called last here to ensure our log is sent before destruction.
    }
}

/**
 * A Composable function that displays instructions and observes its own lifecycle events.
 *
 * @param viewModel The viewmodel used to observe
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LifeTrackerDemoScreen(viewModel: TrackerModelView, lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current) {
    // A constant for logging from within this Composable.
    val TAG = "ActivityStateTransition"

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // `DisposableEffect` is a side-effect Composable used for managing resources
    // that need to be cleaned up when the composable leaves the screen (is "disposed").
    // It's perfect for adding and removing observers.
    // The `key1 = lifecycleOwner` means this effect will re-run if the lifecycleOwner changes.
    DisposableEffect(lifecycleOwner) {
        // Create an observer that logs lifecycle events.
        val observer = LifecycleEventObserver { _, event ->
            // We can log the event that the Composable's observer receives.
            // This shows how a Composable can react to the Activity's state.
            Log.d(TAG, "[Composable] Observed Event: ${event.name}")
            viewModel.addLog(event)

            // trigger snackbar if enabled
            scope.launch {
                if (viewModel.showSnackbarOnTransition.value) {
                    snackbarHostState.showSnackbar(
                        message = "Event: ${event.name}",
//                        withDismissAction = false
                    )
                }
            }
        }

        // Add the observer to the lifecycle of the owner (our Activity).
        lifecycleOwner.lifecycle.addObserver(observer)

        // The `onDispose` block is crucial. It's called when the Composable
        // is removed from the composition. We must clean up our observer here
        // to prevent memory leaks.
        onDispose {
            Log.d(TAG, "[Composable] Disposing Effect. Removing observer.")
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // collect values as state so relevant UI components are rerendered
    val logEntries by viewModel.logEntries.collectAsState()
    val showSnackbars by viewModel.showSnackbarOnTransition.collectAsState()
    val currentLifecycleState = lifecycleOwner.lifecycle.currentState

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Lifecycle Events") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Display the current lifecycle state.
            Text(
                text = "Current State: ${currentLifecycleState.name}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            // Toggle for enabling/disabling snackbars.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Show Snackbar on Transition", modifier = Modifier.weight(1f))
                Switch(
                    checked = showSnackbars,
                    onCheckedChange = { viewModel.showSnackbarOnTransition.value = it }
                )
            }
            // The scrolling list of logs.
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(logEntries) { entry ->
                    LogItem(logEntry = entry)
                }
            }
        }
    }
}

@Composable
fun LogItem(logEntry: LogEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Status color code circle
        Spacer(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(logEntry.color)
        )
        // Timestamp
        Text(
            text = logEntry.timestamp,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        // Event Name
        Text(
            text = logEntry.event.name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Assing4_1Theme {
        LifeTrackerDemoScreen(viewModel = TrackerModelView())
    }
}