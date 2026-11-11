package com.muscleplay.app.feature.profile.support

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muscleplay.app.core.network.dto.SupportTicketResponseDTO

private val OrangePrimary = Color(0xFFFF5722)
private val OrangeSecondary = Color(0xFFFF9800)
private val DarkBackground = Color(0xFF0F0F17)
private val TextMuted = Color(0xFF9E9B9E)
private val CardBackground = Color(0xFF181824)
private val CardBorderColor = Color(0xFF2A283A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTicketsScreen(
    ticketRepository: com.muscleplay.app.feature.profile.support.data.TicketRepository? = null,
    onBack: () -> Unit,
    onRaiseTicket: () -> Unit = {},
) {
    val viewModel = remember(ticketRepository) { MyTicketsViewModel(ticketRepository) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("My Tickets", fontWeight = FontWeight.Bold, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = onRaiseTicket,
                    containerColor = OrangePrimary,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Add, contentDescription = null, tint = Color.White) },
                    text = { Text("Raise New Ticket", fontWeight = FontWeight.Bold) }
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        items(listOf("All", "Open", "In Progress", "Resolved", "Closed")) { filter ->
                            FilterChip(
                                selected = uiState.selectedFilter == filter,
                                onClick = { viewModel.setFilter(filter) },
                                label = { Text(filter, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = OrangePrimary,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color.White.copy(alpha = 0.08f),
                                    labelColor = Color.White.copy(alpha = 0.7f)
                                )
                            )
                        }
                    }
                }

                when (val state = uiState.ticketsState) {
                    is TicketsUiState.Loading -> {
                        items(3) {
                            ShimmerTicketCard()
                        }
                    }

                    is TicketsUiState.Success -> {
                        val filteredList = filterTickets(state.tickets, uiState.selectedFilter)
                        if (filteredList.isEmpty()) {
                            item {
                                Text(
                                    text = "No tickets to show.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextMuted,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
                                )
                            }
                        } else {
                            items(filteredList, key = { it.ticketNumber.ifBlank { it.hashCode().toString() } }) { ticket ->
                                GlassTicketCard(
                                    ticket = ticket,
                                    onClick = { viewModel.selectTicket(ticket.ticketNumber) }
                                )
                            }
                        }
                    }

                    is TicketsUiState.Empty -> {
                        item {
                            Text(
                                text = "No tickets to show.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
                            )
                        }
                    }

                    is TicketsUiState.Error -> {
                        item {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                            ) {
                                Text(
                                    text = state.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextMuted,
                                    textAlign = TextAlign.Center
                                )
                                OutlinedButton(
                                    onClick = { viewModel.loadUserTickets() },
                                    shape = RoundedCornerShape(14.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, OrangePrimary),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangePrimary)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Retry", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Ticket Details Dialog
            if (uiState.selectedTicketNumber != null) {
                GlassTicketDetailsDialog(
                    detailsState = uiState.ticketDetailsState,
                    onDismiss = { viewModel.selectTicket(null) },
                    onViewMessages = { viewModel.showMessagesDialog() },
                    onRetry = { viewModel.selectTicket(uiState.selectedTicketNumber) }
                )
            }

            // Ticket Messages Dialog
            if (uiState.isMessagesDialogVisible) {
                GlassTicketMessagesDialog(
                    messagesState = uiState.ticketMessagesState,
                    onDismiss = { viewModel.closeMessagesDialog() },
                    onRetry = { viewModel.showMessagesDialog() }
                )
            }
        }
    }
}

private fun filterTickets(tickets: List<SupportTicketResponseDTO>, filter: String): List<SupportTicketResponseDTO> {
    return when (filter) {
        "Open" -> tickets.filter { it.status.equals("OPEN", ignoreCase = true) }
        "In Progress" -> tickets.filter { it.status.equals("IN_PROGRESS", ignoreCase = true) }
        "Resolved" -> tickets.filter { it.status.equals("RESOLVED", ignoreCase = true) }
        "Closed" -> tickets.filter { it.status.equals("CLOSED", ignoreCase = true) }
        else -> tickets
    }
}

@Composable
private fun GlassTicketCard(
    ticket: SupportTicketResponseDTO,
    onClick: () -> Unit,
) {
    val statusColor = when (ticket.status.uppercase()) {
        "RESOLVED", "CLOSED" -> Color(0xFF4CAF50)
        "IN_PROGRESS" -> OrangeSecondary
        else -> OrangePrimary
    }

    GlassCardContainer(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = ticket.ticketNumber.ifBlank { "#TKT-REF" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Surface(
                    color = statusColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = ticket.status.replace("_", " "),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = ticket.subject,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = ticket.category ?: "Support Ticket",
                    style = MaterialTheme.typography.labelSmall,
                    color = OrangeSecondary
                )

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "View Details",
                    tint = Color.White.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun ShimmerTicketCard() {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    GlassCardContainer(modifier = Modifier.fillMaxWidth().height(100.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White.copy(alpha = alpha))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White.copy(alpha = alpha))
            )
        }
    }
}

@Composable
private fun GlassTicketDetailsDialog(
    detailsState: TicketDetailsState,
    onDismiss: () -> Unit,
    onViewMessages: () -> Unit,
    onRetry: () -> Unit,
) {
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color(0xFF181824),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Ticket Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White.copy(alpha = 0.7f))
                }
            }
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 340.dp)
                    .verticalScroll(scrollState)
            ) {
                when (detailsState) {
                    is TicketDetailsState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(180.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = OrangePrimary)
                        }
                    }

                    is TicketDetailsState.Success -> {
                        val ticket = detailsState.ticket
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = ticket.ticketNumber,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = OrangeSecondary
                            )

                            DetailRow("Subject:", ticket.subject)
                            DetailRow("Category:", ticket.category ?: "General")
                            DetailRow("Status:", ticket.status)
                            DetailRow("Priority:", ticket.priority)
                            DetailRow("Created At:", formatCreatedAtDate(ticket.createdAt))
                            DetailRow("Resolution:", ticket.resolutionNotes?.takeIf { it.isNotBlank() } ?: "No resolution yet")

                            ticket.description.takeIf { it.isNotBlank() }?.let {
                                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                                Text("Description:", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                                Text(it, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                            }

//                            ticket.assignedToEmail?.takeIf { it.isNotBlank() }?.let { email ->
//                                DetailRow("Assigned To:", email)
//                            }
                        }
                    }

                    is TicketDetailsState.Error -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                        ) {
                            Text(
                                text = detailsState.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )
                            OutlinedButton(
                                onClick = onRetry,
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, OrangePrimary),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangePrimary)
                            ) {
                                Text("Retry", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    else -> {}
                }
            }
        },
        confirmButton = {
            if (detailsState is TicketDetailsState.Success) {
                Button(
                    onClick = onViewMessages,
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    Icon(Icons.Default.Forum, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("View Messages", fontWeight = FontWeight.Bold)
                }
            }
        }
    )
}

