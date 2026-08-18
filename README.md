# Smart Home Monitoring & Control System

This project consists of an Android mobile application and a web-based Hardware Simulator, synchronized in real-time using Google Firebase Cloud Firestore.

## Project Structure
*   `Android_App/`: Source code for the native Android application (Jetpack Compose, MVVM).
*   `Web_Simulator/`: Source code for the React-based hardware simulator.
*   `Technical_Documentation.md`: Comprehensive report on system architecture and operations.
*   `APK/`: Directory containing the final Android build.

## Setup Instructions

### 1. Android Application
1.  Open the `Android_App/` folder in Android Studio.
2.  Ensure `google-services.json` is present in the `app/` directory (already included).
3.  Build and run the app on an emulator or physical device.

### 2. Web Hardware Simulator
1.  Navigate to the `Web_Simulator/` folder.
2.  Install dependencies: `npm install`.
3.  Ensure `.env.local` is configured with Firebase API keys (already included).
4.  Run the simulator: `npm run dev`.

### 3. Firestore Configuration
*   The system uses `devices`, `floors`, and `telemetry` collections.
*   Ensure Firestore security rules allow the necessary read/write access for testing.

## Technical Highlights
*   **Real-time Synchronization**: Bidirectional sync between Mobile and Web via Firestore listeners.
*   **Safety Cutoffs**: Automated monitoring of hazardous devices with session time limits.
*   **Dynamic Layouts**: Grid-based floor plan representation with scalable metadata.
