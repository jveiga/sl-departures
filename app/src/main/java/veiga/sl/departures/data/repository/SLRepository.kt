package veiga.sl.departures.data.repository

import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import veiga.sl.departures.data.api.SLService
import veiga.sl.departures.data.db.FavoriteStopDao
import veiga.sl.departures.data.db.FavoriteStopEntity
import veiga.sl.departures.data.local.PreferencesManager
import veiga.sl.departures.domain.model.Departure
import veiga.sl.departures.domain.model.Stop

class SLRepository(
    private val api: SLService,
    private val dao: FavoriteStopDao,
    private val preferences: PreferencesManager,
) {
    private val resrobotApiKey = veiga.sl.departures.BuildConfig.RESROBOT_API_KEY
    private val departuresApiKey = veiga.sl.departures.BuildConfig.DEPARTURES_API_KEY

    fun getFavorites(): Flow<List<Stop>> =
        dao.getAllFavorites().map { entities ->
            entities.map { Stop(it.id, it.name, isFavorite = true) }
        }

    suspend fun getNearbyStops(
        lat: Double,
        lon: Double,
    ): List<Stop> {
        // Try Resrobot to get all nearby stops
        try {
            val url = "https://api.resrobot.se/v2.1/location.nearbystops"
            // Use radius=2000 and no specific product filter to get both T-bana and Bus
            val response = api.getResrobotNearbyStops(url, resrobotApiKey, lat, lon, products = "")

            val stops = response.stopLocationOrCoordLocation?.mapNotNull { it.StopLocation }
            if (!stops.isNullOrEmpty()) {
                return stops.map { Stop(it.extId, it.name, it.dist) }.distinctBy { it.name }
            }
        } catch (e: Exception) {
            // Silently fail
        }

        // Fallback to SL nearbystops
        val slApiKey = preferences.getApiKey() ?: ""
        val endpoints =
            listOf(
                "https://api.trafiklab.se/sl/nearbystops/v2/nearbystops.json",
                "https://api.trafiklab.se/sl/nearbystops/nearbystops.json",
            )

        for (url in endpoints) {
            try {
                val response = api.getNearbyStops(url, slApiKey, lat, lon)
                if (response.LocationList?.StopLocation != null) {
                    return response.LocationList.StopLocation.map {
                        Stop(it.siteid, it.name, it.dist)
                    }
                }
            } catch (e: Exception) {
                // Silently fail
            }
        }

        throw Exception("Could not fetch nearby stops from any source")
    }

    suspend fun getDepartures(
        siteId: String,
        stopName: String? = null,
    ): List<Departure> {
        // Ensure siteId is 9 digits for Rikshållplats format if it's not already
        // Some siteIds might already be 9 digits (from Resrobot), e.g. 740021684
        val rikshållplatsId =
            if (siteId.length < 9) {
                "74000$siteId"
            } else if (siteId.startsWith("74")) {
                siteId
            } else {
                siteId // fallback
            }
        val url = "https://realtime-api.trafiklab.se/v1/departures/$rikshållplatsId"

        try {
            val response = api.getTimetableDepartures(url, departuresApiKey)
            if (response.departures != null) {
                return response.departures.map {
                    val timeString = it.realtime ?: it.scheduled ?: ""
                    Departure(
                        line = it.route?.designation ?: "??",
                        destination = it.route?.direction ?: "Unknown",
                        displayTime = formatTime(timeString),
                        remainingTime = calculateRemainingTime(timeString),
                        transportMode = it.route?.transport_mode ?: "METRO",
                        groupOfLine = null,
                        stopName = stopName,
                    )
                }
            }
        } catch (e: Exception) {
            throw e
        }

        return emptyList()
    }

    private fun calculateRemainingTime(isoTime: String): String? {
        return try {
            if (isoTime.isBlank()) return null
            val departure = OffsetDateTime.parse(isoTime).toInstant()
            val now = Instant.now()
            val diffMinutes = Duration.between(now, departure).toMinutes()

            when {
                diffMinutes <= 0 -> "Now"
                diffMinutes < 60 -> "${diffMinutes} min"
                else -> null // Too far in the future to show as "remaining"
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun formatTime(isoTime: String): String {
        // Simple formatter to extract HH:mm from ISO string 2024-05-20T10:30:00
        return try {
            if (isoTime.contains("T")) {
                isoTime.substringAfter("T").substring(0, 5)
            } else {
                isoTime
            }
        } catch (e: Exception) {
            isoTime
        }
    }

    suspend fun toggleFavorite(stop: Stop) {
        if (stop.isFavorite) {
            dao.deleteFavorite(stop.id)
        } else {
            dao.insertFavorite(FavoriteStopEntity(stop.id, stop.name))
        }
    }

    fun saveApiKey(key: String) {
        preferences.saveApiKey(key)
    }

    fun getApiKey(): String? = preferences.getApiKey()

    suspend fun clearAll() {
        dao.deleteAll()
        preferences.clear()
    }
}
