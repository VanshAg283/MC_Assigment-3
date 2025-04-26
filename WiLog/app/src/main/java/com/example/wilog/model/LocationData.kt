package com.example.wilog.model

data class LocationData(
    val name: String,
    val rssiValues: List<Int>, // General RSSI values
    val networks: List<WifiNetwork>
)

data class WifiNetwork(
    val ssid: String,
    val bssid: String,
    val rssi: Int // Adding signal strength to each network
)