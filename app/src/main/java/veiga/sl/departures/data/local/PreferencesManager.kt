package veiga.sl.departures.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class PreferencesManager(
    context: Context,
) {
    private val masterKey =
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

    private val sharedPreferences =
        EncryptedSharedPreferences.create(
            context,
            "sl_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )

    fun getApiKey(): String? {
        val key = sharedPreferences.getString("api_key", null)
        return if (key.isNullOrBlank()) veiga.sl.departures.BuildConfig.SL_NEARBY_API_KEY else key
    }

    fun saveApiKey(key: String) {
        sharedPreferences.edit().putString("api_key", key).apply()
    }

    fun clear() {
        sharedPreferences.edit().clear().apply()
    }
}
