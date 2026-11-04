package com.muscleplay.app.core.network.auth

import android.content.Context
import android.content.SharedPreferences

object AndroidAppContext {
    var applicationContext: Context? = null
}

class AndroidSessionStorage(
    private val contextProvider: () -> Context? = { AndroidAppContext.applicationContext }
) : SessionStorage {

    private val inMemoryFallback = mutableMapOf<String, Any?>()

    private val prefs: SharedPreferences?
        get() = contextProvider()?.getSharedPreferences("muscleplay_session_prefs", Context.MODE_PRIVATE)

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
        return if (p != null) {
            p.getBoolean(key, defaultValue)
        } else {
            (inMemoryFallback[key] as? Boolean) ?: defaultValue
        }
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
