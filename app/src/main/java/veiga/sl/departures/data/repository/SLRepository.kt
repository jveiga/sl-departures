package veiga.sl.departures.data.repository

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
    private val preferences: PreferencesManager
) {
    fun getFavorites(): Flow<List<Stop>> {
        return dao.getAllFavorites().map { entities ->
            entities.map { Stop(it.id, it.name, isFavorite = true) }
        }
    }

    suspend fun getNearbyStops(lat: Double, lon: Double): List<Stop> {
        val apiKey = preferences.getApiKey() ?: ""
        val response = api.getNearbyStops(apiKey, lat, lon)
        return response.LocationList?.StopLocation?.map {
            Stop(it.siteid, it.name, it.dist)
        } ?: emptyList()
    }

    suspend fun getDepartures(siteId: String): List<Departure> {
        val apiKey = preferences.getApiKey() ?: ""
        val response = api.getDepartures(apiKey, siteId)
        return response.ResponseData?.Metros?.map {
            Departure(it.LineNumber, it.Destination, it.DisplayTime, "METRO", it.GroupOfLine)
        } ?: emptyList()
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
}
