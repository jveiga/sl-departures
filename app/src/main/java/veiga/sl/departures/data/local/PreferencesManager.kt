package veiga.sl.departures.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class PreferencesManager(
    context: Context,
) {
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val sharedPreferences =
        EncryptedSharedPreferences.create(
            "sl_prefs",
            masterKeyAlias,
            context,
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
