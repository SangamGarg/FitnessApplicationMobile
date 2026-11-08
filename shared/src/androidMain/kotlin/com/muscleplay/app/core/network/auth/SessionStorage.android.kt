package com.muscleplay.app.core.network.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object AndroidAppContext {
    var applicationContext: Context? = null
}

class AndroidSessionStorage(
    private val contextProvider: () -> Context? = { AndroidAppContext.applicationContext },
) : SessionStorage {

    private val inMemoryFallback = mutableMapOf<String, Any?>()

    private val prefs: SharedPreferences?
        get() {
            val ctx = contextProvider() ?: return null
            return try {
                val masterKey = MasterKey.Builder(ctx)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()

                EncryptedSharedPreferences.create(
                    ctx,
                    "muscleplay_encrypted_session_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            } catch (e: Throwable) {
                try {
                    ctx.getSharedPreferences("muscleplay_session_prefs", Context.MODE_PRIVATE)
                } catch (ex: Throwable) {
                    null
                }
            }
        }

    override fun getString(key: String): String? {
        val p = prefs
        return if (p != null) {
            p.getString(key, null)
        } else {
            inMemoryFallback[key] as? String
        }
    }

    override fun putString(key: String, value: String?) {
        inMemoryFallback[key] = value
        val p = prefs
        p?.edit()?.apply {
            if (value == null) remove(key) else putString(key, value)
            apply()
        }
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        val p = prefs
        return p?.getBoolean(key, defaultValue)
            ?: ((inMemoryFallback[key] as? Boolean) ?: defaultValue)
    }

    override fun putBoolean(key: String, value: Boolean) {
        inMemoryFallback[key] = value
        val p = prefs
        p?.edit()?.apply {
            putBoolean(key, value)
            apply()
        }
    }

    override fun clear() {
        inMemoryFallback.clear()
        prefs?.edit()?.clear()?.apply()
    }
}

actual fun createPlatformSessionStorage(): SessionStorage {
    return AndroidSessionStorage()
}
