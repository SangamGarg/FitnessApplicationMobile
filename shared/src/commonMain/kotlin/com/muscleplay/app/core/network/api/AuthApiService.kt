package com.muscleplay.app.core.network.api

import com.muscleplay.app.core.network.NetworkResult
import com.muscleplay.app.core.network.dto.*

interface AuthApiService {
    suspend fun sendSignupOtp(email: String): NetworkResult<OtpActionResponseDto>
    suspend fun verifySignupOtp(email: String, otp: String): NetworkResult<OtpActionResponseDto>
    suspend fun sendForgotPasswordOtp(email: String): NetworkResult<OtpActionResponseDto>
    suspend fun verifyForgotPasswordOtp(email: String, otp: String): NetworkResult<OtpActionResponseDto>
    suspend fun resetPasswordWithOtp(request: ResetPasswordWithOtpRequestDto): NetworkResult<OtpActionResponseDto>
    suspend fun loginRegister(firebaseIdToken: String): NetworkResult<UserRegisterResponseDto>
    suspend fun refreshToken(refreshToken: String): NetworkResult<RefreshTokenResponseDto>
    suspend fun logout(refreshToken: String, logoutEverywhere: Boolean = false): NetworkResult<Boolean>
    suspend fun addUserDetails(accessToken: String, details: UserDetailsDto): NetworkResult<UserDetailsResponseDto>
    suspend fun patchUserDetails(accessToken: String, patch: UserPatchRequestDto): NetworkResult<UserDetailsResponseDto>
    suspend fun getUserDetails(accessToken: String): NetworkResult<UserDetailsResponseDto>
    suspend fun deleteUserAccount(accessToken: String): NetworkResult<Boolean>

    suspend fun signIn(request: SignInRequestDto): NetworkResult<AuthResponseDto>
    suspend fun signUp(request: SignUpRequestDto): NetworkResult<AuthResponseDto>
    suspend fun getUserProfile(authToken: String): NetworkResult<UserDto>
}
