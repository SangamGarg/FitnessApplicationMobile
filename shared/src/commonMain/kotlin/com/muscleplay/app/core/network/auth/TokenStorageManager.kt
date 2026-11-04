package com.muscleplay.app.core.network.auth

import com.muscleplay.app.core.network.NetworkResult
import com.muscleplay.app.core.network.dto.RefreshTokenResponseDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.concurrent.Volatile

class TokenStorageManager(
    private val sessionStorage: SessionStorage = createPlatformSessionStorage()
) {
    private val refreshMutex = Mutex()

    @Volatile
    var accessToken: String? = sessionStorage.getString("access_token")
        private set

    @Volatile
    var refreshToken: String? = sessionStorage.getString("refresh_token")
        private set

    private val _isProfileComplete = MutableStateFlow(
        sessionStorage.getBoolean("is_profile_complete", false)
    )
    val isProfileCompleteFlow: StateFlow<Boolean> = _isProfileComplete.asStateFlow()

    val isProfileComplete: Boolean
        get() = _isProfileComplete.value

    @Volatile
    var savedEmail: String? = sessionStorage.getString("saved_email")
        private set

    @Volatile
    var savedUserName: String? = sessionStorage.getString("saved_user_name")
        private set

    @Volatile
    var lastRegisteredFcmToken: String? = sessionStorage.getString("last_registered_fcm_token")
        private set

    private val _onSessionExpired = MutableStateFlow(false)
    val onSessionExpired: StateFlow<Boolean> = _onSessionExpired.asStateFlow()

    fun saveTokens(
        accessToken: String?,
        refreshToken: String?,
        profileComplete: Boolean = false,
        email: String? = null,
        userName: String? = null
    ) {
        if (!accessToken.isNullOrBlank()) {
            this.accessToken = accessToken
            sessionStorage.putString("access_token", accessToken)
        }
        if (!refreshToken.isNullOrBlank()) {
            this.refreshToken = refreshToken
            sessionStorage.putString("refresh_token", refreshToken)
        }
        this._isProfileComplete.value = profileComplete
        sessionStorage.putBoolean("is_profile_complete", profileComplete)

        if (!email.isNullOrBlank()) {
            this.savedEmail = email
            sessionStorage.putString("saved_email", email)
        }
        if (!userName.isNullOrBlank()) {
            this.savedUserName = userName
            sessionStorage.putString("saved_user_name", userName)
        }
    }

    fun updateProfileComplete(complete: Boolean) {
        this._isProfileComplete.value = complete
        sessionStorage.putBoolean("is_profile_complete", complete)
    }

    fun saveRegisteredFcmToken(token: String) {
        this.lastRegisteredFcmToken = token
        sessionStorage.putString("last_registered_fcm_token", token)
    }

    fun clearSession() {
        this.accessToken = null
        this.refreshToken = null
        this._isProfileComplete.value = false
        this.savedEmail = null
        this.savedUserName = null
        this.lastRegisteredFcmToken = null
        sessionStorage.clear()
    }

    /**
     * Production-grade, thread-safe Token Refresh workflow with single Mutex lock gate.
     * Prevents duplicate/concurrent refresh calls when multiple 401s occur simultaneously.
     */
    suspend fun performTokenRefresh(
        refreshCall: suspend (refreshToken: String) -> NetworkResult<RefreshTokenResponseDto>,
        failedToken: String?
    ): NetworkResult<String> {
        return refreshMutex.withLock {
            // Double-Check: If another thread refreshed the access token while we waited for the lock
            val currentAccess = accessToken
            if (!currentAccess.isNullOrBlank() && currentAccess != failedToken) {
                return NetworkResult.Success(currentAccess)
            }

            val currentRefresh = refreshToken
            if (currentRefresh.isNullOrBlank()) {
                clearSession()
                _onSessionExpired.value = true
                return NetworkResult.Error(401, "Session expired. Please sign in again.")
            }

            when (val result = refreshCall(currentRefresh)) {
                is NetworkResult.Success -> {
                    val newAccess = result.data.accessToken
                    val newRefresh = result.data.refreshToken ?: currentRefresh
                    if (!newAccess.isNullOrBlank()) {
                        saveTokens(
                            accessToken = newAccess,
                            refreshToken = newRefresh,
                            profileComplete = isProfileComplete,
                            email = savedEmail,
                            userName = savedUserName
                        )
                        _onSessionExpired.value = false
                        NetworkResult.Success(newAccess)
                    } else {
                        clearSession()
                        _onSessionExpired.value = true
                        NetworkResult.Error(401, "Invalid refresh token response from server.")
                    }
                }
                is NetworkResult.Error -> {
                    if (result.statusCode == 401 || result.statusCode == 403) {
                        clearSession()
                        _onSessionExpired.value = true
                    }
                    NetworkResult.Error(result.statusCode, result.message)
                }
                is NetworkResult.Exception -> {
                    NetworkResult.Exception(result.throwable)
                }
            }
        }
    }
}
