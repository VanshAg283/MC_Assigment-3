package com.example.wilog.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.wilog.WifiViewModel
import com.example.wilog.model.LocationData
import com.example.wilog.storage.LocationStorage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSelectionScreen(
    viewModel: WifiViewModel,
    onScanClick: (String) -> Unit,
    onResultClick: (String) -> Unit
) {
    val context = LocalContext.current
    var locations by remember { mutableStateOf(LocationStorage.getAll(context)) }
    var showAddDialog by remember { mutableStateOf(false) }
    var newLocation by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "WiFi Location Analyzer",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = "Add") },
                text = { Text("Add Location") },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (locations.isEmpty()) {
                EmptyLocationsList()
            } else {
                LocationsList(
                    locations = locations,
                    onScanClick = onScanClick,
                    onResultClick = onResultClick,
                    onDeleteClick = { locationName ->
                        LocationStorage.delete(context, locationName)
                        locations = LocationStorage.getAll(context)
                        scope.launch {
                            snackbarHostState.showSnackbar("Location deleted")
                        }
                    }
                )
            }
        }

        if (showAddDialog) {
            AddLocationDialog(
                newLocation = newLocation,
                onNewLocationChange = { newLocation = it },
                onDismiss = {
                    showAddDialog = false
                    newLocation = ""
                },
                onConfirm = {
                    if (newLocation.isNotBlank()) {
                        LocationStorage.saveEmptyLocation(context, newLocation)
                        locations = LocationStorage.getAll(context)
                        showAddDialog = false
                        onScanClick(newLocation)
                        newLocation = ""
                    }
                }
            )
        }
    }
}

@Composable
fun EmptyLocationsList() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.LocationOff,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "No locations yet",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Add a new location to start scanning WiFi networks",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun LocationsList(
    locations: Map<String, Any?>,
    onScanClick: (String) -> Unit,
    onResultClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit
) {
    // Track which items have been animated already in a persistent map
    val animatedItems = remember { mutableStateMapOf<String, Boolean>() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(locations.keys.toList()) { index, locationName ->
            val locationData = locations[locationName]

            // Check if this item should be animated (only once)
            val shouldAnimate = !animatedItems.containsKey(locationName)

            // Trigger a one-time animation with a stable key
            var visible by remember(locationName) { mutableStateOf(!shouldAnimate) }

            LaunchedEffect(locationName) {
                if (shouldAnimate) {
                    delay(index * 50L) // Reduced delay for smoother experience
                    visible = true
                    // Mark as animated
                    animatedItems[locationName] = true
                }
            }

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 4 },
                exit = fadeOut(tween(300))
            ) {
                LocationCard(
                    locationName = locationName,
                    locationData = locationData,
                    onScanClick = { onScanClick(locationName) },
                    onResultClick = { onResultClick(locationName) },
                    onDeleteClick = { onDeleteClick(locationName) }
                )
            }
        }
        // Add some padding at the bottom so the FAB doesn't cover content
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationCard(
    locationName: String,
    locationData: Any?,
    onScanClick: () -> Unit,
    onResultClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Top row with location name and delete button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Location name on the left
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = locationName,
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                // Delete button on the right
                IconButton(
                    onClick = onDeleteClick,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete"
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (locationData != null) {
                // For scanned locations, show RSSI range bar instead of badge
                val locationInfo = locationData as? com.example.wilog.model.LocationData
                if (locationInfo != null && locationInfo.rssiValues.isNotEmpty()) {
                    RssiRangeBar(rssiValues = locationInfo.rssiValues)
                }
            } else {
                StatusBadge(
                    icon = Icons.Default.Pending,
                    label = "Not scanned yet",
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom row with scan and results buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Scan/Rescan button
                FilledTonalButton(
                    onClick = onScanClick,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.NetworkWifi,
                        contentDescription = "Scan"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (locationData == null) "Scan" else "Rescan")
                }

                // Results button (only shown if location has been scanned)
                if (locationData != null) {
                    Button(
                        onClick = onResultClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Poll,
                            contentDescription = "Results"
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Results")
                    }
                }
            }
        }
    }
}

@Composable
fun RssiRangeBar(rssiValues: List<Int>) {
    // Filter out empty slots (-100 values)
    val validValues = rssiValues.filter { it > -100 }

    if (validValues.isEmpty()) {
        Text(
            text = "No valid RSSI data",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )
        return
    }

    // Calculate min, max, and average RSSI values
    val minRssi = validValues.minOrNull() ?: -100
    val maxRssi = validValues.maxOrNull() ?: -100
    val avgRssi = validValues.average().toInt()

    Column(modifier = Modifier.fillMaxWidth()) {
        // RSSI range info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "RSSI Range: $minRssi to $maxRssi dBm",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Avg: $avgRssi dBm",
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // RSSI Range Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.LightGray.copy(alpha = 0.3f))
        ) {
            // Normalized positions for min, max, and avg
            val normalizedMin = normalizeRssi(minRssi)
            val normalizedMax = normalizeRssi(maxRssi)
            val normalizedAvg = normalizeRssi(avgRssi)

            // Range bar (from min to max)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(normalizedMax)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                            colors = listOf(
                                getRssiColor(minRssi),
                                getRssiColor(maxRssi)
                            ),
                            startX = 0f,
                            endX = Float.POSITIVE_INFINITY
                        )
                    )
            )

            // Average indicator
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .offset(x = (normalizedAvg * LocalDensity.current.density).dp - 2.dp)
                    .background(Color.White)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Signal strength indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Stronger",
                style = MaterialTheme.typography.labelSmall,
                color = Color(76, 175, 80)
            )
            Text(
                text = "Weaker",
                style = MaterialTheme.typography.labelSmall,
                color = Color(244, 67, 54)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Networks count
        val locationInfo = rssiValues as? com.example.wilog.model.LocationData
        if (locationInfo != null) {
            Text(
                text = "${validValues.size} RSSI samples, ${locationInfo.networks.size} networks",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Helper function to normalize RSSI values to 0-1 range
// Typical RSSI values range from -30 (strong) to -100 (weak)
private fun normalizeRssi(rssi: Int): Float {
    // Invert the range so that stronger signals (higher values) appear to the left
    return (1 - (rssi.coerceIn(-100, -30) + 100) / 70f).coerceIn(0f, 1f)
}

// Get color based on RSSI strength
private fun getRssiColor(rssi: Int): Color {
    return when {
        rssi > -50 -> Color(76, 175, 80) // Good - Green
        rssi > -70 -> Color(255, 193, 7) // Medium - Yellow/Amber
        else -> Color(244, 67, 54) // Poor - Red
    }
}

@Composable
fun StatusBadge(
    icon: ImageVector,
    label: String,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color
) {
    Surface(
        modifier = Modifier.padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        contentColor = contentColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
fun AddLocationDialog(
    newLocation: String,
    onNewLocationChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Location") },
        text = {
            Column {
                Text(
                    "Enter a name for this location to start scanning WiFi networks.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = newLocation,
                    onValueChange = onNewLocationChange,
                    label = { Text("Location Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null
                        )
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = newLocation.isNotBlank()
            ) {
                Text("Add and Scan")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}
