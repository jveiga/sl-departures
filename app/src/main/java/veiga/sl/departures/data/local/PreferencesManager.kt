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
        return if (key.isNullOrBlank()) "eb641c93cb8a4604a13a9d76ee0e2c96" else key
    }

    fun saveApiKey(key: String) {
        sharedPreferences.edit().putString("api_key", key).apply()
    }

    fun clear() {
        sharedPreferences.edit().clear().apply()
    }
}
