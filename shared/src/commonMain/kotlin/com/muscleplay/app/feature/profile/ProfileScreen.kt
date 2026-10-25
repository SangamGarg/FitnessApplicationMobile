package com.muscleplay.app.feature.profile

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Vibrant Orange Glassmorphism Theme Palette
private val OrangePrimary = Color(0xFFFF5722)
private val OrangeSecondary = Color(0xFFFF9800)
private val OrangeGlow = Color(0xFFFF3D00)
private val DarkBackground = Color(0xFF0F0F17)

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToSettings: () -> Unit = {},
    onNavigateToHelp: () -> Unit = {},
    onNavigateToDiagnostics: () -> Unit = {},
    onNavigateToGallery: () -> Unit = {},
    onLogout: () -> Unit = {},
) {
    val listState = rememberLazyListState()
    
    // Smooth Collapse Progress (0f = Expanded card, 1f = Compact top-left header)
    val collapseProgress by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) {
                1f
            } else {
                (listState.firstVisibleItemScrollOffset / 350f).coerceIn(0f, 1f)
            }
        }
    }

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showFeedbackBottomSheet by remember { mutableStateOf(false) }
    var showRateDialog by remember { mutableStateOf(false) }
    var showFollowDialog by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Ambient Glowing Background Pulsing Animation
    val infiniteTransition = rememberInfiniteTransition(label = "profilePulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Glowing Background Orbs
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(OrangeGlow.copy(alpha = pulseAlpha), Color.Transparent),
                    center = Offset(w * 0.8f, h * 0.2f),
                    radius = w * 0.6f
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        OrangeSecondary.copy(alpha = pulseAlpha * 0.6f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.15f, h * 0.8f),
                    radius = w * 0.5f
                )
            )
        }

        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 4.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Collapsible Motion Profile Header Card
                item {
                    GlassCollapsibleProfileCard(
                        userName = "Alex Fitness",
                        userEmail = "alex.fitness@muscleplay.com",
                        membershipStatus = "Pro Member",
                        streakDays = 12,
                        collapseProgress = collapseProgress,
                        onSettingsClick = onNavigateToSettings
                    )
                }

                // Progress Gallery Section
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
                        TextButton(onClick = { onNavigateToGallery() }) {
                            Text("View All", color = OrangeSecondary, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    GlassProgressGallerySection(
                        photos = sampleProgressPhotos.take(4),
                        onImageClick = { photo ->
                            snackbarMessage = "Viewing ${photo.title}"
                        }
                    )
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
                            icon = Icons.Default.Settings,
                            title = "Settings & Preferences",
                            subtitle = "Preferences, notifications, theme",
                            onClick = { onNavigateToSettings() }
                        )
                        GlassProfileMenuItem(
                            icon = Icons.Default.HealthAndSafety,
                            title = "Self-Diagnostics Check",
                            subtitle = "Verify camera, sensors, and app health",
                            onClick = { onNavigateToDiagnostics() }
                        )
                        GlassProfileMenuItem(
                            icon = Icons.AutoMirrored.Filled.HelpOutline,
                            title = "Help & Support",
                            subtitle = "FAQs, contact support, guides",
                            onClick = { onNavigateToHelp() }
                        )
                        GlassProfileMenuItem(
                            icon = Icons.Default.Feedback,
                            title = "Tell Us What You Think",
                            subtitle = "Share your feedback & feature requests",
                            onClick = { showFeedbackBottomSheet = true }
                        )
                        GlassProfileMenuItem(
                            icon = Icons.Default.Star,
                            title = "Rate and Review Us",
                            subtitle = "Support MusclePlay on the Play Store",
                            onClick = { showRateDialog = true }
                        )
                        GlassProfileMenuItem(
                            icon = Icons.Default.Share,
                            title = "Follow Us On",
                            subtitle = "Instagram, YouTube, X, and Community",
                            onClick = { showFollowDialog = true }
                        )
                        GlassProfileMenuItem(
                            icon = Icons.AutoMirrored.Filled.Logout,
                            title = "Logout",
                            subtitle = "Sign out of your MusclePlay account",
                            tint = Color(0xFFFF5252),
                            onClick = { showLogoutDialog = true }
                        )
                    }
                }
            }

            // Dialogs & Bottom Sheets
            if (showLogoutDialog) {
                AlertDialog(
                    onDismissRequest = { showLogoutDialog = false },
                    title = { Text("Logout") },
                    text = { Text("Are you sure you want to log out of MusclePlay?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showLogoutDialog = false
                                onLogout()
                                snackbarMessage = "Logged out successfully"
                            }
                        ) {
                            Text("Logout", color = Color(0xFFFF5252))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showLogoutDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            if (showFeedbackBottomSheet) {
                GlassFeedbackBottomSheet(
                    onDismiss = { showFeedbackBottomSheet = false },
                    onSubmit = { _, _, _ ->
                        showFeedbackBottomSheet = false
                        snackbarMessage = "Thank you for your feedback!"
                    }
                )
            }

            if (showRateDialog) {
                AlertDialog(
                    onDismissRequest = { showRateDialog = false },
                    icon = {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = OrangeSecondary
                        )
                    },
                    title = { Text("Rate MusclePlay") },
                    text = { Text("Enjoying MusclePlay? Please take a moment to rate us 5 stars on the Play Store and help us grow!") },
                    confirmButton = {
                        Button(
                            onClick = {
                                showRateDialog = false
                                snackbarMessage = "Redirecting to Play Store..."
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                        ) {
                            Text("Rate Now")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showRateDialog = false }) {
                            Text("Later")
                        }
                    }
                )
            }

            if (showFollowDialog) {
                AlertDialog(
                    onDismissRequest = { showFollowDialog = false },
                    title = { Text("Follow Us On") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Stay updated with workout challenges and fitness tips:")
                            Text("• Instagram: @MusclePlayApp", color = OrangeSecondary)
                            Text("• YouTube: MusclePlay Official", color = OrangeSecondary)
                            Text("• X (Twitter): @MusclePlay", color = OrangeSecondary)
                            Text(
                                "• Community Discord: discord.gg/muscleplay",
                                color = OrangeSecondary
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { showFollowDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                        ) {
                            Text("Close")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun GlassCollapsibleProfileCard(
    userName: String,
    userEmail: String,
    membershipStatus: String,
    streakDays: Int,
    collapseProgress: Float,
    onSettingsClick: () -> Unit
) {
    val avatarSize = (84.dp - (42.dp * collapseProgress))
    val avatarIconSize = (50.dp - (24.dp * collapseProgress))
    val cardPadding = (22.dp - (10.dp * collapseProgress))
    val cornerRadius = (28.dp - (8.dp * collapseProgress))

    GlassContainer(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(cornerRadius)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(cardPadding)
        ) {
            if (collapseProgress < 0.5f) {
                // Expanded Full Card Layout
                val alpha = (1f - (collapseProgress * 2f)).coerceIn(0f, 1f)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer { this.alpha = alpha },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(avatarSize)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(OrangePrimary, OrangeSecondary))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile Picture",
                            tint = Color.White,
                            modifier = Modifier.size(avatarIconSize)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = userName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    Text(
                        text = userEmail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ProfileStatBadge(label = "Status", value = membershipStatus)
                        ProfileStatBadge(label = "Streak", value = "$streakDays Days 🔥")
                    }
                }
            } else {
                // Compact Motion Header Layout (Positioned Top Left)
                val alpha = ((collapseProgress - 0.5f) * 2f).coerceIn(0f, 1f)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer { this.alpha = alpha },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(OrangePrimary, OrangeSecondary))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile Picture",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = userName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "$userEmail • $streakDays Days 🔥",
                                style = MaterialTheme.typography.bodySmall,
                                color = OrangeSecondary,
                                fontWeight = FontWeight.Bold
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassFeedbackBottomSheet(
    onDismiss: () -> Unit,
    onSubmit: (isThumbsUp: Boolean?, rating: Int, feedbackText: String) -> Unit,
) {
    var isThumbsUp by remember { mutableStateOf<Boolean?>(true) }
    var starRating by remember { mutableStateOf(5) }
    var feedbackText by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = Color(0xFF181824)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header Title
            Text(
                text = "Tell us what you think",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            // Q1: What do you think of MusclePlay? + Thumbs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "What do you think of MusclePlay?",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Thumbs Up
                    IconButton(
                        onClick = { isThumbsUp = true },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (isThumbsUp == true) OrangePrimary
                                else Color.White.copy(alpha = 0.1f)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = "Thumbs Up",
                            tint = Color.White
                        )
                    }

                    // Thumbs Down
                    IconButton(
                        onClick = { isThumbsUp = false },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (isThumbsUp == false) Color(0xFFFF5252)
                                else Color.White.copy(alpha = 0.1f)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ThumbDown,
                            contentDescription = "Thumbs Down",
                            tint = Color.White
                        )
                    }
                }
            }

            // Q2: How satisfied are you with this experience? + Stars
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "How satisfied are you with this experience?",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..5).forEach { starIndex ->
                        Icon(
                            imageVector = if (starIndex <= starRating) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Star $starIndex",
                            tint = if (starIndex <= starRating) OrangeSecondary else Color.White.copy(
                                alpha = 0.3f
                            ),
                            modifier = Modifier
                                .size(36.dp)
                                .clickable { starRating = starIndex }
                        )
                    }
                }
            }

            // Q3: How can we improve your experience? optional
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row {
                    Text(
                        text = "How can we improve your experience? ",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "optional",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }

                OutlinedTextField(
                    value = feedbackText,
                    onValueChange = { if (it.length <= 1000) feedbackText = it },
                    placeholder = {
                        Text(
                            "Share details about your experience...",
                            color = Color.White.copy(alpha = 0.4f)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = OrangePrimary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                    ),
                    maxLines = 4
                )

                Text(
                    text = "${feedbackText.length}/1,000",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.align(Alignment.End)
                )
            }

            // Send Button
            Button(
                onClick = { onSubmit(isThumbsUp, starRating, feedbackText) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
            ) {
                Text(
                    text = "Send",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Footer Disclaimer
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.06f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Don't include personal information like your name, address, or phone number.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "By selecting Send, you give MusclePlay the right to use your feedback to improve its services.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileStatBadge(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = OrangeSecondary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun GlassProgressGallerySection(
    photos: List<ProgressPhoto>,
    onImageClick: (ProgressPhoto) -> Unit,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        items(photos) { photo ->
            GlassContainer(
                modifier = Modifier
                    .width(140.dp)
                    .height(160.dp)
                    .clickable { onImageClick(photo) },
                shape = RoundedCornerShape(20.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = OrangePrimary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = photo.title,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
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
    GlassContainer(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = tint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
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
private fun GlassContainer(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(24.dp),
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(shape)
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
                shape = shape
            )
    ) {
        content()
    }
}
