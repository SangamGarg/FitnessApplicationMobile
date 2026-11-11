package com.muscleplay.app.feature.profile.support.data

import com.muscleplay.app.core.network.ApiClient
import com.muscleplay.app.core.network.NetworkResult
import com.muscleplay.app.core.network.auth.TokenStorageManager
import com.muscleplay.app.core.network.dto.EmailFromUserRequestDto
import com.muscleplay.app.core.network.dto.EmailFromUserResponseDto
import com.muscleplay.app.core.network.dto.SupportAttachment
import com.muscleplay.app.core.network.dto.SupportEmailHomeResponseDto

interface EmailSupportRepository {
    suspend fun fetchEmailSupportHome(): NetworkResult<SupportEmailHomeResponseDto>
    suspend fun sendSupportEmail(
        requestDto: EmailFromUserRequestDto,
        attachments: List<SupportAttachment>,
    ): NetworkResult<EmailFromUserResponseDto>
}

class EmailSupportRepositoryImpl(
    private val apiClient: ApiClient,
    private val tokenStorageManager: TokenStorageManager,
) : EmailSupportRepository {

    override suspend fun fetchEmailSupportHome(): NetworkResult<SupportEmailHomeResponseDto> {
        val accessToken = tokenStorageManager.accessToken ?: ""
        return apiClient.fetchEmailSupportHome(accessToken)
    }

    override suspend fun sendSupportEmail(
        requestDto: EmailFromUserRequestDto,
        attachments: List<SupportAttachment>,
    ): NetworkResult<EmailFromUserResponseDto> {
        val accessToken = tokenStorageManager.accessToken ?: ""
        return apiClient.sendSupportEmail(accessToken, requestDto, attachments)
    }
}
