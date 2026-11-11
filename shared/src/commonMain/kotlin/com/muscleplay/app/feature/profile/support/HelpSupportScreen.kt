package com.muscleplay.app.feature.profile.support

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muscleplay.app.core.network.dto.SupportFaqCategoryDto
import com.muscleplay.app.core.network.dto.SupportFaqDto

private val OrangePrimary = Color(0xFFFF5722)
private val OrangeSecondary = Color(0xFFFF9800)
private val DarkBackground = Color(0xFF0F0F17)
private val TextMuted = Color(0xFF9E9B9E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpSupportScreen(
    supportRepository: com.muscleplay.app.feature.profile.support.data.SupportRepository? = null,
    onBack: () -> Unit,
    onNavigateToAiBot: () -> Unit = {},
    onNavigateToEmailSupport: () -> Unit = {},
    onNavigateToRaiseTicket: () -> Unit = {},
    onNavigateToMyTickets: () -> Unit = {},
) {
    val viewModel = remember(supportRepository) { HelpSupportViewModel(supportRepository) }
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
                    title = {
                        Text(
                            "Help & Support Desk",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    },
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
                        placeholder = {
                            Text(
                                text = "Search FAQs, MP Coins, AI tracking...",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 13.sp,
                                lineHeight = 16.sp
                            )
                        },
                        textStyle = TextStyle(
                            fontSize = 13.sp,
                            lineHeight = 16.sp,
                            color = Color.White
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = OrangePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { viewModel.updateSearchQuery("") },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = Color.White.copy(alpha = 0.5f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp),
                        shape = RoundedCornerShape(14.dp),
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
                            style = MaterialTheme.typography.titleSmall,
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

                // Categories Section (if loaded)
                val homeData = (uiState.homeState as? SupportHomeUiState.Success)?.homeData
                val categories = homeData?.categories ?: emptyList()
                if (categories.isNotEmpty()) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "FAQ Categories",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(horizontal = 2.dp)
                            ) {
                                items(categories, key = { it.id }) { category ->
                                    val isSelected = uiState.selectedCategoryKey == category.categoryKey
                                    GlassCategoryCard(
                                        category = category,
                                        isSelected = isSelected,
                                        onClick = { viewModel.selectCategory(category.categoryKey) }
                                    )
                                }
                            }
                        }
                    }
                }

                // FAQ Header
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (!uiState.selectedCategoryKey.isNullOrBlank()) "Category FAQs" else "Frequently Asked Questions",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Render FAQs or Shimmers
                val isSearching = uiState.searchQuery.isNotBlank() || uiState.selectedCategoryKey != null
                if (isSearching) {
                    when (val searchState = uiState.searchFaqState) {
                        is FaqSearchUiState.Loading -> {
                            items(3) {
                                ShimmerFaqItem()
                            }
                        }

                        is FaqSearchUiState.Success -> {
                            val faqs = searchState.faqs
                            if (faqs.isEmpty()) {
                                item {
                                    Text(
                                        text = "No matching FAQs found.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextMuted,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                                    )
                                }
                            } else {
                                items(faqs, key = { it.id }) { faqDto ->
                                    val faqItem = SupportFaqAdapter.toFaqItem(faqDto)
                                    val isExpanded = uiState.expandedFaqId == faqItem.id.toLong()
                                    val currentVote = uiState.faqHelpfulVotes[faqItem.id.toLong()]

                                    GlassFaqItemCard(
                                        faq = faqItem,
                                        isExpanded = isExpanded,
                                        currentVote = currentVote,
                                        onToggle = { viewModel.toggleFaq(faqItem.id.toLong()) },
                                        onVote = { isHelpful -> viewModel.voteHelpful(faqItem.id.toLong(), isHelpful) },
                                        onActionClick = { target -> viewModel.openPolicyDialog(target) }
                                    )
                                }
                            }
                        }

                        is FaqSearchUiState.Error -> {
                            item {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                                ) {
                                    Text(
                                        text = "Something went wrong. Please try again.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextMuted,
                                        textAlign = TextAlign.Center
                                    )
                                    OutlinedButton(
                                        onClick = { viewModel.performFaqSearch() },
                                        shape = RoundedCornerShape(14.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, OrangePrimary),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangePrimary)
                                    ) {
                                        Text("Retry Search", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        else -> {}
                    }
                } else {
                    // Home Popular FAQs
                    when (val homeState = uiState.homeState) {
                        is SupportHomeUiState.Loading -> {
                            items(4) {
                                ShimmerFaqItem()
                            }
                        }

                        is SupportHomeUiState.Success -> {
                            val popularFaqs = homeState.homeData.popularFaqs
                            if (popularFaqs.isEmpty()) {
                                items(defaultFaqs, key = { it.id }) { faq ->
                                    val isExpanded = uiState.expandedFaqId == faq.id.toLong()
                                    val currentVote = uiState.faqHelpfulVotes[faq.id.toLong()]

                                    GlassFaqItemCard(
                                        faq = faq,
                                        isExpanded = isExpanded,
                                        currentVote = currentVote,
                                        onToggle = { viewModel.toggleFaq(faq.id.toLong()) },
                                        onVote = { isHelpful -> viewModel.voteHelpful(faq.id.toLong(), isHelpful) },
                                        onActionClick = { target -> viewModel.openPolicyDialog(target) }
                                    )
                                }
                            } else {
                                items(popularFaqs, key = { it.id }) { faqDto ->
                                    val faqItem = SupportFaqAdapter.toFaqItem(faqDto)
                                    val isExpanded = uiState.expandedFaqId == faqItem.id.toLong()
                                    val currentVote = uiState.faqHelpfulVotes[faqItem.id.toLong()]

                                    GlassFaqItemCard(
                                        faq = faqItem,
                                        isExpanded = isExpanded,
                                        currentVote = currentVote,
                                        onToggle = { viewModel.toggleFaq(faqItem.id.toLong()) },
                                        onVote = { isHelpful -> viewModel.voteHelpful(faqItem.id.toLong(), isHelpful) },
                                        onActionClick = { target -> viewModel.openPolicyDialog(target) }
                                    )
                                }
                            }
                        }

                        is SupportHomeUiState.Error -> {
                            item {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                                ) {
                                    Text(
                                        text = "Something went wrong. Please try again.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextMuted,
                                        textAlign = TextAlign.Center
                                    )
                                    OutlinedButton(
                                        onClick = { viewModel.loadSupportHome() },
                                        shape = RoundedCornerShape(14.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, OrangePrimary),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangePrimary)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Retry Loading", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
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
                                Icon(
                                    Icons.Default.ConfirmationNumber,
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp),
                                    tint = OrangeSecondary
                                )
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
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Raise a New Ticket",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
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
                        HorizontalDivider(
                            color = Color.White.copy(alpha = 0.1f),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            TextButton(onClick = { viewModel.openPolicyDialog("Terms of Service") }) {
                                Text(
                                    "Terms of Service",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                            TextButton(onClick = { viewModel.openPolicyDialog("Privacy Policy") }) {
                                Text(
                                    "Privacy Policy",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                            TextButton(onClick = { viewModel.openPolicyDialog("About Us") }) {
                                Text(
                                    "About Us",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))

                        val appVersionText = homeData?.let {
                            "${it.footer ?: "© 2026 MusclePlay App. All Rights Reserved."} • v${it.appVersion ?: "1.0.0"} (Build ${it.appBuild ?: "104"})"
                        } ?: "MusclePlay App v1.0.0 (Build 104) • All Rights Reserved"

                        Text(
                            text = appVersionText,
                            style = MaterialTheme.typography.labelSmall,
                            color = OrangeSecondary.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Glass Policy Dialog Modal
        if (uiState.dialogState !is PolicyDialogUiState.Idle) {
            GlassPolicyDialog(
                dialogState = uiState.dialogState,
                onDismiss = { viewModel.closePolicyDialog() },
                onRetry = { title -> viewModel.openPolicyDialog(title) }
            )
        }
    }
}

object SupportFaqAdapter {
    fun toFaqItem(dto: SupportFaqDto): FaqItem {
        return FaqItem(
            id = dto.id.toInt(),
            category = dto.categoryName.ifBlank { dto.categoryKey },
            question = dto.question,
            answer = dto.answer,
            actionText = null,
            policyTarget = null
        )
    }
}

@Composable
private fun GlassCategoryCard(
    category: SupportFaqCategoryDto,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) OrangePrimary.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.06f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) OrangePrimary else Color.White.copy(alpha = 0.15f)
        ),
        modifier = Modifier.width(150.dp).height(100.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) OrangePrimary else OrangePrimary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = mapCategoryIcon(category.iconKey),
                    contentDescription = null,
                    tint = if (isSelected) Color.White else OrangeSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                )
                category.description?.let { desc ->
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.6f),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

private fun mapCategoryIcon(iconKey: String?): ImageVector {
    return when (iconKey?.uppercase()?.trim()) {
        "ACCOUNT_LOGIN", "LOGIN" -> Icons.Default.AccountCircle
        "PRIVACY", "SECURITY" -> Icons.Default.Security
        "COINS", "WALLET" -> Icons.Default.MonetizationOn
        "WORKOUT", "FITNESS" -> Icons.Default.FitnessCenter
        else -> Icons.AutoMirrored.Filled.HelpOutline
    }
}

@Composable
private fun ShimmerFaqItem() {
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

    GlassCardContainer(modifier = Modifier.fillMaxWidth().height(64.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = alpha))
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White.copy(alpha = alpha))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(10.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White.copy(alpha = alpha))
                )
            }
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
    onClick: () -> Unit,
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
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = OrangeSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Surface(
                    color = badgeColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f),
                    maxLines = 1
                )
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
    onActionClick: (String) -> Unit,
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
                        Icon(
                            Icons.AutoMirrored.Filled.HelpOutline,
                            contentDescription = null,
                            tint = OrangeSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Surface(
                            color = OrangeSecondary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                faq.category,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = OrangeSecondary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = faq.question,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        action,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = OrangeSecondary
                                    )
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = OrangeSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
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
                        Text(
                            "Was this answer helpful?",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                            IconButton(
                                onClick = { onVote(true) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (currentVote == true) Color(0xFF4CAF50).copy(
                                            alpha = 0.3f
                                        ) else Color.White.copy(alpha = 0.08f)
                                    )
                            ) {
                                Icon(
                                    Icons.Default.ThumbUp,
                                    contentDescription = "Helpful",
                                    tint = if (currentVote == true) Color(0xFF4CAF50) else Color.White.copy(
                                        alpha = 0.6f
                                    ),
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            IconButton(
                                onClick = { onVote(false) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (currentVote == false) Color(0xFFFF5252).copy(
                                            alpha = 0.3f
                                        ) else Color.White.copy(alpha = 0.08f)
                                    )
                            ) {
                                Icon(
                                    Icons.Default.ThumbDown,
                                    contentDescription = "Not Helpful",
                                    tint = if (currentVote == false) Color(0xFFFF5252) else Color.White.copy(
                                        alpha = 0.6f
                                    ),
                                    modifier = Modifier.size(16.dp)
                                )
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
    dialogState: PolicyDialogUiState,
    onDismiss: () -> Unit,
    onRetry: (String) -> Unit,
) {
    val scrollState = rememberScrollState()

    val title = when (dialogState) {
        is PolicyDialogUiState.Loading -> dialogState.title
        is PolicyDialogUiState.Success -> dialogState.title
        is PolicyDialogUiState.Error -> dialogState.title
        else -> "Policy Document"
    }

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
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White.copy(alpha = 0.7f)
                    )
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
                Text("MusclePlay $title", fontWeight = FontWeight.Bold, color = OrangeSecondary)

                when (dialogState) {
                    is PolicyDialogUiState.Loading -> {
                        val infiniteTransition = rememberInfiniteTransition(label = "dialogShimmer")
                        val alpha by infiniteTransition.animateFloat(
                            initialValue = 0.2f,
                            targetValue = 0.6f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(800, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "alpha"
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            repeat(5) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(if (it % 2 == 0) 0.9f else 0.75f)
                                        .height(14.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color.White.copy(alpha = alpha))
                                )
                            }
                        }
                    }

                    is PolicyDialogUiState.Success -> {
                        Text(
                            text = dialogState.content,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f),
                            lineHeight = 18.sp
                        )
                    }

                    is PolicyDialogUiState.Error -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                        ) {
                            Text(
                                text = "Something went wrong. Please try again.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )
                            OutlinedButton(
                                onClick = { onRetry(title) },
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, OrangePrimary),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangePrimary)
                            ) {
                                Text("Retry", fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
                Text("Got It", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun GlassCardContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
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
