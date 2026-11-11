package com.muscleplay.app.feature.profile.support.data

import com.muscleplay.app.core.network.ApiClient
import com.muscleplay.app.core.network.NetworkResult
import com.muscleplay.app.core.network.auth.TokenStorageManager
import com.muscleplay.app.core.network.dto.CreateSupportTicketRequestDTO
import com.muscleplay.app.core.network.dto.SupportAttachment
import com.muscleplay.app.core.network.dto.SupportTicketHomeResponseDto
import com.muscleplay.app.core.network.dto.SupportTicketMessageResponseDTO
import com.muscleplay.app.core.network.dto.SupportTicketResponseDTO

interface TicketRepository {
    suspend fun fetchUserTickets(): NetworkResult<List<SupportTicketResponseDTO>>
    suspend fun fetchTicketDetails(ticketNumber: String): NetworkResult<SupportTicketResponseDTO>
    suspend fun fetchTicketMessages(ticketNumber: String): NetworkResult<List<SupportTicketMessageResponseDTO>>
    suspend fun fetchTicketsHome(): NetworkResult<SupportTicketHomeResponseDto>
    suspend fun createTicket(
        requestDto: CreateSupportTicketRequestDTO,
        files: List<SupportAttachment>,
    ): NetworkResult<SupportTicketResponseDTO>
}

class TicketRepositoryImpl(
    private val apiClient: ApiClient,
    private val tokenStorageManager: TokenStorageManager,
) : TicketRepository {

    override suspend fun fetchUserTickets(): NetworkResult<List<SupportTicketResponseDTO>> {
        val accessToken = tokenStorageManager.accessToken ?: ""
        return apiClient.fetchUserTickets(accessToken)
    }

    override suspend fun fetchTicketDetails(ticketNumber: String): NetworkResult<SupportTicketResponseDTO> {
        val accessToken = tokenStorageManager.accessToken ?: ""
        return apiClient.fetchTicketDetails(accessToken, ticketNumber)
    }

    override suspend fun fetchTicketMessages(ticketNumber: String): NetworkResult<List<SupportTicketMessageResponseDTO>> {
        val accessToken = tokenStorageManager.accessToken ?: ""
        return apiClient.fetchTicketMessages(accessToken, ticketNumber)
    }

    override suspend fun fetchTicketsHome(): NetworkResult<SupportTicketHomeResponseDto> {
        val accessToken = tokenStorageManager.accessToken ?: ""
        return apiClient.fetchTicketsHome(accessToken)
    }

    override suspend fun createTicket(
        requestDto: CreateSupportTicketRequestDTO,
        files: List<SupportAttachment>,
    ): NetworkResult<SupportTicketResponseDTO> {
        val accessToken = tokenStorageManager.accessToken ?: ""
        return apiClient.createTicket(accessToken, requestDto, files)
    }
}
