package com.muscleplay.app.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val OrangePrimary = Color(0xFFFF5722)
private val OrangeSecondary = Color(0xFFFF9800)
private val DarkBackground = Color(0xFF0F0F17)

data class SupportTicketItem(
    val id: String,
    val subject: String,
    val category: String,
    val status: String,
    val date: String,
    val response: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTicketsScreen(
    onBack: () -> Unit,
    onRaiseTicket: () -> Unit = {}
) {
    val tickets = listOf(
        SupportTicketItem(
            id = "#MP-84920",
            subject = "Pose tracking calibration issue in Squat Master",
            category = "Pose Tracking & Rep Counting",
            status = "In Progress",
            date = "Today, 10:30 AM",
            response = "Our engineering team is inspecting camera frame timing for squat depth thresholds."
        ),
        SupportTicketItem(
            id = "#MP-83102",
            subject = "Subscription upgrade receipt missing",
            category = "Account & Subscription",
            status = "Resolved",
            date = "Jan 12, 2025",
            response = "Receipt has been resent to your registered email address. Thank you!"
        ),
        SupportTicketItem(
            id = "#MP-81045",
            subject = "Custom exercise request for Lunges",
            category = "Category Not Present / Other",
            status = "Resolved",
            date = "Dec 28, 2024",
            response = "Lunge exercise detector is coming in the next release update!"
        )
    )

    var selectedFilter by remember { mutableStateOf("All") }
    var selectedTicket by remember { mutableStateOf<SupportTicketItem?>(null) }

    val filteredTickets = when (selectedFilter) {
        "Open" -> tickets.filter { it.status != "Resolved" }
        "Resolved" -> tickets.filter { it.status == "Resolved" }
        else -> tickets
    }

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
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
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
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf("All", "Open", "Resolved").forEach { filter ->
                            FilterChip(
                                selected = selectedFilter == filter,
                                onClick = { selectedFilter = filter },
                                label = { Text(filter, fontWeight = FontWeight.Bold) },
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

                items(filteredTickets) { ticket ->
                    GlassTicketCard(
                        ticket = ticket,
                        onClick = { selectedTicket = ticket }
                    )
                }
            }

            selectedTicket?.let { ticket ->
                AlertDialog(
                    onDismissRequest = { selectedTicket = null },
                    title = { Text("Ticket Details - ${ticket.id}") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = ticket.subject, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Category: ${ticket.category}", style = MaterialTheme.typography.bodySmall, color = OrangeSecondary)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Latest Support Update:", fontWeight = FontWeight.SemiBold)
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White.copy(alpha = 0.08f)
                                )
                            ) {
                                Text(
                                    text = ticket.response,
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(onClick = { selectedTicket = null }, colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)) {
                            Text("Close")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun GlassTicketCard(
    ticket: SupportTicketItem,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.12f),
                        Color.White.copy(alpha = 0.04f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.35f),
                        OrangePrimary.copy(alpha = 0.25f),
                        Color.White.copy(alpha = 0.08f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = ticket.id,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = OrangePrimary
                )
                GlassStatusBadge(status = ticket.status)
            }

            Text(
                text = ticket.subject,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = ticket.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Text(
                    text = ticket.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = OrangeSecondary
                )
            }
        }
    }
}

@Composable
fun GlassStatusBadge(status: String) {
    val isResolved = status == "Resolved"
    val containerColor = if (isResolved) OrangePrimary.copy(alpha = 0.25f) else OrangeSecondary.copy(alpha = 0.25f)
    val textColor = if (isResolved) Color.White else OrangeSecondary

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(containerColor)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}
