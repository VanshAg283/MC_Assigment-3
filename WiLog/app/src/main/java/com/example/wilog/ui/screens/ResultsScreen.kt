package com.example.wilog.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wilog.WifiViewModel
import com.example.wilog.model.LocationData
import com.example.wilog.storage.LocationStorage
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.NetworkWifi1Bar
import androidx.compose.material.icons.filled.NetworkWifi2Bar
import androidx.compose.material.icons.filled.NetworkWifi3Bar
import androidx.compose.material.icons.filled.SignalWifi4Bar
import androidx.compose.ui.draw.clip
import kotlin.math.absoluteValue
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun ResultsScreen(
    viewModel: WifiViewModel,
    location: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val locationData = remember { LocationStorage.get(context, location) }

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("RSSI Matrix", "Networks")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Results",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = location,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = { /* Share results */ }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) },
                        icon = {
                            Icon(
                                imageVector = if (index == 0)
                                    Icons.Default.GridView
                                else
                                    Icons.Default.List,
                                contentDescription = title
                            )
                        }
                    )
                }
            }

            if (locationData == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "No data available for this location",
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            } else {
                // Use a simpler transition - remove complex animations that cause lag
                AnimatedContent(
                    targetState = selectedTabIndex,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) with
                                fadeOut(animationSpec = tween(300))
                    }
                ) { targetTab ->
                    when (targetTab) {
                        0 -> RssiMatrixTab(locationData)
                        1 -> NetworksListTab(locationData)
                    }
                }
            }
        }
    }
}

@Composable
fun RssiMatrixTab(locationData: LocationData) {
    // Wrap everything in a scrollable column
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Use rememberScrollState and verticalScroll to make content scrollable
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.SignalCellular4Bar,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "RSSI Values Matrix",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Text(
                text = "Signal strength at 100 points",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp, start = 32.dp)
            )

            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.elevatedCardElevation(4.dp)
            ) {
                // Create a fixed 10x10 grid to display all 100 data points
                // Set a fixed height for the grid to ensure it doesn't try to expand infinitely
                LazyVerticalGrid(
                    columns = GridCells.Fixed(10),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .heightIn(max = 420.dp), // Set a maximum height
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(locationData.rssiValues.withIndex().toList()) { (index, rssi) ->

                        // Color based on signal strength
                        val color = when {
                            rssi > -50 -> Color(76, 175, 80) // Good - Green
                            rssi > -70 -> Color(255, 193, 7) // Medium - Yellow/Amber
                            rssi == -100 -> Color(96, 96, 96) // Empty slot - Gray
                            else -> Color(244, 67, 54) // Poor - Red
                        }

                        // Directly draw cell without animations
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(color.copy(alpha = 0.2f))
                                .border(1.dp, color, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (rssi == -100) "—" else rssi.toString(),
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                fontWeight = if (rssi > -70) FontWeight.Bold else FontWeight.Normal,
                                color = if (rssi == -100)
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Legend card
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Signal Strength Legend",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    SignalLegendItem(
                        color = Color(76, 175, 80),
                        label = "Good Signal (> -50 dBm)",
                        description = "Excellent connectivity"
                    )

                    SignalLegendItem(
                        color = Color(255, 193, 7),
                        label = "Medium Signal (-50 to -70 dBm)",
                        description = "Reliable connectivity"
                    )

                    SignalLegendItem(
                        color = Color(244, 67, 54),
                        label = "Poor Signal (< -70 dBm)",
                        description = "Unstable connectivity"
                    )

                    SignalLegendItem(
                        color = Color(96, 96, 96),
                        label = "No Signal (-100 dBm)",
                        description = "No data available"
                    )
                }
            }

            // Add extra padding at the bottom for better scrolling experience
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SignalLegendItem(
    color: Color,
    label: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color.copy(alpha = 0.2f))
                .border(1.dp, color, RoundedCornerShape(4.dp))
        )

        Column(
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun NetworksListTab(locationData: LocationData) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Detected Networks",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${locationData.networks.size}",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (locationData.networks.isEmpty()) {
                EmptyNetworksList()
            } else {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Modern header with all three columns
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Network",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1.5f)
                            )
                            Text(
                                text = "BSSID",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1.5f)
                            )
                            Text(
                                text = "Signal",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(0.8f),
                                textAlign = TextAlign.Center
                            )
                        }

                        // Network list with signal strength indicators - now using real RSSI values
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(locationData.networks) { network ->
                                NetworkListItem(
                                    ssid = network.ssid,
                                    bssid = network.bssid,
                                    rssi = network.rssi // Use the actual RSSI from the model
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NetworkListItem(
    ssid: String,
    bssid: String,
    rssi: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Network name and BSSID column
            Column(
                modifier = Modifier.weight(3f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // WiFi signal icon with appropriate color based on strength
                    Icon(
                        imageVector = when {
                            rssi > -50 -> Icons.Default.SignalWifi4Bar
                            rssi > -70 -> Icons.Default.NetworkWifi3Bar
                            rssi > -80 -> Icons.Default.NetworkWifi2Bar
                            else -> Icons.Default.NetworkWifi1Bar
                        },
                        contentDescription = null,
                        tint = getSignalColor(rssi),
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = if (ssid.isNotEmpty()) ssid else "<Hidden Network>",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = bssid,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 28.dp) // Align with text above
                )
            }

            // Signal strength indicator
            SignalStrengthIndicator(
                rssi = rssi,
                modifier = Modifier.weight(0.8f)
            )
        }

        Divider(
            modifier = Modifier.padding(top = 8.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun SignalStrengthIndicator(rssi: Int, modifier: Modifier = Modifier) {
    val signalColor = getSignalColor(rssi)

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Modern circular signal strength indicator
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(signalColor.copy(alpha = 0.15f))
                    .border(
                        width = 2.dp,
                        color = signalColor,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$rssi",
                    color = signalColor,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Signal quality text
            Text(
                text = when {
                    rssi > -50 -> "Excellent"
                    rssi > -70 -> "Good"
                    rssi > -80 -> "Fair"
                    else -> "Poor"
                },
                style = MaterialTheme.typography.labelSmall,
                color = signalColor
            )
        }
    }
}

// Helper function to get color based on signal strength
private fun getSignalColor(rssi: Int): Color {
    return when {
        rssi > -50 -> Color(76, 175, 80) // Excellent - Green
        rssi > -70 -> Color(56, 142, 60) // Good - Darker Green
        rssi > -80 -> Color(255, 193, 7) // Fair - Yellow/Amber
        else -> Color(244, 67, 54) // Poor - Red
    }
}

@Composable
fun EmptyNetworksList() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.WifiOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No networks detected",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Try scanning again in a different area",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
