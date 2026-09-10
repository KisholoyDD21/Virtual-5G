package com.virtual5g.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.virtual5g.core.logging.SecureLogger

/**
 * Small Keystore-backed key/value store. Currently holds exactly one thing:
 * whether this device has ever been observed attached via real 5G NR (see
 * DeviceCapabilityManager for why that has to be persisted rather than
 * queried fresh each time). Falls back to a plain, non-encrypted
 * SharedPreferences file only if Keystore initialization itself fails
 * (some OEM/security-provider edge cases) - never crashes the app over this.
 */
class SecureFlagStore(context: Context) {

    private val prefs: SharedPreferences = runCatching {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }.getOrElse { error ->
        SecureLogger.w("SecureFlagStore", "Falling back to unencrypted prefs", error)
        context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
    }

    fun hasEverObservedNrAttach(): Boolean = prefs.getBoolean(KEY_EVER_OBSERVED_NR, false)

    fun markNrAttachObserved() {
        prefs.edit().putBoolean(KEY_EVER_OBSERVED_NR, true).apply()
    }

    private companion object {
        const val FILE_NAME = "virtual5g_secure_flags"
        const val KEY_EVER_OBSERVED_NR = "ever_observed_nr_attach"
    }
}
