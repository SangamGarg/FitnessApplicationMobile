package com.muscleplay.app.feature.games

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.muscleplay.app.core.network.dto.GameCatalogueResponseDto
import com.muscleplay.app.core.network.dto.GameDetailResponseDto
import com.muscleplay.app.core.network.dto.GamificationHomeSummaryResponseDto
import com.muscleplay.app.feature.games.data.GamesRepository

private val OrangePrimary = Color(0xFFFF5722)
private val OrangeSecondary = Color(0xFFFF9800)
private val OrangeGlow = Color(0xFFFF3D00)
private val DarkBackground = Color(0xFF0F0F17)
private val TextMuted = Color(0xFF9E9B9E)
private val CardBackground = Color(0xFF181824)
private val CardBorderColor = Color(0xFF2A283A)

private val supportedGameTypes = setOf("SQUAT_CHALLENGE", "PUSH_UP_CHALLENGE")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamesScreen(
    gamesRepository: GamesRepository? = null,
    onPlayGame: (String) -> Unit,
    onNavigateToRewardHistory: () -> Unit = {},
    onNavigateToGameHistory: () -> Unit = {},
) {
    val viewModel = remember(gamesRepository) { GamesViewModel(gamesRepository) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val infiniteTransition = rememberInfiniteTransition(label = "gamesPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(OrangeGlow.copy(alpha = pulseAlpha), Color.Transparent),
                    center = Offset(w * 0.85f, h * 0.15f),
                    radius = w * 0.65f
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        OrangeSecondary.copy(alpha = pulseAlpha * 0.6f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.15f, h * 0.85f),
                    radius = w * 0.55f
                )
            )
        }

        Scaffold(
            containerColor = Color.Transparent
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 24.dp, bottom = 90.dp)
            ) {
                // Header & Top Navigation Action Buttons
                item {
                    when (val state = uiState.homeSummaryState) {
                        is GamificationHomeUiState.Success -> {
                            val data = state.summary
                            HeaderSection(
                                summary = data,
                                onNavigateToRewardHistory = onNavigateToRewardHistory,
                                onNavigateToGameHistory = onNavigateToGameHistory
                            )
                        }

                        is GamificationHomeUiState.Loading -> {
                            ShimmerGamesHeader()
                        }

                        is GamificationHomeUiState.Error -> {
                            HeaderSectionFallback(
                                onNavigateToRewardHistory = onNavigateToRewardHistory,
                                onNavigateToGameHistory = onNavigateToGameHistory
                            )
                        }
                    }
                }

                // User Progress & Stats Section
                item {
                    when (val state = uiState.homeSummaryState) {
                        is GamificationHomeUiState.Success -> {
                            UserProgressCard(summary = state.summary)
                        }

                        is GamificationHomeUiState.Loading -> {
                            ShimmerProgressCard()
                        }

                        is GamificationHomeUiState.Error -> {
                            Surface(
                                color = OrangePrimary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    OrangePrimary.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = state.message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White,
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedButton(
                                        onClick = { viewModel.loadGamificationHome(forceRefresh = true) },
                                        shape = RoundedCornerShape(10.dp),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            OrangePrimary
                                        ),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangePrimary)
                                    ) {
                                        Icon(
                                            Icons.Default.Refresh,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "Retry",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Catalogue Title
                item {
                    val count =
                        (uiState.homeSummaryState as? GamificationHomeUiState.Success)?.summary?.totalAvailableGames
                            ?: 0
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Choose your game",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Surface(
                            color = Color.White.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "$count Games Available",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = OrangeSecondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Render Catalogue Games
                val catalogueGames =
                    (uiState.homeSummaryState as? GamificationHomeUiState.Success)?.summary?.games
                        ?: emptyList()

                if (uiState.homeSummaryState is GamificationHomeUiState.Loading) {
                    items(3) {
                        ShimmerGameCard()
                    }
                } else if (catalogueGames.isEmpty() && uiState.homeSummaryState is GamificationHomeUiState.Success) {
                    item {
                        Text(
                            text = "No games available right now.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
                        )
                    }
                } else if (catalogueGames.isNotEmpty()) {
                    items(catalogueGames, key = { it.gameType.ifBlank { it.title } }) { gameDto ->
                        val isSupported =
                            supportedGameTypes.contains(gameDto.gameType.uppercase().trim())
                        GlassGameCatalogueCard(
                            game = gameDto,
                            isSupported = isSupported,
                            onClick = { viewModel.selectGame(gameDto.gameType) }
                        )
                    }
                }
            }
        }

        // Game Details Dialog
        if (uiState.selectedGameType != null) {
            GlassGameDetailsDialog(
                detailState = uiState.gameDetailState,
                onDismiss = { viewModel.selectGame(null) },
                onStartGame = { gameType ->
                    val routeKey = when (gameType.uppercase().trim()) {
                        "SQUAT_CHALLENGE" -> "squat_master"
                        "PUSH_UP_CHALLENGE" -> "push_up_master"
                        else -> "rep_challenge"
                    }
                    viewModel.selectGame(null)
                    onPlayGame(routeKey)
                },
                onRetry = { viewModel.selectGame(uiState.selectedGameType) }
            )
        }
    }
}

@Composable
private fun HeaderSection(
    summary: GamificationHomeSummaryResponseDto,
    onNavigateToRewardHistory: () -> Unit,
    onNavigateToGameHistory: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    color = OrangePrimary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        OrangePrimary.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = summary.title ?: "AI Fitness Games",
                        style = MaterialTheme.typography.labelSmall,
                        color = OrangePrimary,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = summary.subTitle ?: "Play & Challenge 🎮",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onNavigateToRewardHistory) {
                    Icon(
                        Icons.Default.CardGiftcard,
                        contentDescription = "Rewards",
                        tint = OrangeSecondary
                    )
                }
                IconButton(onClick = onNavigateToGameHistory) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = "History",
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = summary.description
                ?: "Move, play, and challenge yourself with AI camera tracking.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun HeaderSectionFallback(
    onNavigateToRewardHistory: () -> Unit,
    onNavigateToGameHistory: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Play & Challenge 🎮",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(onClick = onNavigateToRewardHistory) {
                Icon(
                    Icons.Default.CardGiftcard,
                    contentDescription = "Rewards",
                    tint = OrangeSecondary
                )
            }
            IconButton(onClick = onNavigateToGameHistory) {
                Icon(
                    Icons.Default.History,
                    contentDescription = "History",
                    tint = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun UserProgressCard(summary: GamificationHomeSummaryResponseDto) {
    GlassCardContainer {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = OrangePrimary.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Level ${summary.level} Athlete 👑",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = OrangeSecondary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = "${summary.totalXp} XP (${summary.xpToNextLevel} to next)",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            val targetXp = (summary.totalXp + summary.xpToNextLevel).coerceAtLeast(1L)
            val xpProgress = (summary.totalXp.toFloat() / targetXp).coerceIn(0f, 1f)

            LinearProgressIndicator(
                progress = { xpProgress },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = OrangePrimary,
                trackColor = CardBorderColor
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatBadge(
                    icon = Icons.Default.MonetizationOn,
                    label = "Coins",
                    value = "${summary.coins} MP",
                    color = OrangeSecondary
                )
                StatBadge(
                    icon = Icons.Default.LocalFireDepartment,
                    label = "Streak",
                    value = "${summary.currentStreak} Days",
                    color = OrangePrimary
                )
                StatBadge(
                    icon = Icons.Default.EmojiEvents,
                    label = "Best Streak",
                    value = "${summary.longestStreak} Days",
                    color = Color(0xFF4CAF50)
                )
            }
        }
    }
}

@Composable
private fun StatBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color,
) {
    Surface(
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Column {
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 9.sp
                )
                Text(
                    value,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun GlassGameCatalogueCard(
    game: GameCatalogueResponseDto,
    isSupported: Boolean,
    onClick: () -> Unit,
) {
    GlassCardContainer {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(OrangePrimary.copy(alpha = 0.2f))
                        .border(1.dp, OrangePrimary.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!game.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = game.imageUrl,
                            contentDescription = game.title,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp))
                        )
                    } else {
                        Icon(
                            Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = OrangePrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = game.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )

                        if (!isSupported) {
                            Surface(
                                color = Color.White.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "Coming Soon",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        } else if (game.isNew) {
                            Surface(
                                color = OrangePrimary.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "NEW",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = OrangeSecondary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = "${game.level ?: "BEGINNER"} • ${game.durationLabel ?: "${game.durationValue}s"}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = OrangeSecondary
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            game.description?.let { desc ->
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 2
                )
                Spacer(Modifier.height(14.dp))
            }

            // Reward Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "+${game.rewardXp} XP",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = OrangePrimary
                    )
                    Text(
                        "+${game.rewardCoins} MP",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = OrangeSecondary
                    )
                }

                Button(
                    onClick = onClick,
                    modifier = Modifier.height(38.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSupported) OrangePrimary else Color.White.copy(
                            alpha = 0.12f
                        )
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isSupported) "Play Game" else "View Details",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GlassGameDetailsDialog(
    detailState: GameDetailUiState,
    onDismiss: () -> Unit,
    onStartGame: (String) -> Unit,
    onRetry: () -> Unit,
) {
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = CardBackground,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Game Details",
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
                    .verticalScroll(scrollState)
            ) {
                when (detailState) {
                    is GameDetailUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(180.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = OrangePrimary)
                        }
                    }

                    is GameDetailUiState.Success -> {
                        val detail = detailState.detail
                        val game = detail.game ?: GameCatalogueResponseDto()
                        val isSupported =
                            supportedGameTypes.contains(game.gameType.uppercase().trim())

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = game.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = OrangeSecondary
                            )

                            game.description?.let {
                                Text(
                                    it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                DetailRow("Level:", game.level ?: "BEGINNER")
                                DetailRow(
                                    "Duration:",
                                    game.durationLabel ?: "${game.durationValue}s"
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                DetailRow("Reward XP:", "+${game.rewardXp} XP")
                                DetailRow("Reward Coins:", "+${game.rewardCoins} MP")
                            }

                            if (detail.rules.isNotEmpty()) {
                                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                                Text(
                                    "Rules:",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                                detail.rules.forEach { rule ->
                                    Text(
                                        "• $rule",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            if (detail.tips.isNotEmpty()) {
                                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                                Text(
                                    "Tips:",
                                    fontWeight = FontWeight.Bold,
                                    color = OrangeSecondary,
                                    fontSize = 12.sp
                                )
                                detail.tips.forEach { tip ->
                                    Text(
                                        "💡 $tip",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }

                    is GameDetailUiState.Error -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                        ) {
                            Text(
                                text = detailState.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )
                            OutlinedButton(
                                onClick = onRetry,
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    OrangePrimary
                                ),
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
            val game = (detailState as? GameDetailUiState.Success)?.detail?.game
            val isSupported =
                game != null && supportedGameTypes.contains(game.gameType.uppercase().trim())

            Button(
                onClick = {
                    if (game != null) {
                        onStartGame(game.gameType)
                    }
                },
                enabled = isSupported,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSupported) OrangePrimary else Color.White.copy(
                        alpha = 0.12f
                    )
                )
            ) {
                Text(
                    text = if (isSupported) "Start Game 🎮" else "Coming Soon",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        Text(
            value,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun ShimmerGameCard() {
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

    GlassCardContainer {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = alpha))
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(18.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = alpha))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = alpha))
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = alpha))
            )
        }
    }
}

@Composable
private fun ShimmerGamesHeader() {
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

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .height(20.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White.copy(alpha = alpha))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White.copy(alpha = alpha))
        )
    }
}

@Composable
private fun ShimmerProgressCard() {
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

    GlassCardContainer {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(18.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White.copy(alpha = alpha))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = alpha))
            )
        }
    }
}

@Composable
private fun GlassCardContainer(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
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
                shape = RoundedCornerShape(26.dp)
            )
    ) {
        content()
    }
}
