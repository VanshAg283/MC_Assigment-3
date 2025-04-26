# WiLog: WiFi Location Analyzer

WiLog is an Android app built with Jetpack Compose that logs the received signal strength (RSSI) of WiFi Access Points (APs) at different user-defined locations. The app visualizes and compares WiFi signal ranges across locations, helping users understand how WiFi coverage varies in their environment.


## Features

- **Add Locations:** Users can add custom-named locations (e.g., "Living Room", "Office", "Cafeteria").
- **Scan WiFi Networks:** For each location, the app scans available WiFi networks and logs 100 RSSI samples.
- **Visualize Signal Strength:** Each location displays a range bar showing minimum, maximum, and average RSSI values.
- **Compare Locations:** Users can view and compare WiFi signal strength across at least three different locations.
- **Persistent Storage:** All scanned data is saved locally for future reference.
- **Animated UI:** Smooth animations for list items and interactive cards.
- **User-Friendly Interface:** Material 3 design, clear feedback, and easy navigation.


## Implementation Overview

### 1. **Architecture**

- **Jetpack Compose:** All UI is built using Compose for a modern, reactive experience.
- **MVVM Pattern:** The app uses a `WifiViewModel` to manage state and business logic.
- **Local Storage:** Locations and their scan data are stored using a custom `LocationStorage` utility.

### 2. **Key Components**

#### **LocationSelectionScreen**

- **Purpose:** Main screen listing all saved locations.
- **Features:**
  - Floating Action Button (FAB) to add new locations.
  - Animated list of location cards.
  - Snackbar feedback for actions (e.g., deletion).
  - Handles empty state with a friendly message.

#### **LocationsList**

- **Purpose:** Displays all locations in a scrollable, animated list.
- **Features:**
  - Each item animates into view for a smooth user experience.
  - Each card shows location name, scan status, and action buttons.

#### **LocationCard**

- **Purpose:** Represents a single location.
- **Features:**
  - Shows location name and delete button.
  - Displays a status badge if not scanned, or a signal strength bar if scanned.
  - "Scan/Rescan" button to initiate WiFi scanning.
  - "Results" button to view detailed scan results.

#### **RssiRangeBar**

- **Purpose:** Visualizes the RSSI range for a location.
- **Features:**
  - Shows min, max, and average RSSI values.
  - Color-coded bar (green/yellow/red) based on signal strength.
  - Indicates number of samples and networks detected.

#### **AddLocationDialog**

- **Purpose:** Dialog for adding a new location.
- **Features:**
  - Input field for location name.
  - "Add and Scan" button to immediately start scanning after adding.


## Data Flow

1. **Adding a Location:**
   - User taps FAB → `AddLocationDialog` appears.
   - User enters a name and confirms → Location is saved and scan starts.

2. **Scanning WiFi:**
   - On scan, the app collects up to 100 RSSI values for all visible WiFi networks.
   - Data is stored as a `LocationData` object (location name, RSSI values, network info).

3. **Displaying Data:**
   - Each location card shows a range bar if data exists, or a badge if not scanned.
   - Users can rescan or view detailed results for each location.

4. **Deleting a Location:**
   - User taps delete icon → Location and its data are removed from storage.


## Code Highlights

- **Animations:** Uses `AnimatedVisibility` and Compose transitions for smooth UI.
- **RSSI Normalization:** RSSI values are normalized to a 0-1 range for visualization.
- **Color Coding:** Signal strength is color-coded (green for strong, yellow for medium, red for weak).
- **Persistence:** All data is stored locally using a custom storage class.


## How to Use

1. **Add at least three locations** using the FAB.
2. **Scan WiFi** at each location (move to the physical location before scanning).
3. **View and compare** the RSSI range bars for each location.
4. **Tap "Results"** for detailed network information at each location.


## Assignment Requirements Checklist

- **App interface:** Modern, animated, and user-friendly UI.
- **Data logging:** Logs 100 RSSI samples per location, with persistent storage.
- **Three locations:** Supports any number of locations; easily demonstrates three or more.
- **Visualization:** Clearly shows how RSSI ranges differ across locations.


## Dependencies

- Jetpack Compose
- Material 3
- Kotlin Coroutines


## Notes

- The app requires location and WiFi permissions to scan networks.
- RSSI values are device-dependent and may vary with hardware and environment.