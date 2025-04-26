package com.example.wilog.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NetworkWifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.wilog.WifiViewModel

@Composable
fun ScanningScreen(
    viewModel: WifiViewModel,
    location: String,
    onScanComplete: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(key1 = location) {
        viewModel.startScan(location) { locationData ->
            // Scan completed, navigate to results
            onScanComplete()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LocationHeader(location)

            Spacer(modifier = Modifier.height(48.dp))

            ScanningAnimation()

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Scanning WiFi Networks",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Please wait while we collect signal information...",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun LocationHeader(location: String) {
    ElevatedCard(
        modifier = Modifier.padding(horizontal = 16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = location,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun ScanningAnimation() {
    // Animated WiFi scanning effect
    Box(
        modifier = Modifier
            .size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        // Multiple pulsating circles
        val infiniteTransition = rememberInfiniteTransition(label = "scan_animation")

        val circles = 3
        repeat(circles) { index ->
            val delay = index * 500 // Stagger the animations

            val scale by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing, delayMillis = delay),
                    repeatMode = RepeatMode.Restart
                ),
                label = "scale_$index"
            )

            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.7f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing, delayMillis = delay),
                    repeatMode = RepeatMode.Restart
                ),
                label = "alpha_$index"
            )

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha))
            )
        }

        // Center WiFi icon
        Icon(
            imageVector = Icons.Default.NetworkWifi,
            contentDescription = "Scanning",
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}
