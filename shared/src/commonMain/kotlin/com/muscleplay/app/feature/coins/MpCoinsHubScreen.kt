package com.muscleplay.app.feature.coins

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.muscleplay.app.feature.coins.model.*

private val OrangePrimary = Color(0xFFFF5722)
private val OrangeSecondary = Color(0xFFFF9800)
private val DarkBackground = Color(0xFF0F0F17)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MpCoinsHubScreen(
    onBack: () -> Unit,
    viewModel: MpCoinsHubViewModel = viewModel { MpCoinsHubViewModel() },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current

    var showPolicyBottomSheet by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.purchaseSuccessMessage) {
        uiState.purchaseSuccessMessage?.let {
            snackbarMessage = it
            viewModel.clearPurchaseMessage()
        }
    }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(DarkBackground)
    ) {
        Scaffold(containerColor = Color.Transparent, snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 88.dp, start = 16.dp, end = 16.dp)
            ) { data ->
                Snackbar(
                    snackbarData = data,
                    shape = RoundedCornerShape(16.dp),
                    containerColor = Color(0xFF181824),
                    contentColor = Color.White,
                    actionColor = OrangeSecondary,
                    dismissActionContentColor = Color.White.copy(alpha = 0.7f)
                )
            }
        }, topBar = {
            TopAppBar(
                title = {
                Text(
                    "MP Coins", fontWeight = FontWeight.ExtraBold, color = Color.White
                )
            }, navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            }, actions = {
                IconButton(onClick = { showPolicyBottomSheet = true }) {
                    Icon(
                        Icons.AutoMirrored.Filled.HelpOutline,
                        contentDescription = "Rules & Economy Policy",
                        tint = OrangeSecondary
                    )
                }
            }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }) { innerPadding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // Balance Hero Card with Status Badge & Progress Indicator
                GlassCoinCard {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(
                                8.dp, Alignment.CenterHorizontally
                            ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "CURRENT MP COINS BALANCE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OrangeSecondary,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                            }
                            Surface(
                                color = if (uiState.availableCoins >= 1000) Color(0xFF4CAF50).copy(
                                    alpha = 0.2f
                                ) else OrangePrimary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (uiState.availableCoins >= 1000) Color(0xFF4CAF50) else OrangePrimary
                                )
                            ) {
                                Text(
                                    text = if (uiState.availableCoins >= 1000) "Eligible to Cashout 🎉" else "1,000 MP Min for Cashout",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (uiState.availableCoins >= 1000) Color(0xFF4CAF50) else OrangeSecondary,
                                    modifier = Modifier.padding(
                                        horizontal = 10.dp, vertical = 6.dp
                                    )
                                )
                            }
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(32.dp).clip(CircleShape)
                                    .background(OrangeSecondary.copy(alpha = 0.25f))
                                    .border(1.dp, OrangeSecondary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.MonetizationOn,
                                    contentDescription = null,
                                    tint = OrangeSecondary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Text(
                                text = "${uiState.availableCoins} MP",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }

                        // Linear Progress Indicator towards 1,000 MP Cashout Threshold
                        val thresholdProgress =
                            (uiState.availableCoins.toFloat() / 1000f).coerceIn(0f, 1f)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Cashout Threshold Progress (${(thresholdProgress * 100).toInt()}%)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "₹${(uiState.availableCoins / 100.0)} Value",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = OrangeSecondary
                                )
                            }
                            LinearProgressIndicator(
                                progress = { thresholdProgress },
                                modifier = Modifier.fillMaxWidth().height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (uiState.availableCoins >= 1000) Color(0xFF4CAF50) else OrangePrimary,
                                trackColor = Color.White.copy(alpha = 0.1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Four-Way Glossy & Glassy Segmented Tab Controller
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.12f), Color.White.copy(alpha = 0.04f)
                            )
                        )
                    ).border(
                        width = 1.dp, brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.35f),
                                OrangePrimary.copy(alpha = 0.25f),
                                Color.White.copy(alpha = 0.08f)
                            )
                        ), shape = RoundedCornerShape(20.dp)
                    ).padding(5.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        CoinsHubTab.entries.forEach { tab ->
                            val isSelected = uiState.activeTab == tab
                            Box(
                                modifier = Modifier.weight(1f).clip(RoundedCornerShape(15.dp))
                                    .background(
                                        if (isSelected) {
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    OrangePrimary, Color(0xFFFF3D00)
                                                )
                                            )
                                        } else {
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    Color.Transparent, Color.Transparent
                                                )
                                            )
                                        }
                                    ).then(
                                        if (isSelected) {
                                            Modifier.border(
                                                width = 1.dp, brush = Brush.linearGradient(
                                                    colors = listOf(
                                                        Color.White.copy(alpha = 0.6f),
                                                        OrangeSecondary
                                                    )
                                                ), shape = RoundedCornerShape(15.dp)
                                            )
                                        } else Modifier
                                    ).clickable { viewModel.selectTab(tab) }
                                    .padding(vertical = 11.dp), contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tab.label,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Dynamic Views based on Active Tab
                when (uiState.activeTab) {
                    CoinsHubTab.BUY -> {
                        // TAB 1: BUY COIN STORE
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(bottom = 90.dp)
                        ) {
                            // Promo Banner
                            item {
                                Surface(
                                    color = OrangePrimary.copy(alpha = 0.18f),
                                    shape = RoundedCornerShape(18.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp, OrangePrimary.copy(alpha = 0.4f)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Stars,
                                            contentDescription = null,
                                            tint = OrangeSecondary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text(
                                            text = "🔥 Spend MP Coins in paid challenges to double your reward pool!",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            // Coin Packs List
                            items(uiState.coinPacks, key = { it.id }) { pack ->
                                GlassCoinCard {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier.size(46.dp).clip(CircleShape)
                                                    .background(OrangePrimary.copy(alpha = 0.25f))
                                                    .border(
                                                        1.dp,
                                                        OrangePrimary.copy(alpha = 0.5f),
                                                        CircleShape
                                                    ), contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    Icons.Default.Savings,
                                                    contentDescription = null,
                                                    tint = OrangeSecondary,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }

                                            Column(
                                                modifier = Modifier.weight(1f),
                                                verticalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                pack.tagText?.let { tag ->
                                                    Surface(
                                                        color = OrangeSecondary,
                                                        shape = RoundedCornerShape(6.dp)
                                                    ) {
                                                        Text(
                                                            text = tag,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color.White,
                                                            maxLines = 1,
                                                            softWrap = false,
                                                            modifier = Modifier.padding(
                                                                horizontal = 6.dp,
                                                                vertical = 2.dp
                                                            )
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = "${pack.coinsAmount} MP Coins",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color.White,
                                                    maxLines = 1
                                                )
                                                Text(
                                                    text = if (pack.bonusCoins > 0) "Includes +${pack.bonusCoins} Bonus Coins" else "No Bonus",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.White.copy(alpha = 0.6f),
                                                    maxLines = 1
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Button(
                                            onClick = { viewModel.buyCoinPack(pack) },
                                            shape = RoundedCornerShape(14.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                                            contentPadding = PaddingValues(
                                                horizontal = 12.dp, vertical = 8.dp
                                            )
                                        ) {
                                            Text(
                                                pack.priceText,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    CoinsHubTab.REFERRAL -> {
                        // TAB 2: SHARE & EARN (REFERRAL VIRAL GROWTH)
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 90.dp)
                        ) {
                            // Referral Hero Card
                            item {
                                GlassCoinCard {
                                    Column(
                                        modifier = Modifier.padding(15.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier.size(48.dp).clip(CircleShape)
                                                    .background(OrangePrimary.copy(alpha = 0.25f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    Icons.Default.CardGiftcard,
                                                    contentDescription = null,
                                                    tint = OrangeSecondary,
                                                    modifier = Modifier.size(28.dp)
                                                )
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    "Invite Friends, Earn 150 MP Coins each! 🎉",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color.White
                                                )
                                                Text(
                                                    "Your friend gets 100 MP starter coins, and you receive 150 MP Coins when they complete their first challenge.",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.White.copy(alpha = 0.8f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Referral Code & Share Actions
                            item {
                                GlassCoinCard {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        verticalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        Text(
                                            "Your Unique Referral Code",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = OrangeSecondary,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth()
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(Color.White.copy(alpha = 0.08f)).border(
                                                    1.dp,
                                                    OrangePrimary.copy(alpha = 0.4f),
                                                    RoundedCornerShape(16.dp)
                                                ).padding(horizontal = 16.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                uiState.userReferralCode,
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White,
                                                letterSpacing = 2.sp
                                            )

                                            Button(
                                                onClick = {
                                                    clipboardManager.setText(AnnotatedString(uiState.userReferralCode))
                                                    snackbarMessage =
                                                        "Copied referral code ${uiState.userReferralCode}!"
                                                },
                                                shape = RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.ContentCopy,
                                                        contentDescription = "Copy",
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Text("Copy", fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }

                                        // Share Invite Link Action
                                        Button(
                                            onClick = {
                                                snackbarMessage = "Copied share link to clipboard!"
                                                clipboardManager.setText(AnnotatedString(uiState.referralShareText))
                                            },
                                            modifier = Modifier.fillMaxWidth().height(48.dp),
                                            shape = RoundedCornerShape(16.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(Icons.Default.Share, contentDescription = null)
                                                Text(
                                                    "Share Invite Link",
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Milestone Stats Header
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "Friends Invited: ${uiState.totalFriendsInvited}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        "Earned: ${uiState.referralCoinsEarned} MP",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = OrangeSecondary
                                    )
                                }
                            }

                            // Referral Activity List
                            items(uiState.referrals, key = { it.id }) { ref ->
                                GlassCoinCard {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(
                                                ref.friendName,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                ref.statusText,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White.copy(alpha = 0.7f)
                                            )
                                            Text(
                                                ref.dateIso,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White.copy(alpha = 0.5f)
                                            )
                                        }

                                        Surface(
                                            color = if (ref.isCompleted) Color(0xFF4CAF50).copy(
                                                alpha = 0.2f
                                            ) else OrangeSecondary.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = if (ref.isCompleted) "+${ref.rewardCoins} MP ✓" else "Pending",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (ref.isCompleted) Color(0xFF4CAF50) else OrangeSecondary,
                                                modifier = Modifier.padding(
                                                    horizontal = 8.dp, vertical = 4.dp
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    CoinsHubTab.CASHOUT -> {
                        // TAB 3: CASHOUT FLOW
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 90.dp)
                        ) {
                            // Amount Selection & Financial Breakdown
                            item {
                                GlassCoinCard {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        verticalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        Text(
                                            text = "Select Coins to Convert",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            listOf(1000, 2000, uiState.availableCoins).distinct()
                                                .forEach { amt ->
                                                    FilterChip(
                                                        selected = uiState.selectedCoins == amt,
                                                        onClick = {
                                                            viewModel.updateSelectedCoins(
                                                                amt
                                                            )
                                                        },
                                                        label = { Text(if (amt == uiState.availableCoins) "Max ($amt)" else "$amt MP") },
                                                        colors = FilterChipDefaults.filterChipColors(
                                                            selectedContainerColor = OrangePrimary,
                                                            selectedLabelColor = Color.White,
                                                            containerColor = Color.White.copy(alpha = 0.08f),
                                                            labelColor = Color.White.copy(alpha = 0.8f)
                                                        ),
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                        }

                                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                                        // Financial Breakdown
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            BreakdownRow(
                                                "Gross Conversion Value",
                                                "₹${uiState.grossAmountRupees}"
                                            )
                                            BreakdownRow("Processing Fee", "₹0.00 (Free)")
                                            BreakdownRow(
                                                "Estimated Tax / TDS", "₹${uiState.taxAmountRupees}"
                                            )
                                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    "Net Payout Amount",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                                Text(
                                                    "₹${uiState.netAmountRupees}",
                                                    style = MaterialTheme.typography.headlineSmall,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = OrangeSecondary
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Payout Channel Selector & Dynamic Form
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text(
                                        text = "Choose Payout Destination",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )

                                    // Channel Pills (Scrollable LazyRow to prevent squishing)
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        contentPadding = PaddingValues(horizontal = 2.dp)
                                    ) {
                                        items(PayoutChannel.entries) { channel ->
                                            FilterChip(
                                                selected = uiState.selectedChannel == channel,
                                                onClick = { viewModel.selectChannel(channel) },
                                                label = {
                                                    Text(
                                                        channel.label,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = OrangeSecondary,
                                                    selectedLabelColor = Color.White,
                                                    containerColor = Color.White.copy(alpha = 0.08f),
                                                    labelColor = Color.White.copy(alpha = 0.8f)
                                                )
                                            )
                                        }
                                    }

                                    // Dynamic Channel Form Container
                                    GlassCoinCard {
                                        Column(
                                            modifier = Modifier.padding(18.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            when (uiState.selectedChannel) {
                                                PayoutChannel.UPI -> {
                                                    OutlinedTextField(
                                                        value = uiState.upiForm.fullName,
                                                        onValueChange = {
                                                            viewModel.updateUpiForm(
                                                                fullName = it
                                                            )
                                                        },
                                                        label = { Text("Account Legal Name") },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        colors = inputColors()
                                                    )
                                                    OutlinedTextField(
                                                        value = uiState.upiForm.upiId,
                                                        onValueChange = {
                                                            viewModel.updateUpiForm(
                                                                upiId = it
                                                            )
                                                        },
                                                        label = { Text("UPI ID / VPA (regex: name@upi)") },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        colors = inputColors()
                                                    )
                                                    OutlinedTextField(
                                                        value = uiState.upiForm.confirmUpiId,
                                                        onValueChange = {
                                                            viewModel.updateUpiForm(
                                                                confirmUpiId = it
                                                            )
                                                        },
                                                        label = { Text("Confirm UPI ID") },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        colors = inputColors()
                                                    )
                                                }

                                                PayoutChannel.BANK_TRANSFER -> {
                                                    OutlinedTextField(
                                                        value = uiState.bankForm.accountHolderName,
                                                        onValueChange = {
                                                            viewModel.updateBankForm(
                                                                accountHolderName = it
                                                            )
                                                        },
                                                        label = { Text("Bank Account Holder Name") },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        colors = inputColors()
                                                    )
                                                    OutlinedTextField(
                                                        value = uiState.bankForm.accountNumber,
                                                        onValueChange = {
                                                            viewModel.updateBankForm(
                                                                accountNumber = it
                                                            )
                                                        },
                                                        label = { Text("Bank Account Number") },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        colors = inputColors()
                                                    )
                                                    OutlinedTextField(
                                                        value = uiState.bankForm.confirmAccountNumber,
                                                        onValueChange = {
                                                            viewModel.updateBankForm(
                                                                confirmAccountNumber = it
                                                            )
                                                        },
                                                        label = { Text("Confirm Account Number") },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        colors = inputColors()
                                                    )
                                                    OutlinedTextField(
                                                        value = uiState.bankForm.ifscCode,
                                                        onValueChange = {
                                                            viewModel.updateBankForm(
                                                                ifscCode = it
                                                            )
                                                        },
                                                        label = { Text("IFSC Code (e.g. HDFC0001234)") },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        colors = inputColors()
                                                    )
                                                }

                                                PayoutChannel.PAYPAL -> {
                                                    OutlinedTextField(
                                                        value = uiState.paypalForm.fullName,
                                                        onValueChange = {
                                                            viewModel.updatePaypalForm(
                                                                fullName = it
                                                            )
                                                        },
                                                        label = { Text("Registered Legal Name") },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        colors = inputColors()
                                                    )
                                                    OutlinedTextField(
                                                        value = uiState.paypalForm.paypalEmail,
                                                        onValueChange = {
                                                            viewModel.updatePaypalForm(
                                                                paypalEmail = it
                                                            )
                                                        },
                                                        label = { Text("PayPal Registered Email") },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        colors = inputColors()
                                                    )
                                                    OutlinedTextField(
                                                        value = uiState.paypalForm.confirmEmail,
                                                        onValueChange = {
                                                            viewModel.updatePaypalForm(
                                                                confirmEmail = it
                                                            )
                                                        },
                                                        label = { Text("Confirm PayPal Email") },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        colors = inputColors()
                                                    )
                                                }

                                                PayoutChannel.STRIPE -> {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(
                                                                text = if (uiState.stripeForm.isConnected) "Stripe Connected ✓" else "Stripe Connect Direct Deposit",
                                                                style = MaterialTheme.typography.titleMedium,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color.White
                                                            )
                                                            Text(
                                                                text = if (uiState.stripeForm.isConnected) "Account: ${uiState.stripeForm.accountId}" else "Connect your bank account globally via Stripe",
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = Color.White.copy(alpha = 0.6f)
                                                            )
                                                        }

                                                        Spacer(modifier = Modifier.width(12.dp))

                                                        Button(
                                                            onClick = { viewModel.toggleStripeConnect() },
                                                            colors = ButtonDefaults.buttonColors(
                                                                containerColor = if (uiState.stripeForm.isConnected) Color(
                                                                    0xFF4CAF50
                                                                ) else OrangePrimary
                                                            )
                                                        ) {
                                                            Text(
                                                                text = if (uiState.stripeForm.isConnected) "Disconnect" else "Connect Stripe",
                                                                fontWeight = FontWeight.Bold,
                                                                maxLines = 1
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            // Save default checkbox
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.clickable {
                                                    viewModel.toggleSaveAsDefault(
                                                        !uiState.saveAsDefault
                                                    )
                                                }) {
                                                Checkbox(
                                                    checked = uiState.saveAsDefault,
                                                    onCheckedChange = {
                                                        viewModel.toggleSaveAsDefault(
                                                            it
                                                        )
                                                    },
                                                    colors = CheckboxDefaults.colors(checkedColor = OrangePrimary)
                                                )
                                                Text(
                                                    text = "Save as default payout method",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.White.copy(alpha = 0.8f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Convert To Cash Action Button
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { viewModel.triggerBiometricAuthentication() },
                                        enabled = uiState.isEligibleToRedeem,
                                        modifier = Modifier.fillMaxWidth().height(52.dp),
                                        shape = RoundedCornerShape(20.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = OrangePrimary,
                                            disabledContainerColor = Color.White.copy(alpha = 0.12f)
                                        )
                                    ) {
                                        Text(
                                            text = "Convert to Cash (₹${uiState.netAmountRupees}) 🔥",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = if (uiState.isEligibleToRedeem) Color.White else Color.White.copy(
                                                alpha = 0.4f
                                            )
                                        )
                                    }

                                    if (!uiState.isEligibleToRedeem) {
                                        Text(
                                            text = when {
                                                uiState.selectedCoins < 1000 -> "⚠️ Minimum redemption threshold is 1,000 MP Coins."
                                                uiState.selectedCoins > uiState.availableCoins -> "⚠️ Selected amount exceeds your available balance."
                                                !uiState.isFormValid -> "⚠️ Please enter valid payout details for ${uiState.selectedChannel.label}."
                                                else -> "⚠️ Unable to process cashout request."
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = OrangeSecondary,
                                            modifier = Modifier.padding(start = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    CoinsHubTab.HISTORY -> {
                        // TAB 4: REDEMPTION HISTORY TAB
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 90.dp)
                        ) {
                            items(uiState.transactions, key = { it.id }) { tx ->
                                TransactionRowCard(record = tx)
                            }
                        }
                    }
                }
            }
        }

        // Rules & Economy Policy Sheet
        if (showPolicyBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showPolicyBottomSheet = false },
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                containerColor = Color(0xFF181824)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "MP Coins Economy & Rules Policy 📜",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        PolicyBullet(
                            "Share & Earn (Referrals)",
                            "Referrers receive 150 MP Coins once invited friends complete 1st workout. Friends receive 100 MP welcome starter bonus. Max 10 referrals per week."
                        )
                        PolicyBullet(
                            "Gaming & Challenge Entry",
                            "Spend MP Coins to join fitness games/challenges (e.g. 50 MP entry to win up to 100 MP)."
                        )
                        PolicyBullet(
                            "Buying Coins",
                            "Purchased coin packs are credited instantly for challenge entries and premium features."
                        )
                        PolicyBullet(
                            "Redemption / Cashout Rules",
                            "Minimum cashout threshold is 1,000 MP Coins (1,000 MP = ₹10.00). Maximum 1 cashout per 24 hours. Timeline: 1-2 business days."
                        )
                        PolicyBullet(
                            "Anti-Fraud Audit",
                            "Coins generated via emulators, fake referral self-invites, or manipulated AI metrics will be frozen during compliance review."
                        )
                    }

                    Button(
                        onClick = { showPolicyBottomSheet = false },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                    ) {
                        Text("Understood", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Biometric Confirmation Dialog Modal
        if (uiState.isBiometricModalShowing) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissBiometricModal() },
                shape = RoundedCornerShape(28.dp),
                containerColor = Color(0xFF181824),
                icon = {
                    Box(
                        modifier = Modifier.size(56.dp).clip(CircleShape)
                            .background(OrangePrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Fingerprint,
                            contentDescription = "Biometric",
                            tint = OrangeSecondary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                },
                title = {
                    Text(
                        "Biometric Confirmation", fontWeight = FontWeight.Bold, color = Color.White
                    )
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Authenticate using Fingerprint or Face ID to authorize cashout of ${uiState.selectedCoins} MP Coins (₹${uiState.netAmountRupees}).",
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.confirmRedemption() },
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                    ) {
                        Text("Authorize Cashout", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissBiometricModal() }) {
                        Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                    }
                })
        }

        // Success Confirmation Modal
        if (uiState.isSuccessModalShowing) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissSuccessModal() },
                shape = RoundedCornerShape(28.dp),
                containerColor = Color(0xFF181824),
                icon = {
                    Box(
                        modifier = Modifier.size(56.dp).clip(CircleShape)
                            .background(Color(0xFF4CAF50).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                },
                title = {
                    Text(
                        "Redemption Requested! 🎉", fontWeight = FontWeight.Bold, color = Color.White
                    )
                },
                text = {
                    uiState.recentTransaction?.let { tx ->
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "Transaction ID: ${tx.id}",
                                fontWeight = FontWeight.Bold,
                                color = OrangeSecondary
                            )
                            Text("Net Payout: ${tx.netAmount}", color = Color.White)
                            Text("Destination: ${tx.destinationMasked}", color = Color.White)
                            Text(
                                "Estimated Arrival: ${tx.estimatedArrival}",
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.dismissSuccessModal() },
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                    ) {
                        Text("Done", fontWeight = FontWeight.Bold)
                    }
                })
        }
    }
}

@Composable
private fun BreakdownRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun PolicyBullet(title: String, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top
    ) {
        Text("•", color = OrangeSecondary, fontWeight = FontWeight.Bold)
        Column {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun TransactionRowCard(record: TransactionRecord) {
    GlassCoinCard {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    record.id,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "${record.payoutChannel.label} • ${record.destinationMasked}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Text(
                    record.dateIso,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    record.netAmount,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = OrangeSecondary
                )
                Surface(
                    color = when (record.status) {
                        TransactionStatus.COMPLETED -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                        TransactionStatus.PROCESSING, TransactionStatus.PENDING -> OrangeSecondary.copy(
                            alpha = 0.2f
                        )

                        else -> Color(0xFFFF5252).copy(alpha = 0.2f)
                    }, shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = record.status.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when (record.status) {
                            TransactionStatus.COMPLETED -> Color(0xFF4CAF50)
                            TransactionStatus.PROCESSING, TransactionStatus.PENDING -> OrangeSecondary
                            else -> Color(0xFFFF5252)
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun GlassCoinCard(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.10f), Color.White.copy(alpha = 0.03f)
                )
            )
        ).border(
            width = 1.dp, brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.30f), OrangePrimary.copy(alpha = 0.20f)
                )
            ), shape = RoundedCornerShape(22.dp)
        )
    ) {
        content()
    }
}

@Composable
private fun inputColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = OrangePrimary,
    unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
)
