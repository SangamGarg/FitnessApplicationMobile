package com.muscleplay.app.core.network.auth

interface SessionStorage {
    fun getString(key: String): String?
    fun putString(key: String, value: String?)
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean
    fun putBoolean(key: String, value: Boolean)
    fun clear()
}

expect fun createPlatformSessionStorage(): SessionStorage
