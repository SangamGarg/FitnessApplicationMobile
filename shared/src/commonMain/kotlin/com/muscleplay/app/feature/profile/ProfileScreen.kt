package com.muscleplay.app.feature.profile

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhonelinkOff
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.PublicOff
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import com.muscleplay.app.core.network.auth.UserSessionRepository
import com.muscleplay.app.feature.auth.AuthRepository
import com.muscleplay.app.feature.home.data.DashboardRepository
import com.muscleplay.app.feature.profile.data.ProfileRepository
import kotlinx.coroutines.launch

private val OrangePrimary = Color(0xFFFF5722)
private val OrangeSecondary = Color(0xFFFF9800)
private val OrangeGlow = Color(0xFFFF3D00)
private val DarkBackground = Color(0xFF0F0F17)
private val CardBackground = Color(0xFF181824)
private val CardBorderColor = Color(0xFF2A283A)
private val CyanAccent = Color(0xFF00E5FF)
private val GreenAccent = Color(0xFF00E676)
private val TextMuted = Color(0xFF9E9B9E)

@Immutable
data class ProgressPhoto(
    val id: String,
    val title: String,
    val date: String,
    val subtitle: String,
)

val sampleProgressPhotos = listOf(
    ProgressPhoto("1", "Day 1 - Starting Out", "Oct 1, 2024", "Initial assessment & baseline"),
    ProgressPhoto("2", "Day 15 - Building Habit", "Oct 15, 2024", "Consistent routine established"),
    ProgressPhoto("3", "Day 30 - Core Strength", "Nov 1, 2024", "Core stability improved"),
    ProgressPhoto("4", "Day 45 - Endurance Peak", "Nov 15, 2024", "Cardio stamina milestone"),
    ProgressPhoto("5", "Day 60 - Muscle Tone", "Dec 1, 2024", "Upper body definition"),
    ProgressPhoto("6", "Day 75 - Definition Boost", "Dec 15, 2024", "Lower body strength gains"),
    ProgressPhoto("7", "Day 90 - Peak Fitness", "Jan 1, 2025", "Transformation milestone achieved"),
    ProgressPhoto("8", "Day 100 - Master Level", "Jan 10, 2025", "Ready for advanced workouts")
)

@Immutable
data class AchievementBadge(
    val id: String,
    val title: String,
    val emoji: String,
    val isUnlocked: Boolean,
    val criteria: String,
    val progressText: String,
)

val sampleAchievementBadges = listOf(
    AchievementBadge(
        "1", "First 100 Reps", "🥇", true, "Complete 100 total reps in workouts", "100 / 100 Done"
    ), AchievementBadge(
        "2", "14-Day Streak Titan", "🔥", true, "Maintain a 14-day workout streak", "14 / 14 Days"
    ), AchievementBadge(
        "3",
        "Boss Battle Conqueror",
        "👑",
        true,
        "Defeat a Boss Battle in Fitness Games",
        "Boss Defeated"
    ), AchievementBadge(
        "4", "1,000 MP Coins Earner", "🪙", true, "Earn 1,000 total MP Coins", "1,250 MP Earned"
    ), AchievementBadge(
        "5",
        "50 Squat Speed Demon",
        "⚡",
        false,
        "Complete 50 Squats in under 2 mins",
        "40 / 50 Reps (80%)"
    )
)

@Immutable
data class SocialHandleItem(
    val name: String,
    val handle: String,
    val icon: ImageVector,
    val iconColor: Color,
    val url: String,
)

