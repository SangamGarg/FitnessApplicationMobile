package com.muscleplay.app.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private val OrangePrimary = Color(0xFFFF5722)
private val OrangeSecondary = Color(0xFFFF9800)
private val DarkBackground = Color(0xFF0F0F17)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpSupportScreen(
    onBack: () -> Unit,
    onNavigateToRaiseTicket: () -> Unit = {},
    onNavigateToMyTickets: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFaq by remember { mutableStateOf<String?>(null) }
    var showChatbotDialog by remember { mutableStateOf(false) }
    var showEmailDialog by remember { mutableStateOf(false) }
    var selectedPolicyTitle by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val faqs = listOf(
        Pair("How does AI pose tracking work?", "MusclePlay uses advanced on-device ML models to track your joint movements during workouts like squats and push-ups in real-time."),
        Pair("Why is camera permission required?", "Camera access is strictly used locally on your device for real-time rep counting and form evaluation. No video is ever recorded or uploaded."),
        Pair("How do I upgrade to Pro Membership?", "You can upgrade to Pro Membership anytime via your account settings or subscription portal to unlock advanced workout analytics and challenges."),
        Pair("How do I reset my streak?", "Streaks are updated daily when you complete your workouts. If you miss a day, you can use a streak freeze or restart your streak."),
        Pair("Are my progress photos secure?", "Yes! All progress gallery photos are securely stored locally on your device storage.")
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text("Help & Support", fontWeight = FontWeight.Bold, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search FAQs or help topics...", color = Color.White.copy(alpha = 0.4f)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = OrangePrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                        ),
                        singleLine = true
                    )
                }

                // Need Instant Help? Section with LazyRow Cards
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Need Instant Help?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = OrangeSecondary
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp)
                        ) {
                            item {
                                GlassInstantHelpCard(
                                    icon = Icons.AutoMirrored.Filled.Chat,
                                    title = "Chat with AI Bot",
                                    subtitle = "Instant 24/7 assistant",
                                    onClick = { showChatbotDialog = true }
                                )
                            }
                            item {
                                GlassInstantHelpCard(
                                    icon = Icons.Default.ConfirmationNumber,
                                    title = "View My Tickets",
                                    subtitle = "Track raised issues",
                                    onClick = onNavigateToMyTickets
                                )
                            }
                            item {
                                GlassInstantHelpCard(
                                    icon = Icons.Default.Email,
                                    title = "Email Us",
                                    subtitle = "support@muscleplay.com",
                                    onClick = { showEmailDialog = true }
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Frequently Asked Questions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                val filteredFaqs = faqs.filter { 
                    it.first.contains(searchQuery, ignoreCase = true) || 
                    it.second.contains(searchQuery, ignoreCase = true) 
                }

                items(filteredFaqs.size) { index ->
                    val faq = filteredFaqs[index]
                    GlassFaqItemCard(
                        question = faq.first,
                        answer = faq.second,
                        onClick = { selectedFaq = faq.first }
                    )
                }

                // Still Need Help? Section
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    GlassCardContainer {
                        Column(
                            modifier = Modifier.padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ConfirmationNumber,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = OrangePrimary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Still Need Help?",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Can't find a solution above? Submit a ticket and our support team will inspect your issue.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onNavigateToRaiseTicket,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Raise a Ticket", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }

                // Footer Section
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            TextButton(onClick = { selectedPolicyTitle = "About Us" }) {
                                Text("About Us", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                            }
                            TextButton(onClick = { selectedPolicyTitle = "Privacy Policy" }) {
                                Text("Privacy Policy", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            TextButton(onClick = { selectedPolicyTitle = "Terms & Conditions" }) {
                                Text("Terms & Conditions", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                            }
                            TextButton(onClick = { selectedPolicyTitle = "Refund & Cancellation Policy" }) {
                                Text("Refund & Cancellation Policy", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "App Version: 1.0.0",
                            style = MaterialTheme.typography.labelSmall,
                            color = OrangeSecondary.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }

            // Chatbot Dialog
            if (showChatbotDialog) {
                var botMessage by remember { mutableStateOf("") }
                AlertDialog(
                    onDismissRequest = { showChatbotDialog = false },
                    icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, tint = OrangePrimary) },
                    title = { Text("MusclePlay AI Bot") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Hello! I am your MusclePlay AI assistant. How can I help you today?")
                            OutlinedTextField(
                                value = botMessage,
                                onValueChange = { botMessage = it },
                                placeholder = { Text("Ask about workouts, rep counts, streaks...") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showChatbotDialog = false
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("AI Assistant: I'm analyzing your request!")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                        ) {
                            Text("Send")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showChatbotDialog = false }) {
                            Text("Close")
                        }
                    }
                )
            }

            // Email Dialog
            if (showEmailDialog) {
                AlertDialog(
                    onDismissRequest = { showEmailDialog = false },
                    icon = { Icon(Icons.Default.Email, contentDescription = null, tint = OrangePrimary) },
                    title = { Text("Email Support") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Send an email directly to support@muscleplay.com")
                            Text("Expected response time: Under 12 hours", style = MaterialTheme.typography.bodySmall, color = OrangeSecondary)
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showEmailDialog = false
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Opening email client...")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                        ) {
                            Text("Open Email Client")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showEmailDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // FAQ Details Dialog
            selectedFaq?.let { question ->
                val faqPair = faqs.find { it.first == question }
                if (faqPair != null) {
                    AlertDialog(
                        onDismissRequest = { selectedFaq = null },
                        title = { Text(faqPair.first) },
                        text = { Text(faqPair.second) },
                        confirmButton = {
                            Button(onClick = { selectedFaq = null }, colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)) {
                                Text("Got it")
                            }
                        }
                    )
                }
            }

            // Policy Dialog
            selectedPolicyTitle?.let { policy ->
                AlertDialog(
                    onDismissRequest = { selectedPolicyTitle = null },
                    title = { Text(policy) },
                    text = {
                        Text("MusclePlay $policy - Version 1.0.0\n\nAll fitness features, pose tracking data, and user progress logs strictly adhere to privacy & data protection guidelines.")
                    },
                    confirmButton = {
                        Button(onClick = { selectedPolicyTitle = null }, colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)) {
                            Text("Close")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun GlassInstantHelpCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    GlassCardContainer(
        modifier = Modifier
            .width(160.dp)
            .height(130.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(OrangePrimary.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = OrangePrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.White
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
fun GlassFaqItemCard(
    question: String,
    answer: String,
    onClick: () -> Unit
) {
    GlassCardContainer(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(OrangePrimary.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                    contentDescription = null,
                    tint = OrangePrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = question,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = answer,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f),
                    maxLines = 1
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun GlassCardContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
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
    ) {
        content()
    }
}
