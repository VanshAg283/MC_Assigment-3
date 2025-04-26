package com.example.wilog

import android.annotation.SuppressLint
import android.app.Application
import android.content.*
import android.net.wifi.WifiManager
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import com.example.wilog.model.*
import com.example.wilog.storage.LocationStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WifiViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager

    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState: StateFlow<ScanState> = _scanState.asStateFlow()

    // Default RSSI value for empty slots (-100 dBm indicates very poor/no signal)
    private val DEFAULT_RSSI = -100

    var onScanResult: ((LocationData) -> Unit)? = null

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
                val success = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                } else {
                    true
                }

                if (success) {
                    val results = wifiManager.scanResults
                    val rssi = results.map { it.level }.take(100)

                    // Include RSSI values with each network
                    val nets = results.map {
                        WifiNetwork(it.SSID, it.BSSID, it.level)
                    }

                    // Fill with default values if needed
                    val rssiValues = rssi.toMutableList()
                    while (rssiValues.size < 100) {
                        rssiValues.add(DEFAULT_RSSI)
                    }

                    val data = LocationData(currentLoc ?: "", rssiValues.take(100), nets)
                    LocationStorage.save(context!!, data)
                    _scanState.value = ScanState.Success(data)
                    onScanResult?.invoke(data)
                } else {
                    _scanState.value = ScanState.Failed("Scan was not successful")
                }
            }
        }
    }

    private var currentLoc: String? = null

    @SuppressLint("MissingPermission")
    fun startScan(location: String, onComplete: (LocationData) -> Unit) {
        currentLoc = location
        onScanResult = onComplete
        _scanState.value = ScanState.Scanning

        val filter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }

        val success = wifiManager.startScan()
        if (!success) {
            // Fallback if scan fails - still create 100 data points
            val results = wifiManager.scanResults
            val rssiValues = results.map { it.level }.toMutableList()

            // Fill with default values if we have fewer than 100 points
            while (rssiValues.size < 100) {
                rssiValues.add(DEFAULT_RSSI)
            }

            // Include RSSI values with each network
            val nets = results.map {
                WifiNetwork(it.SSID, it.BSSID, it.level)
            }

            val data = LocationData(location, rssiValues.take(100), nets)
            LocationStorage.save(context, data)
            _scanState.value = ScanState.Success(data)
            onScanResult?.invoke(data)
        }
    }

    override fun onCleared() {
        try {
            context.unregisterReceiver(receiver)
        } catch (e: Exception) {
            // Receiver might not be registered
        }
        super.onCleared()
    }

    sealed class ScanState {
        object Idle : ScanState()
        object Scanning : ScanState()
        data class Success(val data: LocationData) : ScanState()
        data class Failed(val error: String) : ScanState()
    }
}