@Composable
fun ProfileScreen(
    authRepository: AuthRepository? = null,
    dashboardRepository: DashboardRepository? = null,
    userSessionRepository: UserSessionRepository? = null,
    profileRepository: ProfileRepository? = null,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToHelp: () -> Unit = {},
    onNavigateToDiagnostics: () -> Unit = {},
    onNavigateToGallery: () -> Unit = {},
    onNavigateToCoinRedemption: () -> Unit = {},
    onLogout: () -> Unit = {},
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val sessionProfile by (userSessionRepository?.userProfile?.collectAsState()
        ?: remember { mutableStateOf(null) })

    val profileViewModel = remember(profileRepository) {
        profileRepository?.let {
            ProfileViewModel(it, coroutineScope)
        }
    }

    val feedbackState by (profileViewModel?.feedbackState?.collectAsState()
        ?: remember { mutableStateOf<FeedbackSubmitState>(FeedbackSubmitState.Idle) })

    val followUsState by (profileViewModel?.followUsState?.collectAsState()
        ?: remember { mutableStateOf<FollowUsUiState>(FollowUsUiState.Loading) })

    val viewModel = remember(dashboardRepository) {
        dashboardRepository?.let {
            com.muscleplay.app.feature.home.DashboardViewModel(
                it, coroutineScope
            )
        }
    }

    val uiState by (viewModel?.uiState?.collectAsState() ?: remember {
        mutableStateOf<com.muscleplay.app.feature.home.DashboardUiState>(
            com.muscleplay.app.feature.home.DashboardUiState.Success(createSampleProfileDashboardDto())
        )
    })

    // Only load dashboard if sessionProfile is not present in memory
    LaunchedEffect(sessionProfile) {
        if (sessionProfile == null) {
            viewModel?.loadDashboard(isRefresh = false)
        }
    }

    val dashboardData = (uiState as? com.muscleplay.app.feature.home.DashboardUiState.Success)?.data
    val userName = sessionProfile?.name ?: dashboardData?.user?.name ?: "Alex Fitness"
    val userEmail =
        sessionProfile?.email ?: dashboardData?.user?.email ?: "alex.fitness@muscleplay.com"
    val membershipStatus =
        if (sessionProfile?.profileCompleted == true || dashboardData?.user?.profileCompleted == true) "Pro Member" else "Member"
    val streakDays = dashboardData?.workoutAnalytics?.activeDays?.toInt() ?: 12
    val mpCoins = dashboardData?.wallet?.balanceCoins?.toInt() ?: 1250

    val expandedHeaderHeight = 250.dp
    val collapsedHeaderHeight = 64.dp
    val density = LocalDensity.current
    val collapseRangePx = with(density) {
        (expandedHeaderHeight - collapsedHeaderHeight).toPx()
    }

    var collapsedPx by remember { mutableFloatStateOf(0f) }
    val collapseProgress = (collapsedPx / collapseRangePx).coerceIn(0f, 1f)

    val nestedScrollConnection = remember(collapseRangePx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                if (delta >= 0f) return Offset.Zero
                val old = collapsedPx
                collapsedPx = (old - delta).coerceIn(0f, collapseRangePx)
                return Offset(0f, -(collapsedPx - old))
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                val delta = available.y
                if (delta <= 0f) return Offset.Zero
                val old = collapsedPx
                collapsedPx = (old - delta).coerceIn(0f, collapseRangePx)
                return Offset(0f, old - collapsedPx)
            }
        }
    }

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showFeedbackBottomSheet by remember { mutableStateOf(false) }
    var showRateDialog by remember { mutableStateOf(false) }
    var showFollowDialog by remember { mutableStateOf(false) }
    var selectedBadgeDetail by remember { mutableStateOf<AchievementBadge?>(null) }
    var showCompareDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    val infiniteTransition = rememberInfiniteTransition(label = "profilePulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f, targetValue = 0.45f, animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing), repeatMode = RepeatMode.Reverse
        ), label = "pulseAlpha"
    )

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(DarkBackground)
    ) {
        // Ambient Background Radial Glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(OrangeGlow.copy(alpha = pulseAlpha * 0.7f), Color.Transparent),
                    center = Offset(w * 0.85f, h * 0.15f),
                    radius = w * 0.75f
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        OrangeSecondary.copy(alpha = pulseAlpha * 0.5f), Color.Transparent
                    ), center = Offset(w * 0.15f, h * 0.8f), radius = w * 0.65f
                )
            )
        }

        Scaffold(
            containerColor = Color.Transparent, snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.padding(bottom = 88.dp, start = 16.dp, end = 16.dp)
                ) { data ->
                    Snackbar(
                        snackbarData = data,
                        shape = RoundedCornerShape(16.dp),
                        containerColor = CardBackground,
                        contentColor = Color.White,
                        actionColor = OrangeSecondary,
                        dismissActionContentColor = TextMuted
                    )
                }
            }) { innerPadding ->
            Column(
                modifier = Modifier.fillMaxSize().nestedScroll(nestedScrollConnection)
            ) {
                // Pinned Header with Level & XP Bar
                Box(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 10.dp)) {
                    GlassCollapsibleProfileCard(
                        userName = userName,
                        userEmail = userEmail,
                        membershipStatus = membershipStatus,
                        streakDays = streakDays,
                        mpCoins = mpCoins,
                        collapseProgress = collapseProgress,
                        expandedHeight = expandedHeaderHeight,
                        collapsedHeight = collapsedHeaderHeight,
                        onSettingsClick = onNavigateToSettings
                    )
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Task 2: Lifetime Athletic Trophies & Badges Section
                    item {
                        GlassTrophiesAndBadgesSection(
                            badges = sampleAchievementBadges,
                            onBadgeClick = { badge -> selectedBadgeDetail = badge })
                    }

                    // Progress Gallery Section with Transformation Compare Button
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Progress Gallery",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                TextButton(onClick = { showCompareDialog = true }) {
                                    Text(
                                        "Compare 🖼️",
                                        color = OrangePrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                TextButton(onClick = { onNavigateToGallery() }) {
                                    Text(
                                        "View All",
                                        color = OrangeSecondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        GlassProgressGallerySection(
                            photos = sampleProgressPhotos.take(4), onImageClick = { photo ->
                                snackbarMessage = "Viewing ${photo.title}"
                            })
                    }

                    item {
                        Text(
                            text = "App & Account Options",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    // Options List
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            GlassProfileMenuItem(
                                icon = Icons.Default.AccountBalanceWallet,
                                title = "MP Coins",
                                subtitle = "Buy, earn via invites, play games, cash out & view history",
                                onClick = { onNavigateToCoinRedemption() })
                            GlassProfileMenuItem(
                                icon = Icons.Default.Settings,
                                title = "Settings & Preferences",
                                subtitle = "Preferences, notifications, theme",
                                onClick = { onNavigateToSettings() })
                            // Task 4: Export Workout Logs Report
                            GlassProfileMenuItem(
                                icon = Icons.Default.SaveAlt,
                                title = "Export Workout Logs & Analytics",
                                subtitle = "Download PDF report or CSV spreadsheet",
                                onClick = { showExportDialog = true })
                            GlassProfileMenuItem(
                                icon = Icons.Default.HealthAndSafety,
                                title = "Self-Diagnostics Check",
                                subtitle = "Verify camera, sensors, and app health",
                                onClick = { onNavigateToDiagnostics() })
                            GlassProfileMenuItem(
                                icon = Icons.AutoMirrored.Filled.HelpOutline,
                                title = "Help & Support",
                                subtitle = "FAQs, contact support, guides",
                                onClick = { onNavigateToHelp() })
                            GlassProfileMenuItem(
                                icon = Icons.Default.Feedback,
                                title = "Tell Us What You Think",
                                subtitle = "Share your feedback & feature requests",
                                onClick = { showFeedbackBottomSheet = true })
                            GlassProfileMenuItem(
                                icon = Icons.Default.Star,
                                title = "Rate and Review Us",
                                subtitle = "Support MusclePlay on the Play Store",
                                onClick = { showRateDialog = true })
                            GlassProfileMenuItem(
                                icon = Icons.Default.Share,
                                title = "Follow Us On",
                                subtitle = "Instagram, YouTube, X, and Community",
                                onClick = { showFollowDialog = true })
                            GlassProfileMenuItem(
                                icon = Icons.AutoMirrored.Filled.Logout,
                                title = "Logout",
                                subtitle = "Sign out of your MusclePlay account",
                                tint = Color(0xFFFF5252),
                                onClick = { showLogoutDialog = true })
                        }
                    }
                }
            }

            // Trophy Detail Modal
            selectedBadgeDetail?.let { badge ->
                AlertDialog(
                    onDismissRequest = { selectedBadgeDetail = null },
                    shape = RoundedCornerShape(24.dp),
                    containerColor = CardBackground,
                    icon = {
                        Box(
                            modifier = Modifier.size(52.dp).clip(CircleShape)
                                .background(OrangePrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(badge.emoji, fontSize = 28.sp)
                        }
                    },
                    title = {
                        Text(
                            badge.title, fontWeight = FontWeight.Bold, color = Color.White
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "Criteria: ${badge.criteria}",
                                color = Color.White.copy(alpha = 0.85f)
                            )
                            Text(
                                "Progress: ${badge.progressText}",
                                fontWeight = FontWeight.Bold,
                                color = OrangeSecondary
                            )
                            Surface(
                                color = if (badge.isUnlocked) GreenAccent.copy(alpha = 0.2f) else Color.White.copy(
                                    alpha = 0.08f
                                ), shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (badge.isUnlocked) "UNLOCKED 🎉" else "LOCKED 🔒",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (badge.isUnlocked) GreenAccent else TextMuted,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                selectedBadgeDetail = null
                                snackbarMessage = "Sharing ${badge.title} badge..."
                            }, colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                        ) {
                            Text("Share Badge 📲", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { selectedBadgeDetail = null }) {
                            Text("Close", color = TextMuted)
                        }
                    })
            }

            // Transformation Comparison Modal
            if (showCompareDialog) {
                var photo1 by remember { mutableStateOf(sampleProgressPhotos.first()) }
                var photo2 by remember { mutableStateOf(sampleProgressPhotos.last()) }

                AlertDialog(
                    onDismissRequest = { showCompareDialog = false },
                    shape = RoundedCornerShape(24.dp),
                    containerColor = CardBackground,
                    icon = {
                        Box(
                            modifier = Modifier.size(52.dp).clip(CircleShape)
                                .background(OrangePrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Compare,
                                contentDescription = null,
                                tint = OrangeSecondary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    },
                    title = {
                        Text(
                            "Compare Transformation 🖼️",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                "Side-by-side milestone comparison:",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ComparePhotoPreviewCard("BEFORE", photo1, Modifier.weight(1f))
                                ComparePhotoPreviewCard("AFTER", photo2, Modifier.weight(1f))
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showCompareDialog = false
                                snackbarMessage = "Transformation comparison saved!"
                            }, colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                        ) {
                            Text("Done", fontWeight = FontWeight.Bold)
                        }
                    })
            }

            // Export Workout Logs Modal
            if (showExportDialog) {
                AlertDialog(
                    onDismissRequest = { showExportDialog = false },
                    shape = RoundedCornerShape(24.dp),
                    containerColor = CardBackground,
                    icon = {
                        Box(
                            modifier = Modifier.size(52.dp).clip(CircleShape)
                                .background(OrangePrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.SaveAlt,
                                contentDescription = null,
                                tint = OrangeSecondary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    },
                    title = {
                        Text(
                            "Export Workout Logs 💾",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                "Choose your preferred report export format:",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )

                            Button(
                                onClick = {
                                    showExportDialog = false
                                    snackbarMessage = "PDF Report generated successfully! 📄"
                                },
                                modifier = Modifier.fillMaxWidth().height(46.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                            ) {
                                Text("Export PDF Report 📄", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    showExportDialog = false
                                    snackbarMessage = "CSV Spreadsheet exported successfully! 📊"
                                },
                                modifier = Modifier.fillMaxWidth().height(46.dp),
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp, OrangeSecondary
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangeSecondary)
                            ) {
                                Text("Export CSV Spreadsheet 📊", fontWeight = FontWeight.Bold)
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(onClick = { showExportDialog = false }) {
                            Text("Cancel", color = TextMuted)
                        }
                    })
            }

            // Dark Glassmorphic 3-Option Logout Confirmation Dialog
            if (showLogoutDialog) {
                AlertDialog(
                    onDismissRequest = { showLogoutDialog = false },
                    shape = RoundedCornerShape(24.dp),
                    containerColor = CardBackground,
                    icon = {
                        Box(
                            modifier = Modifier.size(52.dp).clip(CircleShape)
                                .background(Color(0xFFFF5252).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Logout",
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    },
                    title = {
                        Text(
                            text = "Logout Options 🚪",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Choose your preferred sign-out option:",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )

                            // Option 1: Logout from This Device
                            Button(
                                onClick = {
                                    showLogoutDialog = false
                                    coroutineScope.launch {
                                        authRepository?.logout(logoutEverywhere = false)
                                        onLogout()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(
                                        0xFFFF5252
                                    )
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.PhonelinkOff,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        "Logout from This Device 📱",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            // Option 2: Logout from All Devices
                            OutlinedButton(
                                onClick = {
                                    showLogoutDialog = false
                                    coroutineScope.launch {
                                        authRepository?.logout(logoutEverywhere = true)
                                        onLogout()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp, OrangeSecondary
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangeSecondary)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.PublicOff,
                                        contentDescription = null,
                                        tint = OrangeSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        "Logout from All Devices 🌐",
                                        fontWeight = FontWeight.Bold,
                                        color = OrangeSecondary
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(onClick = { showLogoutDialog = false }) {
                            Text("Cancel", color = TextMuted, fontWeight = FontWeight.Bold)
                        }
                    })
            }

            val feedbackError = (feedbackState as? FeedbackSubmitState.Error)?.message

            LaunchedEffect(feedbackState) {
                when (val state = feedbackState) {
                    is FeedbackSubmitState.Success -> {
                        snackbarMessage = state.message
                        showFeedbackBottomSheet = false
                        profileViewModel?.resetFeedbackState()
                    }

                    else -> {}
                }
            }

            if (showFeedbackBottomSheet) {
                GlassFeedbackBottomSheet(
                    isSubmitting = feedbackState is FeedbackSubmitState.Submitting,
                    errorMessage = feedbackError,
                    onDismiss = {
                        showFeedbackBottomSheet = false
                        profileViewModel?.resetFeedbackState()
                    },
                    onSubmit = { text ->
                        profileViewModel?.resetFeedbackState()
                        profileViewModel?.submitFeedback(text.trim())
                    })
            }

            if (showRateDialog) {
                AlertDialog(
                    onDismissRequest = { showRateDialog = false },
                    shape = RoundedCornerShape(24.dp),
                    containerColor = CardBackground,
                    icon = {
                        Box(
                            modifier = Modifier.size(56.dp).clip(CircleShape)
                                .background(OrangePrimary.copy(alpha = 0.2f))
                                .border(1.dp, OrangePrimary.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Rate Us",
                                tint = OrangeSecondary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    },
                    title = {
                        Text(
                            text = "Rate MusclePlay ⭐️",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    },
                    text = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Enjoying MusclePlay? Please take a moment to rate us 5 stars on the Play Store and help us grow!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.85f)
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(5) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = OrangeSecondary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showRateDialog = false
                                snackbarMessage = "Redirecting to Play Store..."
                            },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                        ) {
                            Text("Rate Now 🔥", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showRateDialog = false }) {
                            Text("Later", color = TextMuted)
                        }
                    })
            }

            if (showFollowDialog) {
                val uriHandler = LocalUriHandler.current

                LaunchedEffect(showFollowDialog) {
                    profileViewModel?.loadFollowUsLinks()
                }

                AlertDialog(
                    onDismissRequest = { showFollowDialog = false },
                    shape = RoundedCornerShape(24.dp),
                    containerColor = CardBackground,
                    title = {
                        Text(
                            text = "Follow Us On 🌐",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Stay updated with workout challenges, tips & community events:",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )

                            when (val followState = followUsState) {
                                is FollowUsUiState.Loading -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        repeat(3) {
                                            SocialHandleRowShimmerItem()
                                        }
                                    }
                                }

                                is FollowUsUiState.Success -> {
                                    LazyColumn(
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.heightIn(max = 280.dp)
                                    ) {
                                        items(
                                            followState.links,
                                            key = { it.id ?: it.title ?: "" }) { dto ->
                                            val (icon, color) = mapFollowUsIcon(dto.iconKey)
                                            val item = SocialHandleItem(
                                                name = dto.title ?: "MusclePlay",
                                                handle = dto.subtitle ?: "@MusclePlayApp",
                                                icon = icon,
                                                iconColor = color,
                                                url = dto.url ?: ""
                                            )
                                            SocialHandleRowItem(
                                                item = item, onClick = {
                                                    val opened = launchSafeUrl(dto.url, uriHandler)
                                                    if (!opened) {
                                                        snackbarMessage = "Unable to open link"
                                                    } else {
                                                        showFollowDialog = false
                                                    }
                                                })
                                        }
                                    }
                                }

                                is FollowUsUiState.Empty -> {
                                    Text(
                                        text = "No social links available at the moment.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                                    )
                                }

                                is FollowUsUiState.Error -> {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
                                    ) {
                                        Text(
                                            text = followState.message,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextMuted,
                                            textAlign = TextAlign.Center
                                        )
                                        OutlinedButton(
                                            onClick = {
                                                profileViewModel?.loadFollowUsLinks(
                                                    forceRefresh = true
                                                )
                                            },
                                            shape = RoundedCornerShape(14.dp),
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp, OrangePrimary
                                            ),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = OrangePrimary
                                            )
                                        ) {
                                            Text(
                                                "Retry",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { showFollowDialog = false },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                        ) {
                            Text("Close", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    })
            }
        }
    }
}

@Composable
fun GlassProgressGallerySection(
    photos: List<ProgressPhoto>,
    onImageClick: (ProgressPhoto) -> Unit,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        items(photos, key = { it.id }) { photo ->
            Surface(
                onClick = { onImageClick(photo) },
                shape = RoundedCornerShape(18.dp),
                color = CardBackground,
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor),
                modifier = Modifier.width(130.dp).height(120.dp)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier.size(50.dp).clip(RoundedCornerShape(12.dp))
                            .background(OrangePrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = null,
                            tint = OrangePrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Text(
                        photo.title,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1
                    )
                    Text(
                        photo.date,
                        style = MaterialTheme.typography.labelSmall,
                        color = OrangeSecondary,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun GlassProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: Color = OrangePrimary,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = CardBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier.size(42.dp).clip(CircleShape)
                        .background(tint.copy(alpha = 0.18f)), contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = title,
                        tint = tint,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.4f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassFeedbackBottomSheet(
    isSubmitting: Boolean = false,
    errorMessage: String? = null,
    onDismiss: () -> Unit,
    onSubmit: (text: String) -> Unit,
) {
    var feedbackText by remember { mutableStateOf("") }
    val isEnabled = feedbackText.isNotBlank() && !isSubmitting

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = CardBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "Tell Us What You Think 💬",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                "Your feedback directly shapes new features for MusclePlay!",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )

            if (!errorMessage.isNullOrBlank()) {
                Surface(
                    color = OrangePrimary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OrangePrimary.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        //text = errorMessage,
                        text = "Error Occurred While Submitting Feedback. Please Try Again.",
                        style = MaterialTheme.typography.bodySmall,
                        color = OrangePrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedTextField(
                    value = feedbackText,
                    onValueChange = { input ->
                        if (input.length <= 500) {
                            feedbackText = input
                        }
                    },
                    placeholder = {
                        Text(
                            "Describe your feedback or feature request...", color = TextMuted
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isSubmitting,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = OrangePrimary,
                        unfocusedBorderColor = CardBorderColor
                    )
                )

                Text(
                    text = "${feedbackText.length}/500",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (feedbackText.length == 500) OrangePrimary else TextMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }

            if (isSubmitting) {
                Button(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary.copy(alpha = 0.6f))
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp
                    )
                }
            } else if (isEnabled) {
                Button(
                    onClick = {
                        if (feedbackText.isNotBlank()) {
                            onSubmit(feedbackText)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    Text("Submit Feedback 🚀", fontWeight = FontWeight.Bold, color = Color.White)
                }
            } else {
                OutlinedButton(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, Color.White.copy(alpha = 0.2f)
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        disabledContentColor = Color.White.copy(
                            alpha = 0.4f
                        )
                    )
                ) {
                    Text("Submit Feedback 🚀", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SocialHandleRowShimmerItem() {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 0.6f, animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing), repeatMode = RepeatMode.Reverse
        ), label = "alpha"
    )

    Surface(
        modifier = Modifier.fillMaxWidth().height(58.dp),
        shape = RoundedCornerShape(16.dp),
        color = CardBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(38.dp).clip(CircleShape)
                    .background(Color.White.copy(alpha = alpha))
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(0.5f).height(14.dp)
                        .clip(RoundedCornerShape(4.dp)).background(Color.White.copy(alpha = alpha))
                )
                Box(
                    modifier = Modifier.fillMaxWidth(0.35f).height(10.dp)
                        .clip(RoundedCornerShape(4.dp)).background(Color.White.copy(alpha = alpha))
                )
            }
        }
    }
}

private fun mapFollowUsIcon(iconKey: String?): Pair<ImageVector, Color> {
    return when (iconKey?.uppercase()?.trim()) {
        "INSTAGRAM" -> Icons.Default.PhotoCamera to Color(0xFFE1306C)
        "YOUTUBE" -> Icons.Default.PlayCircle to Color(0xFFFF0000)
        "TWITTER", "X" -> Icons.Default.AlternateEmail to Color(0xFF1DA1F2)
        "DISCORD" -> Icons.Default.Forum to Color(0xFF5865F2)
        "FACEBOOK" -> Icons.Default.Public to Color(0xFF1877F2)
        else -> Icons.Default.Public to OrangeSecondary
    }
}

private fun launchSafeUrl(
    url: String?,
    uriHandler: androidx.compose.ui.platform.UriHandler,
): Boolean {
    if (url.isNullOrBlank()) return false
    val trimmed = url.trim()
    val isHttpOrHttps = trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith(
        "https://",
        ignoreCase = true
    )
    if (!isHttpOrHttps) return false
    return try {
        uriHandler.openUri(trimmed)
        true
    } catch (e: Throwable) {
        false
    }
}

@Composable
fun ProfileStatBadge(label: String, value: String) {
    Surface(
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                label, style = MaterialTheme.typography.labelSmall, color = TextMuted
            )
            Text(
                value,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = OrangeSecondary
            )
        }
    }
}

@Composable
private fun GlassTrophiesAndBadgesSection(
    badges: List<AchievementBadge>,
    onBadgeClick: (AchievementBadge) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = OrangeSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    "Athletic Trophies & Badges 🏆",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(badges, key = { it.id }) { badge ->
                GlassBadgeCard(badge = badge, onClick = { onBadgeClick(badge) })
            }
        }
    }
}

@Composable
private fun GlassBadgeCard(
    badge: AchievementBadge,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = CardBackground,
        border = androidx.compose.foundation.BorderStroke(
            1.dp, if (badge.isUnlocked) OrangePrimary.copy(alpha = 0.5f) else CardBorderColor
        ),
        modifier = Modifier.width(100.dp).height(90.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(badge.emoji, fontSize = 18.sp)
            Text(
                badge.title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth()
            )
            Surface(
                color = if (badge.isUnlocked) OrangeSecondary.copy(alpha = 0.25f) else Color.White.copy(
                    alpha = 0.08f
                ), shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = if (badge.isUnlocked) "Unlocked" else "Locked",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = if (badge.isUnlocked) OrangeSecondary else TextMuted,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun ComparePhotoPreviewCard(
    label: String,
    photo: ProgressPhoto,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = CardBackground,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = OrangeSecondary
            )
            Box(
                modifier = Modifier.fillMaxWidth().height(110.dp).clip(RoundedCornerShape(12.dp))
                    .background(OrangePrimary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Image,
                    contentDescription = null,
                    tint = OrangePrimary,
                    modifier = Modifier.size(42.dp)
                )
            }
            Text(
                photo.title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1
            )
            Text(
                photo.date, style = MaterialTheme.typography.labelSmall, color = TextMuted
            )
        }
    }
}

@Composable
private fun SocialHandleRowItem(
    item: SocialHandleItem,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CardBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier.size(38.dp).clip(CircleShape)
                        .background(item.iconColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.name,
                        tint = item.iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = item.handle,
                        style = MaterialTheme.typography.bodySmall,
                        color = OrangeSecondary
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = "Open Link",
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun GlassCollapsibleProfileCard(
    userName: String,
    userEmail: String,
    membershipStatus: String,
    streakDays: Int,
    mpCoins: Int = 1250,
    collapseProgress: Float,
    expandedHeight: Dp,
    collapsedHeight: Dp,
    onSettingsClick: () -> Unit,
) {
    val cardHeight = lerp(expandedHeight, collapsedHeight, collapseProgress)
    val avatarSize = (64.dp - (26.dp * collapseProgress))
    val avatarIconSize = (36.dp - (16.dp * collapseProgress))
    val cardPaddingHorizontal = (14.dp - (4.dp * collapseProgress))
    val cardPaddingVertical = (8.dp - (2.dp * collapseProgress))
    val cornerRadius = (22.dp - (6.dp * collapseProgress))

    GlassContainer(
        modifier = Modifier.fillMaxWidth().height(cardHeight),
        shape = RoundedCornerShape(cornerRadius)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
                .padding(horizontal = cardPaddingHorizontal, vertical = cardPaddingVertical),
            contentAlignment = Alignment.Center
        ) {
            if (collapseProgress < 0.5f) {
                val alpha = (1f - (collapseProgress * 2f)).coerceIn(0f, 1f)
                Column(
                    modifier = Modifier.fillMaxWidth().graphicsLayer { this.alpha = alpha },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(avatarSize).clip(CircleShape).background(
                                Brush.linearGradient(
                                    listOf(
                                        OrangePrimary, OrangeSecondary
                                    )
                                )
                            ), contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile Picture",
                            tint = Color.White,
                            modifier = Modifier.size(avatarIconSize)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = userName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    Text(
                        text = userEmail,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Task 1: Level 14 & XP Progress Bar
                    val currentXp = 3450
                    val targetXp = 5000
                    Column(
                        modifier = Modifier.fillMaxWidth(0.9f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = OrangePrimary.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Level 14 • Muscle Titan 👑",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = OrangeSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = "$currentXp / $targetXp XP",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }

                        LinearProgressIndicator(
                            progress = { currentXp.toFloat() / targetXp },
                            modifier = Modifier.fillMaxWidth().height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = OrangePrimary,
                            trackColor = CardBorderColor
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ProfileStatBadge(label = "Status", value = membershipStatus)
                            ProfileStatBadge(label = "Streak", value = "$streakDays Days 🔥")
                            ProfileStatBadge(label = "MP Coins", value = "$mpCoins MP 🪙")
                        }
                    }
                }
            } else {
                val alpha = ((collapseProgress - 0.5f) * 2f).coerceIn(0f, 1f)
                Row(
                    modifier = Modifier.fillMaxWidth().graphicsLayer { this.alpha = alpha },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(CircleShape).background(
                                    Brush.linearGradient(
                                        listOf(
                                            OrangePrimary, OrangeSecondary
                                        )
                                    )
                                ), contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile Picture",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = userName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Box(
                                    modifier = Modifier.clip(RoundedCornerShape(50)).background(
                                            Brush.linearGradient(
                                                listOf(OrangePrimary, OrangeSecondary)
                                            )
                                        ).padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = membershipStatus,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                }
                            }
                            Text(
                                text = "Lvl 14 • $streakDays Days 🔥 • $mpCoins MP 🪙",
                                style = MaterialTheme.typography.bodySmall,
                                color = OrangeSecondary,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GlassContainer(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(24.dp),
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier.clip(shape).background(CardBackground).border(
                width = 1.dp, color = CardBorderColor, shape = shape
            )
    ) {
        content()
    }
}

private fun createSampleProfileDashboardDto(): com.muscleplay.app.core.network.dto.DashboardResponseDto {
    return com.muscleplay.app.core.network.dto.DashboardResponseDto(
        user = com.muscleplay.app.core.network.dto.DashboardResponseDto.UserSummary(
            userId = "1",
            email = "alex.fitness@muscleplay.com",
            name = "Alex Fitness",
            profileCompleted = true
        ), workoutAnalytics = com.muscleplay.app.core.network.dto.WorkoutAnalyticsOverviewResponse(
            activeDays = 12
        ), wallet = com.muscleplay.app.core.network.dto.DashboardResponseDto.WalletSummary(
            balanceCoins = 1250
        )
    )
}