@Composable
private fun GlassTicketMessagesDialog(
    messagesState: TicketMessagesState,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
) {
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color(0xFF181824),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Ticket Messages",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White.copy(alpha = 0.7f))
                }
            }
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp)
                    .verticalScroll(scrollState)
            ) {
                when (messagesState) {
                    is TicketMessagesState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = OrangePrimary)
                        }
                    }

                    is TicketMessagesState.Success -> {
                        val messages = messagesState.messages
                        if (messages.isEmpty()) {
                            Text(
                                text = "No messages available yet.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                messages.forEach { msg ->
                                    Surface(
                                        color = Color.White.copy(alpha = 0.06f),
                                        shape = RoundedCornerShape(12.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = msg.actorEmail ?: "Support Agent",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = OrangeSecondary
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = msg.message,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    is TicketMessagesState.Error -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                        ) {
                            Text(
                                text = messagesState.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )
                            OutlinedButton(
                                onClick = onRetry,
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, OrangePrimary),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangePrimary)
                            ) {
                                Text("Retry", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    else -> {}
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
            ) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.6f)
        )
    }
}

private fun formatCreatedAtDate(rawIso: String?): String {
    if (rawIso.isNullOrBlank()) return "N/A"
    return try {
        val cleanIso = rawIso.trim().removeSuffix("Z")
        val parts = cleanIso.split("T", " ")
        if (parts.size >= 2) {
            val dateParts = parts[0].split("-")
            val timeParts = parts[1].split(":")
            if (dateParts.size == 3 && timeParts.size >= 2) {
                val year = dateParts[0]
                val month = dateParts[1]
                val day = dateParts[2]
                val hour = timeParts[0]
                val min = timeParts[1]
                return "$day/$month/$year $hour:$min"
            }
        }
        rawIso
    } catch (e: Throwable) {
        rawIso
    }
}

@Composable
private fun GlassCardContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.10f),
                        Color.White.copy(alpha = 0.03f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.30f),
                        OrangePrimary.copy(alpha = 0.20f)
                    )
                ),
                shape = RoundedCornerShape(22.dp)
            )
    ) {
        content()
    }
}
