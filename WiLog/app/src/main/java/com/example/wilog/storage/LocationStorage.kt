package com.example.wilog.storage

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.wilog.model.LocationData
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

object LocationStorage {
    private const val PREF_NAME = "wifi_storage"
    private const val KEY_LOCATIONS = "locations"
    private const val TAG = "LocationStorage"

    // Lazily initialize Gson once for better performance
    private val gson: Gson by lazy {
        GsonBuilder()
            .setPrettyPrinting()  // Makes JSON readable for debugging
            .create()
    }

    // Cache the type token for better performance
    private val locationMapType = object : TypeToken<Map<String, LocationData?>>() {}.type

    fun save(context: Context, data: LocationData) {
        try {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val list = getAll(context).toMutableMap()
            list[data.name] = data

            val json = gson.toJson(list, locationMapType)
            prefs.edit().putString(KEY_LOCATIONS, json).apply()

            Log.d(TAG, "Location saved: ${data.name}, Networks: ${data.networks.size}, RSSI samples: ${data.rssiValues.size}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save location data: ${e.message}", e)
        }
    }

    fun getAll(context: Context): Map<String, LocationData?> {
        try {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val json = prefs.getString(KEY_LOCATIONS, null) ?: return emptyMap()
            val result = gson.fromJson<Map<String, LocationData?>>(json, locationMapType)
            Log.d(TAG, "Loaded ${result.size} locations")
            return result
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load locations: ${e.message}", e)
            return emptyMap()
        }
    }

    fun saveEmptyLocation(context: Context, locationName: String) {
        try {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val list = getAll(context).toMutableMap()
            list[locationName] = null

            val json = gson.toJson(list, locationMapType)
            prefs.edit().putString(KEY_LOCATIONS, json).apply()

            Log.d(TAG, "Empty location saved: $locationName")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save empty location: ${e.message}", e)
        }
    }

    fun get(context: Context, location: String): LocationData? {
        try {
            val result = getAll(context)[location]
            Log.d(TAG, "Retrieved location: $location, exists: ${result != null}")
            return result
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get location $location: ${e.message}", e)
            return null
        }
    }

    fun delete(context: Context, location: String) {
        try {
            val list = getAll(context).toMutableMap()
            val removed = list.remove(location)

            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val json = gson.toJson(list, locationMapType)
            prefs.edit().putString(KEY_LOCATIONS, json).apply()

            Log.d(TAG, "Deleted location: $location, existed: ${removed != null}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete location $location: ${e.message}", e)
        }
    }

    /**
     * Exports all location data as a formatted JSON string
     */
    fun exportData(context: Context): String {
        return try {
            val data = getAll(context)
            gson.toJson(data)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to export data: ${e.message}", e)
            "{\"error\": \"Failed to export data\"}"
        }
    }

    /**
     * Clears all stored location data
     */
    fun clearAll(context: Context) {
        try {
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove(KEY_LOCATIONS)
                .apply()
            Log.d(TAG, "All location data cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear all data: ${e.message}", e)
        }
    }
}