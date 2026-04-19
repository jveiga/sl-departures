package veiga.sl.departures.data

import android.content.Context
import androidx.room.Room
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import veiga.sl.departures.data.api.SLService
import veiga.sl.departures.data.db.AppDatabase
import veiga.sl.departures.data.local.PreferencesManager
import veiga.sl.departures.data.repository.SLRepository
import java.io.File
import java.util.concurrent.TimeUnit

object DataModule {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private fun provideDatabase(context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "sl_departures_db"
        ).build()
    }

    fun provideRepository(context: Context): SLRepository {
        val db = provideDatabase(context)
        val api = provideSLService(context)
        val preferences = PreferencesManager(context)
        return SLRepository(
            api = api,
            dao = db.favoriteStopDao(),
            preferences = preferences
        )
    }

    private fun provideSLService(context: Context): SLService {
        val cacheSize = (10 * 1024 * 1024).toLong() // 10 MB
        val cache = Cache(File(context.cacheDir, "http_cache"), cacheSize)

        val okHttpClient = OkHttpClient.Builder()
            .cache(cache)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request()
                val response = try {
                    val res = chain.proceed(request)
                    if (res.isSuccessful) res else {
                        val cacheRes = tryCache(chain, request)
                        cacheRes ?: res
                    }
                } catch (e: Exception) {
                    tryCache(chain, request) ?: throw e
                }
                
                val cacheControl = if (request.url.toString().contains("nearbystops")) {
                    "public, max-age=3600"
                } else {
                    "public, max-age=60"
                }

                response.newBuilder()
                    .header("Cache-Control", cacheControl)
                    .removeHeader("Pragma")
                    .build()
            }
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

        return Retrofit.Builder()
            .baseUrl("https://api.sl.se/api2/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(SLService::class.java)
    }

    private fun tryCache(chain: okhttp3.Interceptor.Chain, request: okhttp3.Request): okhttp3.Response? {
        return try {
            val cacheRequest = request.newBuilder()
                .cacheControl(okhttp3.CacheControl.Builder()
                    .onlyIfCached()
                    .maxStale(1, TimeUnit.DAYS)
                    .build())
                .build()
            val res = chain.proceed(cacheRequest)
            if (res.code == 504) null else res
        } catch (e: Exception) {
            null
        }
    }
}
