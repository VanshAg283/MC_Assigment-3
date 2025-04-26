package com.example.wilog.navigation

sealed class NavRoutes(val route: String) {
    object Permission : NavRoutes("permission")
    object LocationSelection : NavRoutes("location_selection")
    object Scanning : NavRoutes("scanning")
    object Results : NavRoutes("results")
}
