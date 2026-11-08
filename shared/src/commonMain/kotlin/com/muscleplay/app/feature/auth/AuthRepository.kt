package com.muscleplay.app.feature.auth

import com.muscleplay.app.core.network.ApiClient
import com.muscleplay.app.core.network.NetworkResult
import com.muscleplay.app.core.network.auth.AuthResult
import com.muscleplay.app.core.network.auth.FirebaseAuthRepository
import com.muscleplay.app.core.network.auth.TokenStorageManager
import com.muscleplay.app.core.network.auth.createFirebaseAuthRepository
import com.muscleplay.app.core.network.dto.ResetPasswordWithOtpRequestDto
import com.muscleplay.app.core.network.dto.UserDetailsDto
import com.muscleplay.app.feature.onboarding.model.UserOnboardingProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class SessionState {
    object Unauthenticated : SessionState()
    object Authenticating : SessionState()
    object BackendExchangePending : SessionState()
    object Authenticated : SessionState()
    object ProfileIncomplete : SessionState()
    object SessionExpired : SessionState()
}

data class AuthFlowData(
    val pendingEmail: String = "",
    val pendingPassword: String = "",
    val pendingVerificationToken: String = "",
)

class AuthRepository(
    private val apiClient: ApiClient = ApiClient(),
    private val firebaseAuthRepository: FirebaseAuthRepository = createFirebaseAuthRepository(),
    val tokenStorageManager: TokenStorageManager = TokenStorageManager(),
    private val userSessionRepository: com.muscleplay.app.core.network.auth.UserSessionRepository? = null,
) {
    init {
        apiClient.tokenStorageManager = tokenStorageManager
    }

    private val _sessionState = MutableStateFlow<SessionState>(
        if (!tokenStorageManager.accessToken.isNullOrBlank()) {
            if (tokenStorageManager.isProfileComplete) SessionState.Authenticated else SessionState.ProfileIncomplete
        } else {
            SessionState.Unauthenticated
        }
    )
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    private val _authFlowData = MutableStateFlow(AuthFlowData())
    val authFlowData: StateFlow<AuthFlowData> = _authFlowData.asStateFlow()

    fun setPendingSignUpData(email: String, pass: String) {
        _authFlowData.value = AuthFlowData(pendingEmail = email, pendingPassword = pass)
    }

    fun setPendingResetPasswordData(email: String, token: String) {
        _authFlowData.value = AuthFlowData(pendingEmail = email, pendingVerificationToken = token)
    }

    fun clearAuthFlowData() {
        _authFlowData.value = AuthFlowData()
    }

    suspend fun sendSignupOtp(email: String): NetworkResult<String> {
        return when (val res = apiClient.sendSignupOtp(email)) {
            is NetworkResult.Success -> NetworkResult.Success(
                res.data.message ?: "OTP sent successfully"
            )

            is NetworkResult.Error -> NetworkResult.Error(res.statusCode, res.message)
            is NetworkResult.Exception -> NetworkResult.Exception(res.throwable)
        }
    }

    suspend fun verifySignupOtp(email: String, otp: String): NetworkResult<String> {
        return when (val res = apiClient.verifySignupOtp(email, otp)) {
            is NetworkResult.Success -> NetworkResult.Success(
                res.data.message ?: "OTP verified successfully"
            )

            is NetworkResult.Error -> NetworkResult.Error(res.statusCode, res.message)
            is NetworkResult.Exception -> NetworkResult.Exception(res.throwable)
        }
    }

    suspend fun sendForgotPasswordOtp(email: String): NetworkResult<String> {
        return when (val res = apiClient.sendForgotPasswordOtp(email)) {
            is NetworkResult.Success -> NetworkResult.Success(
                res.data.message ?: "OTP sent successfully"
            )

            is NetworkResult.Error -> NetworkResult.Error(res.statusCode, res.message)
            is NetworkResult.Exception -> NetworkResult.Exception(res.throwable)
        }
    }

    suspend fun verifyForgotPasswordOtp(email: String, otp: String): NetworkResult<String> {
        return when (val res = apiClient.verifyForgotPasswordOtp(email, otp)) {
            is NetworkResult.Success -> {
                val token = res.data.verificationToken
                if (token.isNullOrBlank()) {
                    NetworkResult.Error(400, "Verification token missing from server response.")
                } else {
                    NetworkResult.Success(token)
                }
            }

            is NetworkResult.Error -> NetworkResult.Error(res.statusCode, res.message)
            is NetworkResult.Exception -> NetworkResult.Exception(res.throwable)
        }
    }

    suspend fun resetPasswordWithOtp(
        email: String,
        verificationToken: String,
        newPass: String,
    ): NetworkResult<String> {
        val req = ResetPasswordWithOtpRequestDto(
            email = email,
            verificationToken = verificationToken,
            newPassword = newPass
        )
        return when (val res = apiClient.resetPasswordWithOtp(req)) {
            is NetworkResult.Success -> NetworkResult.Success(
                res.data.message ?: "Password reset successfully. Please sign in."
            )

            is NetworkResult.Error -> NetworkResult.Error(res.statusCode, res.message)
            is NetworkResult.Exception -> NetworkResult.Exception(res.throwable)
        }
    }

    suspend fun authenticateWithEmail(
        email: String,
        pass: String,
        isSignUp: Boolean,
    ): NetworkResult<Boolean> {
        _sessionState.value = SessionState.Authenticating

        val firebaseResult = if (isSignUp) {
            firebaseAuthRepository.signUpWithEmail(email, pass)
        } else {
            firebaseAuthRepository.signInWithEmail(email, pass)
        }

        when (firebaseResult) {
            is AuthResult.Success -> { /* Continue */ }
            is AuthResult.Error -> {
                _sessionState.value = SessionState.Unauthenticated
                return NetworkResult.Error(401, firebaseResult.message)
            }
        }

        _sessionState.value = SessionState.BackendExchangePending

        val realFirebaseToken = firebaseAuthRepository.getIdToken(forceRefresh = false)
        if (realFirebaseToken.isNullOrBlank()) {
            _sessionState.value = SessionState.Unauthenticated
            return NetworkResult.Error(401, "Failed to retrieve authentication token. Please try again.")
        }

        return when (val backendRes = apiClient.loginRegister(realFirebaseToken)) {
            is NetworkResult.Success -> {
                val data = backendRes.data
                tokenStorageManager.saveTokens(
                    accessToken = data.accessToken,
                    refreshToken = data.refreshToken,
                    profileComplete = data.profileComplete,
                    email = email
                )

                if (data.profileComplete) {
                    _sessionState.value = SessionState.Authenticated
                } else {
                    _sessionState.value = SessionState.ProfileIncomplete
                }
                NetworkResult.Success(data.profileComplete)
            }

            is NetworkResult.Error -> {
                _sessionState.value = SessionState.Unauthenticated
                NetworkResult.Error(backendRes.statusCode, backendRes.message)
            }

            is NetworkResult.Exception -> {
                _sessionState.value = SessionState.Unauthenticated
                NetworkResult.Exception(backendRes.throwable)
            }
        }
    }

    suspend fun submitProfileCompletion(profile: UserOnboardingProfile): NetworkResult<Boolean> {
        val accessToken = tokenStorageManager.accessToken
        if (accessToken.isNullOrBlank()) {
            _sessionState.value = SessionState.Unauthenticated
            return NetworkResult.Error(401, "Authentication token missing. Please sign in again.")
        }

        val dto = UserDetailsDto(
            name = profile.fullName,
            gender = profile.gender.name,
            age = profile.age,
            heightCm = profile.heightCm.toFloat(),
            weightKg = profile.weightKg.toFloat(),
            activityLevel = profile.activityLevel.name,
            goal = profile.fitnessGoal.name,
            bmi = (profile.weightKg / ((profile.heightCm / 100) * (profile.heightCm / 100))).toFloat(),
            dailyCalories = profile.targetDailyCalories.toFloat()
        )

        return when (val res = apiClient.addUserDetails(accessToken, dto)) {
            is NetworkResult.Success -> {
                tokenStorageManager.updateProfileComplete(true)
                userSessionRepository?.updateProfile(
                    com.muscleplay.app.core.network.auth.UserProfile(
                        name = profile.fullName,
                        gender = profile.gender.name,
                        age = profile.age,
                        heightCm = profile.heightCm,
                        weightKg = profile.weightKg,
                        activityLevel = profile.activityLevel.name,
                        goal = profile.fitnessGoal.name,
                        profileCompleted = true
                    )
                )
                _sessionState.value = SessionState.Authenticated
                NetworkResult.Success(true)
            }

            is NetworkResult.Error -> NetworkResult.Error(res.statusCode, res.message)
            is NetworkResult.Exception -> NetworkResult.Exception(res.throwable)
        }
    }

    suspend fun logout(logoutEverywhere: Boolean = false): Boolean {
        val accessToken = tokenStorageManager.accessToken ?: ""
        val refreshToken = tokenStorageManager.refreshToken ?: ""
        if (refreshToken.isNotBlank() && accessToken.isNotBlank()) {
            apiClient.logout(refreshToken, logoutEverywhere, accessToken)
        }
        firebaseAuthRepository.signOut()
        tokenStorageManager.clearSession()
        userSessionRepository?.clearSession()
        clearAuthFlowData()
        _sessionState.value = SessionState.Unauthenticated
        return true
    }

    suspend fun deleteAccount(): Boolean {
        val accessToken = tokenStorageManager.accessToken ?: ""
        if (accessToken.isNotBlank()) {
            apiClient.deleteUserAccount(accessToken)
        }
        return logout(true)
    }
}
