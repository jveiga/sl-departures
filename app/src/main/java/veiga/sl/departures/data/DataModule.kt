package veiga.sl.departures.data

import android.content.Context
import androidx.room.Room
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import veiga.sl.departures.data.api.SLService
import veiga.sl.departures.data.db.AppDatabase
import veiga.sl.departures.data.local.PreferencesManager
import veiga.sl.departures.data.repository.SLRepository
import java.util.concurrent.TimeUnit

object DataModule {
    private val json =
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

    private fun provideDatabase(context: Context): AppDatabase =
        Room
            .databaseBuilder(
                context,
                AppDatabase::class.java,
                "sl_departures_db",
            ).build()

    fun provideRepository(context: Context): SLRepository {
        val db = provideDatabase(context)
        val api = provideSLService(context)
        val preferences = PreferencesManager(context)
        return SLRepository(
            api = api,
            dao = db.favoriteStopDao(),
            preferences = preferences,
        )
    }

    private fun provideSLService(context: Context): SLService {
        val okHttpClient =
            OkHttpClient
                .Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .addInterceptor { chain ->
                    val request =
                        chain
                            .request()
                            .newBuilder()
                            .header("Accept", "application/json")
                            .build()
                    chain.proceed(request)
                }.addInterceptor(
                    HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.NONE
                    },
                ).build()

        return Retrofit
            .Builder()
            .baseUrl("https://api.trafiklab.se/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(SLService::class.java)
    }
}
