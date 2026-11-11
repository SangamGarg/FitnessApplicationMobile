package com.muscleplay.app.feature.profile.support

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muscleplay.app.core.network.NetworkResult
import com.muscleplay.app.core.network.dto.SupportTicketMessageResponseDTO
import com.muscleplay.app.core.network.dto.SupportTicketResponseDTO
import com.muscleplay.app.feature.profile.support.data.TicketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface TicketsUiState {
    data object Loading : TicketsUiState
    data class Success(val tickets: List<SupportTicketResponseDTO>) : TicketsUiState
    data class Error(val message: String) : TicketsUiState
    data object Empty : TicketsUiState
}

sealed interface TicketDetailsState {
    data object Idle : TicketDetailsState
    data object Loading : TicketDetailsState
    data class Success(val ticket: SupportTicketResponseDTO) : TicketDetailsState
    data class Error(val message: String) : TicketDetailsState
}

sealed interface TicketMessagesState {
    data object Idle : TicketMessagesState
    data object Loading : TicketMessagesState
    data class Success(val messages: List<SupportTicketMessageResponseDTO>) : TicketMessagesState
    data class Error(val message: String) : TicketMessagesState
}

data class MyTicketsUiState(
    val selectedFilter: String = "All",
    val ticketsState: TicketsUiState = TicketsUiState.Loading,
    val selectedTicketNumber: String? = null,
    val ticketDetailsState: TicketDetailsState = TicketDetailsState.Idle,
    val isMessagesDialogVisible: Boolean = false,
    val ticketMessagesState: TicketMessagesState = TicketMessagesState.Idle,
)

class MyTicketsViewModel(
    private val ticketRepository: TicketRepository? = null,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyTicketsUiState())
    val uiState: StateFlow<MyTicketsUiState> = _uiState.asStateFlow()

    init {
        loadUserTickets()
    }

    fun loadUserTickets() {
        val repo = ticketRepository ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(ticketsState = TicketsUiState.Loading)
            when (val result = repo.fetchUserTickets()) {
                is NetworkResult.Success -> {
                    val tickets = result.data
                    if (tickets.isEmpty()) {
                        _uiState.value = _uiState.value.copy(ticketsState = TicketsUiState.Empty)
                    } else {
                        _uiState.value = _uiState.value.copy(ticketsState = TicketsUiState.Success(tickets))
                    }
                }

                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        ticketsState = TicketsUiState.Error(
                            "Unable to load ticket history. Please check your connection and try again."
                        )
                    )
                }

                is NetworkResult.Exception -> {
                    _uiState.value = _uiState.value.copy(
                        ticketsState = TicketsUiState.Error(
                            "Unable to load ticket history. Please check your connection and try again."
                        )
                    )
                }
            }
        }
    }

    fun setFilter(filter: String) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
    }

    fun selectTicket(ticketNumber: String?) {
        if (ticketNumber.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(
                selectedTicketNumber = null,
                ticketDetailsState = TicketDetailsState.Idle,
                isMessagesDialogVisible = false,
                ticketMessagesState = TicketMessagesState.Idle
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            selectedTicketNumber = ticketNumber,
            ticketDetailsState = TicketDetailsState.Loading,
            isMessagesDialogVisible = false,
            ticketMessagesState = TicketMessagesState.Idle
        )

        val repo = ticketRepository ?: return
        viewModelScope.launch {
            when (val result = repo.fetchTicketDetails(ticketNumber)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        ticketDetailsState = TicketDetailsState.Success(result.data)
                    )
                }

                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        ticketDetailsState = TicketDetailsState.Error("Unable to load ticket details. Please try again.")
                    )
                }

                is NetworkResult.Exception -> {
                    _uiState.value = _uiState.value.copy(
                        ticketDetailsState = TicketDetailsState.Error("Unable to load ticket details. Please try again.")
                    )
                }
            }
        }
    }

    fun showMessagesDialog() {
        val ticketNumber = _uiState.value.selectedTicketNumber ?: return
        _uiState.value = _uiState.value.copy(
            isMessagesDialogVisible = true,
            ticketMessagesState = TicketMessagesState.Loading
        )

        val repo = ticketRepository ?: return
        viewModelScope.launch {
            when (val result = repo.fetchTicketMessages(ticketNumber)) {
                is NetworkResult.Success -> {
                    val filteredMessages = result.data.filter { !it.internalNote }
                    _uiState.value = _uiState.value.copy(
                        ticketMessagesState = TicketMessagesState.Success(filteredMessages)
                    )
                }

                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        ticketMessagesState = TicketMessagesState.Error("Unable to load ticket messages. Please try again.")
                    )
                }

                is NetworkResult.Exception -> {
                    _uiState.value = _uiState.value.copy(
                        ticketMessagesState = TicketMessagesState.Error("Unable to load ticket messages. Please try again.")
                    )
                }
            }
        }
    }

    fun closeMessagesDialog() {
        _uiState.value = _uiState.value.copy(
            isMessagesDialogVisible = false,
            ticketMessagesState = TicketMessagesState.Idle
        )
    }
}
