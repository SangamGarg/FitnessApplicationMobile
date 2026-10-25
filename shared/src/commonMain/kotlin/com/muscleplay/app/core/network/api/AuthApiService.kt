package com.muscleplay.app.core.network.api

import com.muscleplay.app.core.network.NetworkResult
import com.muscleplay.app.core.network.dto.AuthResponseDto
import com.muscleplay.app.core.network.dto.SignInRequestDto
import com.muscleplay.app.core.network.dto.SignUpRequestDto
import com.muscleplay.app.core.network.dto.UserDto

interface AuthApiService {
    suspend fun signIn(request: SignInRequestDto): NetworkResult<AuthResponseDto>
    suspend fun signUp(request: SignUpRequestDto): NetworkResult<AuthResponseDto>
    suspend fun getUserProfile(authToken: String): NetworkResult<UserDto>
}
