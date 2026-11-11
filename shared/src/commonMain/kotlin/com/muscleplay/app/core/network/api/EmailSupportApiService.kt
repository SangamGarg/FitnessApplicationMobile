package com.muscleplay.app.core.network.api

import com.muscleplay.app.core.network.NetworkResult
import com.muscleplay.app.core.network.dto.EmailFromUserRequestDto
import com.muscleplay.app.core.network.dto.EmailFromUserResponseDto
import com.muscleplay.app.core.network.dto.SupportAttachment
import com.muscleplay.app.core.network.dto.SupportEmailHomeResponseDto

interface EmailSupportApiService {
    suspend fun fetchEmailSupportHome(accessToken: String): NetworkResult<SupportEmailHomeResponseDto>
    suspend fun sendSupportEmail(
        accessToken: String,
        requestDto: EmailFromUserRequestDto,
        attachments: List<SupportAttachment>,
    ): NetworkResult<EmailFromUserResponseDto>
}
