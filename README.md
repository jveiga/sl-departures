# SL Departures (Wear OS)

A modern Wear OS application for tracking real-time public transport departures in Stockholm (SL), built using Jetpack Compose and Material 3.

> [!IMPORTANT]  
> **Note:** This entire project—from architecture and networking to UI and documentation—was generated and refined using **AI** (Google's Android Studio AI assistant).

## Features

- **Nearby Stops**: Automatically finds the closest SL stops based on your current GPS location.
- **Real-time Departures**: View live departure times for buses, trains, metros, and trams.
- **Favorites**: Save your most-used stops for quick access at the top of the list.
- **Secure API Key Management**: 
  - **First-launch prompt** for your Trafiklab API key.
  - **Encrypted storage** using `EncryptedSharedPreferences`.
  - **Settings screen** accessible via a long-press on the main screen title.
- **Robust Offline Support**: 
  - Advanced caching (Stale-While-Revalidate) using OkHttp.
  - View last-known data even when the API is unreachable or your connection is lost.
- **Modern Wear OS UI**: Built with Wear Compose Material 3, optimized for round displays and smooth scrolling.

## Tech Stack

- **UI**: Jetpack Compose for Wear OS (Material 3)
- **Architecture**: MVVM with StateFlow and Repository pattern
- **Networking**: Retrofit + OkHttp + Kotlinx Serialization
- **Database**: Room (for favorites)
- **Security**: Android Security Crypto (EncryptedSharedPreferences)
- **Location**: Google Play Services (Fused Location Provider)
- **Concurrency**: Kotlin Coroutines & Flow

## Setup

1. Clone the repository.
2. Build the project using Android Studio.
3. Get an API Key (Nearby Stops & Realtime Departures) from [Trafiklab](https://www.trafiklab.se/).
4. On the first launch of the app, enter your API key when prompted.
5. If you need to change your key later, **long-press** the "Nearby Stops" header.

## Screenshots

*(Coming soon - The AI hasn't learned to take artistic photos of its own work yet!)*

---
*Developed with ❤️ and AI.*
