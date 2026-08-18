# Technical Documentation: Smart Home Monitoring & Control System

## 1. Executive Summary
This document provides a comprehensive technical breakdown of the Smart Home Monitoring and Control System. The ecosystem consists of a native Android mobile client and a React-based Hardware Simulator, both synchronized in real-time through a centralized Cloud Firestore backend. This architecture fulfills the requirements for a scalable, bi-directional IoT monitoring solution.

## 2. Mobile Application Architecture (Android)
The Android application is built using the **MVVM (Model-View-ViewModel)** architectural pattern, ensuring a clean separation of concerns and reactive data flow through modern Jetpack Compose.

### 2.1 Data Layer (Model & Repository)
*   **Source Files**: `Device.kt`, `FloorPlan.kt`, `SmartHomeRepository.kt`
*   **Implementation**:
    *   `Device.kt` & `FloorPlan.kt`: Define the core data models. `Device.kt` utilizes the `@PropertyName("on")` annotation to bridge the naming discrepancy between the Kotlin `isOn` boolean and the Firestore `on` field, ensuring cross-platform data integrity.
    *   `SmartHomeRepository.kt`: Acts as the single source of truth. It handles all Firebase Firestore interactions, including real-time `Flow` emissions for devices and telemetry via the `snapshots()` listener, and `suspend` functions for asynchronous state updates.

### 2.2 Domain Layer (ViewModel)
*   **Source Files**: `DashboardViewModel.kt`, `AnalyticsViewModel.kt`
*   **Implementation**:
    *   `DashboardViewModel.kt`: Manages the UI state for the main dashboard. It transforms repository flows into `StateFlow` for the UI, handles floor selection logic, and coordinates device creation/toggling using Kotlin Coroutines.
    *   `AnalyticsViewModel.kt`: Processes telemetry data for the analytics screen, providing insights into system activity and safety events.

### 2.3 UI Layer (Jetpack Compose Views)
*   **Source Files**: `MainActivity.kt`, `GridOverlayView.kt`, `MultiSwitchCard.kt`, `ScheduledDeviceCard.kt`
*   **Implementation**:
    *   `MainActivity.kt`: The entry point that sets the global `MaterialTheme` and manages top-level navigation between Dashboard and Analytics.
    *   `GridOverlayView.kt`: A specialized component that renders the floor plan grid and interactive device markers using the `Canvas` API. It dynamically calculates positions based on floor dimensions fetched from Firestore.
    *   `OutletCard.kt`, `MultiSwitchCard.kt`, `ScheduledDeviceCard.kt`: Specialized UI components that encapsulate the control logic and visual representation for each device type.

## 3. Web Hardware Simulator Architecture
The simulator mimics physical hardware behavior and serves as the primary testing tool for the mobile app, represented as a React-based web dashboard.

### 3.1 Framework & Modular Structure
*   **Source Files**: `App.tsx`, `firebase.ts`, `useDevices.ts`, `useFloors.ts`
*   **Implementation**:
    *   `App.tsx`: The main controller handling global state, Firestore write logic, and bidirectional event orchestration.
    *   `firebase.ts`: The central configuration point for the Firebase Web SDK v10+.
    *   `useDevices.ts` & `useFloors.ts`: Reusable React hooks that encapsulate Firestore `onSnapshot()` listeners for real-time reactivity.

### 3.2 Visualization & Interaction
*   **Source Files**: `DeviceMap.tsx`, `DeviceControls.tsx`, `TelemetryPanel.tsx`
*   **Implementation**:
    *   `DeviceMap.tsx`: Implements a percentage-based coordinate system (`left: x/cols`, `top: y/rows`) that mirrors the Android app's grid, ensuring markers appear in the same relative positions across all screen sizes.
    *   `DeviceControls.tsx`: Renders dynamic control interfaces for hardware types like Outlets and Multi-switches.
    *   `TelemetryPanel.tsx`: A real-time activity log that filters events by type and floor, providing a "live" view of system health.

## 4. Synchronization & Integration Logic
The system employs a bidirectional synchronization mechanism centered around **Google Firebase Firestore**.

### 4.1 Real-time Connectivity
*   `SmartHomeRepository.kt` (Android) and `firebase.ts` (Web) both point to a shared Firestore instance.
*   The system uses **`onSnapshot()` (Web)** and **`snapshots()` (Android)** to listen for remote changes, ensuring that a toggle on one platform is reflected on the other in under 200ms.

### 4.2 Property Naming & Consistency
*   A critical architectural bridge was implemented in **`App.tsx` (Web)** and **`SmartHomeRepository.kt` (Android)** to handle the `isOn` vs `on` naming discrepancy. The web simulator now explicitly writes both fields to ensure the Android `@PropertyName("on")` mapping remains functional.

### 4.3 Telemetry ID Strategy
*   **Format**: `[DEVICE_ID]_[EVENT_TYPE]_[TIMESTAMP]` (e.g., `bedroom-light_MANUAL_TOGGLE_17869845`).
*   Both **`App.tsx`** and **`SmartHomeRepository.kt`** use this structured slug format instead of random UUIDs, making the `telemetry` collection easily searchable and chronologically sortable in the Firebase Console.

## 5. Safety & Operational Logic
*   **Source Files**: `ScheduledDeviceCard.kt` (Android), `DeviceControls.tsx` (Web)
*   **Logic**: Hazardous devices utilize a `maxOnDurationMins` threshold. Both platforms monitor the `turnedOnAt` timestamp against this limit to trigger safety warnings.
*   **Implementation**: Real-time "Timeout Warning" badges are displayed in both the mobile app and web simulator when usage limits are exceeded, simulating a physical thermal or time-based safety cutoff.

---
Mobile Application Development  
**Architecture:** MVVM (Android) + Modular Hooks (React) + Cloud Firestore (Backend)
