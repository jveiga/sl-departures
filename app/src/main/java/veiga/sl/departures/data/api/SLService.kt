package veiga.sl.departures.data.api

import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

interface SLService {
    @GET("nearbystops.json")
    suspend fun getNearbyStops(
        @Query("key") apiKey: String,
        @Query("originCoordLat") lat: Double,
        @Query("originCoordLong") long: Double,
        @Query("maxresults") maxResults: Int = 10,
        @Query("radius") radius: Int = 1000
    ): StopResponse

    @GET("realtimedeparturesV4.json")
    suspend fun getDepartures(
        @Query("key") apiKey: String,
        @Query("siteid") siteId: String,
        @Query("timewindow") timeWindow: Int = 60
    ): DepartureResponse
}

@Serializable
data class StopResponse(
    val LocationList: LocationList? = null
)

@Serializable
data class LocationList(
    val StopLocation: List<ApiStop>? = null
)

@Serializable
data class ApiStop(
    val name: String,
    val siteid: String,
    val lat: String,
    val lon: String,
    val dist: Int? = null
)

@Serializable
data class DepartureResponse(
    val ResponseData: ResponseData? = null
)

@Serializable
data class ResponseData(
    val Metros: List<ApiMetro>? = null
)

@Serializable
data class ApiMetro(
    val LineNumber: String,
    val Destination: String,
    val DisplayTime: String,
    val GroupOfLine: String? = null
)
