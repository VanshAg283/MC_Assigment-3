package com.example.wilog

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.wilog.navigation.NavRoutes
import com.example.wilog.ui.screens.LocationSelectionScreen
import com.example.wilog.ui.screens.PermissionScreen
import com.example.wilog.ui.screens.ResultsScreen
import com.example.wilog.ui.screens.ScanningScreen
import com.example.wilog.ui.theme.WiLogTheme

class MainActivity : ComponentActivity() {
    private val requiredPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.CHANGE_WIFI_STATE
    )

    private var permissionsGranted = mutableStateOf(false)

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        permissionsGranted.value = allGranted
    }

    private val viewModel: WifiViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkPermissions()

        enableEdgeToEdge()
        setContent {
            WiLogTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Pass the request permission function to the UI
                    WiLogApp(
                        permissionsGranted = permissionsGranted.value,
                        viewModel = viewModel,
                        onRequestPermission = {
                            // Launch permission request when button is clicked
                            permissionLauncher.launch(requiredPermissions)
                        }
                    )
                }
            }
        }
    }

    private fun checkPermissions() {
        permissionsGranted.value = requiredPermissions.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
}

@Composable
fun WiLogApp(
    permissionsGranted: Boolean,
    viewModel: WifiViewModel,
    onRequestPermission: () -> Unit // Add this parameter
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = if (permissionsGranted) NavRoutes.LocationSelection.route else NavRoutes.Permission.route
    ) {
        composable(NavRoutes.Permission.route) {
            PermissionScreen(
                onRequestPermission = onRequestPermission // Pass the function to the UI
            )
        }

        composable(NavRoutes.LocationSelection.route) {
            LocationSelectionScreen(
                viewModel = viewModel,
                onScanClick = { location ->
                    navController.navigate("${NavRoutes.Scanning.route}/$location")
                },
                onResultClick = { location ->
                    navController.navigate("${NavRoutes.Results.route}/$location")
                }
            )
        }

        composable(
            route = "${NavRoutes.Scanning.route}/{location}",
            arguments = listOf(navArgument("location") { type = NavType.StringType })
        ) { backStackEntry ->
            val location = backStackEntry.arguments?.getString("location") ?: ""
            ScanningScreen(
                viewModel = viewModel,
                location = location,
                onScanComplete = {
                    navController.navigate("${NavRoutes.Results.route}/$location") {
                        popUpTo(NavRoutes.LocationSelection.route)
                    }
                }
            )
        }

        composable(
            route = "${NavRoutes.Results.route}/{location}",
            arguments = listOf(navArgument("location") { type = NavType.StringType })
        ) { backStackEntry ->
            val location = backStackEntry.arguments?.getString("location") ?: ""
            ResultsScreen(
                viewModel = viewModel,
                location = location,
                onBackClick = {
                    navController.navigateUp()
                }
            )
        }
    }
}