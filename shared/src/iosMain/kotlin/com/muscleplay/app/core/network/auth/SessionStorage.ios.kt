package com.muscleplay.app.core.network.auth

import platform.Foundation.NSUserDefaults

class IosSessionStorage : SessionStorage {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun getString(key: String): String? {
        return defaults.stringForKey(key)
    }

    override fun putString(key: String, value: String?) {
        if (value == null) {
            defaults.removeObjectForKey(key)
        } else {
            defaults.setObject(value, forKey = key)
        }
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return if (defaults.objectForKey(key) != null) {
            defaults.boolForKey(key)
        } else {
            defaultValue
        }
    }

    override fun putBoolean(key: String, value: Boolean) {
        defaults.setBool(value, forKey = key)
    }

    override fun clear() {
        defaults.removeObjectForKey("access_token")
        defaults.removeObjectForKey("refresh_token")
        defaults.removeObjectForKey("is_profile_complete")
        defaults.removeObjectForKey("saved_email")
        defaults.removeObjectForKey("saved_user_name")
    }
}

actual fun createPlatformSessionStorage(): SessionStorage {
    return IosSessionStorage()
}
