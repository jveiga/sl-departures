package veiga.sl.departures.data.api

import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface SLService {
    @GET
    suspend fun getNearbyStops(
        @Url url: String,
        @Query("key") apiKey: String,
        @Query("originCoordLat") lat: Double,
        @Query("originCoordLong") long: Double,
        @Query("maxresults") maxResults: Int = 10,
        @Query("radius") radius: Int = 1000,
    ): StopResponse

    @GET
    suspend fun getResrobotNearbyStops(
        @Url url: String,
        @Query("accessId") apiKey: String,
        @Query("originCoordLat") lat: Double,
        @Query("originCoordLong") long: Double,
        @Query("format") format: String = "json",
        @Query("products") products: String = "2",
        // 2 = Metro (T-bana) in Resrobot API
    ): ResrobotStopResponse

    @GET
    suspend fun getTimetableDepartures(
        @Url url: String,
        @Query("key") apiKey: String,
    ): TimetableResponse
}

@Serializable
data class StopResponse(
    val LocationList: LocationList? = null,
    val StatusCode: Int? = null,
    val Message: String? = null,
)

@Serializable
data class LocationList(
    val StopLocation: List<ApiStop>? = null,
)

@Serializable
data class ApiStop(
    val name: String,
    val siteid: String,
    val lat: String,
    val lon: String,
    val dist: Int? = null,
)

@Serializable
data class ResrobotStopResponse(
    val stopLocationOrCoordLocation: List<ResrobotStopWrapper>? = null,
)

@Serializable
data class ResrobotStopWrapper(
    val StopLocation: ResrobotApiStop? = null,
)

@Serializable
data class ResrobotApiStop(
    val name: String,
    val extId: String,
    val lat: Double,
    val lon: Double,
    val dist: Int? = null,
)

@Serializable
data class TimetableResponse(
    val departures: List<TimetableDeparture>? = null,
)

@Serializable
data class TimetableDeparture(
    val scheduled: String? = null,
    val realtime: String? = null,
    val is_realtime: Boolean = false,
    val route: TimetableRoute? = null,
)

@Serializable
data class TimetableRoute(
    val designation: String? = null,
    val direction: String? = null,
    val transport_mode: String? = null,
)
