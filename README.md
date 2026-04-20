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
2. Get your API keys from [Trafiklab](https://www.trafiklab.se/):
   - **SL Realtime Departures V1** (for `DEPARTURES_API_KEY`)
   - **Resrobot - Stolp- & platsuppslag** (for `RESROBOT_API_KEY`)
   - **SL Nearby Stops** (for `SL_NEARBY_API_KEY`)
3. Create or update `local.properties` in the root directory and add your keys:
   ```properties
   RESROBOT_API_KEY=your_resrobot_key_here
   DEPARTURES_API_KEY=your_sl_realtime_key_here
   SL_NEARBY_API_KEY=your_sl_nearby_key_here
   ```
4. Build the project using Android Studio.
5. On the first launch of the app, you can also manually enter an SL Nearby API key if prompted.
6. If you need to change your key later in the app, **long-press** the "Nearby Stations" header.

## API Documentation

This app utilizes several APIs provided by Trafiklab:

- **SL Realtime Departures V1**: Used for fetching live departure information. [Docs](https://www.trafiklab.se/api/sl-apis/sl-realtime-departures-v1/)
- **Resrobot (Location.NearbyStops)**: Primary source for finding nearby transit stations using coordinates. [Docs](https://www.trafiklab.se/api/trafikverket-apis/resrobot/locationnearbystops/)
- **SL Nearby Stops**: Fallback API for station lookups. [Docs](https://www.trafiklab.se/api/sl-apis/sl-nearby-stops/)

## Screenshots

*(Coming soon - The AI hasn't learned to take artistic photos of its own work yet!)*

---
*Developed with ❤️ and AI.*
