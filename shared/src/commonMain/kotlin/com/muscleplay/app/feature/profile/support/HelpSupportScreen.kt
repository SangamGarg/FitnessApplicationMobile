package com.muscleplay.app.feature.profile.support

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

private val OrangePrimary = Color(0xFFFF5722)
private val OrangeSecondary = Color(0xFFFF9800)
private val DarkBackground = Color(0xFF0F0F17)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpSupportScreen(
    onBack: () -> Unit,
    onNavigateToAiBot: () -> Unit = {},
    onNavigateToEmailSupport: () -> Unit = {},
    onNavigateToRaiseTicket: () -> Unit = {},
    onNavigateToMyTickets: () -> Unit = {},
    viewModel: HelpSupportViewModel = viewModel { HelpSupportViewModel() }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

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
                    title = { Text("Help & Support Desk", fontWeight = FontWeight.ExtraBold, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
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
                // Search Input
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        placeholder = { Text("Search FAQs, MP Coins, AI tracking...", color = Color.White.copy(alpha = 0.4f)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = OrangePrimary) },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.White.copy(alpha = 0.5f))
                                }
                            }
                        },
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

                // Instant Help Channels Section
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Instant Contact Channels",
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
                                    icon = Icons.Default.SmartToy,
                                    badgeText = "24/7 Instant",
                                    badgeColor = Color(0xFF4CAF50),
                                    title = "AI Support Coach",
                                    subtitle = "Instant automated answers",
                                    onClick = onNavigateToAiBot
                                )
                            }
                            item {
                                GlassInstantHelpCard(
                                    icon = Icons.Default.Mail,
                                    badgeText = "Replies ~2h",
                                    badgeColor = OrangeSecondary,
                                    title = "Email Support",
                                    subtitle = "File detailed tickets & logs",
                                    onClick = onNavigateToEmailSupport
                                )
                            }
                            item {
                                GlassInstantHelpCard(
                                    icon = Icons.Default.ConfirmationNumber,
                                    badgeText = "My Issues",
                                    badgeColor = Color(0xFF2196F3),
                                    title = "Ticket History",
                                    subtitle = "View raised support tickets",
                                    onClick = onNavigateToMyTickets
                                )
                            }
                        }
                    }
                }

                // FAQ Header
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Frequently Asked Questions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // FAQ List items with Expandable Animations & Feedback
                val filteredFaqs = uiState.faqs.filter {
                    it.question.contains(uiState.searchQuery, ignoreCase = true) ||
                    it.answer.contains(uiState.searchQuery, ignoreCase = true) ||
                    it.category.contains(uiState.searchQuery, ignoreCase = true)
                }

                items(filteredFaqs, key = { it.id }) { faq ->
                    val isExpanded = uiState.expandedFaqId == faq.id
                    val currentVote = uiState.faqHelpfulVotes[faq.id]

                    GlassFaqItemCard(
                        faq = faq,
                        isExpanded = isExpanded,
                        currentVote = currentVote,
                        onToggle = { viewModel.toggleFaq(faq.id) },
                        onVote = { isHelpful -> viewModel.voteHelpful(faq.id, isHelpful) },
                        onActionClick = { target -> viewModel.openPolicy(target) }
                    )
                }

                // Still Need Staff Support Section
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    GlassCardContainer {
                        Column(
                            modifier = Modifier.padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(OrangePrimary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.ConfirmationNumber, contentDescription = null, modifier = Modifier.size(28.dp), tint = OrangeSecondary)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Still Need Human Assistance?",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Submit a support ticket and our engineering team will inspect your account or logs.",
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
                                Text("Raise a New Ticket", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }

                // Policy Footer Links Section
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            TextButton(onClick = { viewModel.openPolicy("Terms of Service") }) {
                                Text("Terms of Service", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                            }
                            TextButton(onClick = { viewModel.openPolicy("Privacy Policy") }) {
                                Text("Privacy Policy", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            TextButton(onClick = { viewModel.openPolicy("MP Coins & Cashout Policy") }) {
                                Text("MP Coins & Cashout Policy", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                            }
                            TextButton(onClick = { viewModel.openPolicy("Fair Play & Anti-Cheat Rules") }) {
                                Text("Fair Play & Anti-Cheat", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "MusclePlay App v1.0.0 (Build 104) • All Rights Reserved",
                            style = MaterialTheme.typography.labelSmall,
                            color = OrangeSecondary.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Glass Policy Dialog Modal
        uiState.selectedPolicyTitle?.let { policyTitle ->
            GlassPolicyDialog(
                policyTitle = policyTitle,
                onDismiss = { viewModel.closePolicy() }
            )
        }
    }
}

@Composable
private fun GlassInstantHelpCard(
    icon: ImageVector,
    badgeText: String,
    badgeColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    GlassCardContainer(
        modifier = Modifier
            .width(170.dp)
            .height(140.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(OrangePrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = OrangeSecondary, modifier = Modifier.size(20.dp))
                }

                Surface(
                    color = badgeColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(badgeText, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = badgeColor, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f), maxLines = 1)
            }
        }
    }
}

@Composable
private fun GlassFaqItemCard(
    faq: FaqItem,
    isExpanded: Boolean,
    currentVote: Boolean?,
    onToggle: () -> Unit,
    onVote: (Boolean) -> Unit,
    onActionClick: (String) -> Unit
) {
    val rotationState by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300)
    )

    GlassCardContainer(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(OrangePrimary.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = null, tint = OrangeSecondary, modifier = Modifier.size(20.dp))
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Surface(
                            color = OrangeSecondary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(faq.category, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = OrangeSecondary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                        Text(text = faq.question, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.rotate(rotationState)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = faq.answer,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                        lineHeight = 18.sp
                    )

                    faq.actionText?.let { action ->
                        faq.policyTarget?.let { target ->
                            TextButton(
                                onClick = { onActionClick(target) },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(action, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = OrangeSecondary)
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = OrangeSecondary, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                    // Helpful Feedback Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Was this answer helpful?", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))

                        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                            IconButton(
                                onClick = { onVote(true) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (currentVote == true) Color(0xFF4CAF50).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.08f))
                            ) {
                                Icon(Icons.Default.ThumbUp, contentDescription = "Helpful", tint = if (currentVote == true) Color(0xFF4CAF50) else Color.White.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                            }

                            IconButton(
                                onClick = { onVote(false) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (currentVote == false) Color(0xFFFF5252).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.08f))
                            ) {
                                Icon(Icons.Default.ThumbDown, contentDescription = "Not Helpful", tint = if (currentVote == false) Color(0xFFFF5252) else Color.White.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GlassPolicyDialog(
    policyTitle: String,
    onDismiss: () -> Unit
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
                Text(policyTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Color.White)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White.copy(alpha = 0.7f))
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("MusclePlay $policyTitle — Document v1.0.0", fontWeight = FontWeight.Bold, color = OrangeSecondary)
                Text(
                    text = when (policyTitle) {
                        "MP Coins & Cashout Policy" -> "1. Minimum Redemption Threshold: Users can cash out once balance reaches 1,000 MP Coins (1,000 MP = ₹10.00).\n2. Payout Velocity: Maximum 1 redemption request per 24 hours.\n3. Processing Time: Payouts via UPI, Bank, PayPal, or Stripe require 1-2 business days.\n4. Anti-Fraud Audit: Coins generated via fake referral self-invites, emulators, or manipulated pose metrics are subject to freezing during compliance audit."
                        "Fair Play & Anti-Cheat Rules" -> "1. Real-Time Rep Verification: MusclePlay utilizes on-device pose detection algorithms. Artificial video feeds or static image spoofing will invalidate workout records.\n2. Referral Integrity: Self-referrals or device ID spoofing will result in referral reward revocation and coin forfeiture."
                        "Privacy Policy" -> "1. Camera Data: Camera access is strictly processed locally on your device hardware for pose tracking. No raw video feed is recorded or stored.\n2. Personal Data: Credentials and profile metrics are stored in app-private storage and encrypted in transit."
                        else -> "MusclePlay $policyTitle outlines standard user responsibilities, terms of service, acceptable fitness app usage, and customer support standards."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                    lineHeight = 18.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
            ) {
                Text("Got It", fontWeight = FontWeight.Bold)
            }
        }
    )
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
