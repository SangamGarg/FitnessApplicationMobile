package com.muscleplay.app.core.network.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TokenStorageManager {
    var accessToken: String? = null
        private set

    var refreshToken: String? = null
        private set

    private val _isProfileComplete = MutableStateFlow(false)
    val isProfileCompleteFlow: StateFlow<Boolean> = _isProfileComplete.asStateFlow()

    val isProfileComplete: Boolean
        get() = _isProfileComplete.value

    var savedEmail: String? = null
        private set

    var savedUserName: String? = null
        private set

    fun saveTokens(
        accessToken: String?,
        refreshToken: String?,
        profileComplete: Boolean = false,
        email: String? = null,
        userName: String? = null
    ) {
        if (!accessToken.isNullOrBlank()) {
            this.accessToken = accessToken
        }
        if (!refreshToken.isNullOrBlank()) {
            this.refreshToken = refreshToken
        }
        this._isProfileComplete.value = profileComplete
        if (!email.isNullOrBlank()) {
            this.savedEmail = email
        }
        if (!userName.isNullOrBlank()) {
            this.savedUserName = userName
        }
    }

    fun updateProfileComplete(complete: Boolean) {
        this._isProfileComplete.value = complete
    }

    fun clearSession() {
        this.accessToken = null
        this.refreshToken = null
        this._isProfileComplete.value = false
        this.savedEmail = null
        this.savedUserName = null
    }
}
