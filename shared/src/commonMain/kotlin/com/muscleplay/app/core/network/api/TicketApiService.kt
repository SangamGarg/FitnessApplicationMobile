package com.muscleplay.app.core.network.api

import com.muscleplay.app.core.network.NetworkResult
import com.muscleplay.app.core.network.dto.CreateSupportTicketRequestDTO
import com.muscleplay.app.core.network.dto.SupportAttachment
import com.muscleplay.app.core.network.dto.SupportTicketHomeResponseDto
import com.muscleplay.app.core.network.dto.SupportTicketMessageResponseDTO
import com.muscleplay.app.core.network.dto.SupportTicketResponseDTO

interface TicketApiService {
    suspend fun fetchUserTickets(accessToken: String): NetworkResult<List<SupportTicketResponseDTO>>
    suspend fun fetchTicketDetails(accessToken: String, ticketNumber: String): NetworkResult<SupportTicketResponseDTO>
    suspend fun fetchTicketMessages(accessToken: String, ticketNumber: String): NetworkResult<List<SupportTicketMessageResponseDTO>>
    suspend fun fetchTicketsHome(accessToken: String): NetworkResult<SupportTicketHomeResponseDto>
    suspend fun createTicket(
        accessToken: String,
        requestDto: CreateSupportTicketRequestDTO,
        files: List<SupportAttachment>,
    ): NetworkResult<SupportTicketResponseDTO>
}
