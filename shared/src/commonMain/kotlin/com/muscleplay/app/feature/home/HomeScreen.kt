package com.muscleplay.app.feature.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muscleplay.app.core.network.auth.UserProfile
import com.muscleplay.app.core.network.dto.DashboardResponseDto
import com.muscleplay.app.feature.home.data.DashboardRepository
import kotlinx.coroutines.launch

private val OrangePrimary = Color(0xFFFF5722)
private val OrangeSecondary = Color(0xFFFF9800)
private val OrangeGlow = Color(0xFFFF3D00)
private val DarkBackground = Color(0xFF0F0F17)
private val CardBackground = Color(0xFF181824)
private val CardBorderColor = Color(0xFF2A283A)
private val CyanAccent = Color(0xFF00E5FF)
private val GreenAccent = Color(0xFF00E676)
private val BlueAccent = Color(0xFF2979FF)
private val TextMuted = Color(0xFF9E9B9E)

@Composable
fun HomeScreen(
    dashboardRepository: DashboardRepository? = null,
    userSessionRepository: com.muscleplay.app.core.network.auth.UserSessionRepository? = null,
    notificationRepository: com.muscleplay.app.core.network.notification.NotificationRepository? = null,
    onNavigateToDiet: () -> Unit = {},
    onNavigateToWorkouts: () -> Unit = {},
    onNavigateToGames: () -> Unit = {},
    onNavigateToCalculators: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToScanner: () -> Unit = {},
    onNavigateToCoinsHub: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
) {
    val coroutineScope = rememberCoroutineScope()
    val sessionProfile by (userSessionRepository?.userProfile?.collectAsState()
        ?: remember { mutableStateOf(null) })

    val viewModel = remember(dashboardRepository) {
        dashboardRepository?.let { DashboardViewModel(it, coroutineScope) }
    }

    val uiState by (viewModel?.uiState?.collectAsState() ?: remember {
        mutableStateOf<DashboardUiState>(DashboardUiState.Success(createSampleDashboardDto()))
    })

    LaunchedEffect(Unit) {
        viewModel?.loadDashboard(isRefresh = false)
    }

    com.muscleplay.app.core.network.notification.RequestNotificationPermissionEffect(
        onRequestCompleted = {
            coroutineScope.launch {
                notificationRepository?.registerCurrentDevice()
            }
        })

    Box(
        modifier = Modifier.fillMaxSize().background(DarkBackground)
    ) {
        // Ambient background gradient glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(OrangeGlow.copy(alpha = 0.25f), Color.Transparent),
                    center = Offset(w * 0.85f, h * 0.15f),
                    radius = w * 0.8f
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(OrangeSecondary.copy(alpha = 0.15f), Color.Transparent),
                    center = Offset(w * 0.15f, h * 0.75f),
                    radius = w * 0.7f
                )
            )
        }

        Scaffold(
            containerColor = Color.Transparent
        ) { innerPadding ->
            when (val state = uiState) {
                is DashboardUiState.Loading -> {
                    DashboardLoadingSkeleton(
                        modifier = Modifier.fillMaxSize().padding(innerPadding)
                    )
                }

                is DashboardUiState.Error -> {
                    DashboardErrorScreen(
                        message = "Unable to load dashboard data right now. Please check your connection and try again.",
                        onRetry = { viewModel?.loadDashboard() },
                        modifier = Modifier.fillMaxSize().padding(innerPadding)
                    )
                }

                is DashboardUiState.Empty -> {
                    DashboardErrorScreen(
                        message = "Unable to load dashboard data right now. Please check your connection and try again.",
                        onRetry = { viewModel?.loadDashboard() },
                        modifier = Modifier.fillMaxSize().padding(innerPadding)
                    )
                }

                is DashboardUiState.Success -> {
                    val data = state.data
                    DashboardContent(
                        data = data,
                        sessionProfile = sessionProfile,
                        onNavigateToDiet = onNavigateToDiet,
                        onNavigateToWorkouts = onNavigateToWorkouts,
                        onNavigateToGames = onNavigateToGames,
                        onNavigateToCalculators = onNavigateToCalculators,
                        onNavigateToProfile = onNavigateToProfile,
                        onNavigateToNotifications = onNavigateToNotifications,
                        modifier = Modifier.fillMaxSize().padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardContent(
    data: DashboardResponseDto,
    sessionProfile: UserProfile?,
    onNavigateToDiet: () -> Unit,
    onNavigateToWorkouts: () -> Unit,
    onNavigateToGames: () -> Unit,
    onNavigateToCalculators: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val userName = sessionProfile?.name ?: data.user?.name ?: "Fitness Freak"
    val unreadNotifications = data.notifications?.unreadCount ?: 0L

    LazyColumn(
        modifier = modifier, contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp
        ), verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Top Header Row
        item {
            TopHeaderRow(
                unreadCount = unreadNotifications,
                onNotificationClick = onNavigateToNotifications,
                onProfileClick = onNavigateToProfile
            )
        }

        // 2. Greeting Section
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Hello, $userName! 👋",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    text = "Stronger today. Healthier tomorrow.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
        }

        // 3. Weekly Goal Hero Banner Card
        item {
            WeeklyGoalCard(
                activity = data.activity, onViewDetails = onNavigateToWorkouts
            )
        }

        // 4. Quick Action Grid (2x3)
        item {
            QuickActionsGrid(
                onStartWorkout = onNavigateToWorkouts,
                onAiCoach = onNavigateToProfile,
                onNutrition = onNavigateToDiet,
                onFitnessGames = onNavigateToGames,
                onCalculators = onNavigateToCalculators,
                onChatbot = onNavigateToProfile
            )
        }

        // 5. Your Progress Section
        item {
            YourProgressSection(
                activity = data.activity,
                workoutAnalytics = data.workoutAnalytics,
                onSeeAll = onNavigateToWorkouts
            )
        }

        // 6. Personal Records Section
        item {
            PersonalRecordsSection(onSeeAll = onNavigateToWorkouts)
        }

        // 7. Today's Challenge & Your Nutrition Bottom Row Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                TodaysChallengeCard(
                    recommendedWorkout = data.recommendedWorkout,
                    onStart = onNavigateToGames,
                    modifier = Modifier.weight(1f)
                )
                YourNutritionCard(
                    nutrition = data.nutrition,
                    onNutritionClick = onNavigateToDiet,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TopHeaderRow(
    unreadCount: Long,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Logo
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(
                    Brush.linearGradient(
                        listOf(OrangePrimary, OrangeSecondary)
                    )
                ), contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = "MusclePlay Logo",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column {
                Row {
                    Text(
                        text = "Muscle",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "Play",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = OrangePrimary
                    )
                }
                Text(
                    text = "Train • Track • Transform",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }
        }

        // Notification Bell + User Avatar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box {
                IconButton(
                    onClick = onNotificationClick,
                    modifier = Modifier.size(42.dp).clip(CircleShape).background(CardBackground)
                        .border(1.dp, CardBorderColor, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                if (unreadCount > 0) {
                    Box(
                        modifier = Modifier.size(12.dp).clip(CircleShape).background(OrangePrimary)
                            .align(Alignment.TopEnd)
                    )
                }
            }

            Box(
                modifier = Modifier.size(42.dp).clip(CircleShape)
                    .background(OrangeSecondary.copy(alpha = 0.3f))
                    .border(1.5.dp, OrangePrimary, CircleShape).clickable { onProfileClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun WeeklyGoalCard(
    activity: com.muscleplay.app.core.network.dto.ActivitySummaryResponseDto?,
    onViewDetails: () -> Unit,
) {
    val progressPercent = activity?.goalProgressPercent ?: 78
    val completedWorkouts = activity?.completedActivities ?: 4L
    val targetWorkouts = 5L

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = CardBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Circular Ring Gauge
            Box(
                modifier = Modifier.size(76.dp), contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 8.dp.toPx()
                    drawCircle(
                        color = Color.White.copy(alpha = 0.1f), style = Stroke(width = strokeWidth)
                    )
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(CyanAccent, GreenAccent, CyanAccent)
                        ),
                        startAngle = -90f,
                        sweepAngle = 360f * (progressPercent / 100f),
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                Text(
                    text = "$progressPercent%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Weekly Goal",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$completedWorkouts / $targetWorkouts Workouts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            Button(
                onClick = onViewDetails,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "View Details",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionsGrid(
    onStartWorkout: () -> Unit,
    onAiCoach: () -> Unit,
    onNutrition: () -> Unit,
    onFitnessGames: () -> Unit,
    onCalculators: () -> Unit,
    onChatbot: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionGridItem(
                icon = Icons.Default.PlayArrow,
                label = "Start Workout",
                iconColor = GreenAccent,
                onClick = onStartWorkout,
                modifier = Modifier.weight(1f)
            )
            ActionGridItem(
                icon = Icons.Default.SmartToy,
                label = "AI Coach",
                iconColor = OrangePrimary,
                onClick = onAiCoach,
                modifier = Modifier.weight(1f)
            )
            ActionGridItem(
                icon = Icons.Default.Fastfood,
                label = "Nutrition",
                iconColor = OrangeSecondary,
                onClick = onNutrition,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionGridItem(
                icon = Icons.Default.Gamepad,
                label = "Fitness Games",
                iconColor = BlueAccent,
                onClick = onFitnessGames,
                modifier = Modifier.weight(1f)
            )
            ActionGridItem(
                icon = Icons.Default.Calculate,
                label = "Calculators",
                iconColor = CyanAccent,
                onClick = onCalculators,
                modifier = Modifier.weight(1f)
            )
            ActionGridItem(
                icon = Icons.AutoMirrored.Filled.Chat,
                label = "Chatbot",
                iconColor = GreenAccent,
                onClick = onChatbot,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ActionGridItem(
    icon: ImageVector,
    label: String,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = CardBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor),
        modifier = modifier.height(95.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.2f)), contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun YourProgressSection(
    activity: com.muscleplay.app.core.network.dto.ActivitySummaryResponseDto?,
    workoutAnalytics: com.muscleplay.app.core.network.dto.WorkoutAnalyticsOverviewResponse?,
    onSeeAll: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeader(title = "Your Progress", onSeeAll = onSeeAll)

        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CardBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Line Chart (Left 65%)
                Box(
                    modifier = Modifier.weight(1.8f).height(140.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val points = listOf(
                            Offset(0f, h * 0.7f),
                            Offset(w * 0.16f, h * 0.55f),
                            Offset(w * 0.33f, h * 0.4f),
                            Offset(w * 0.50f, h * 0.6f),
                            Offset(w * 0.66f, h * 0.5f),
                            Offset(w * 0.83f, h * 0.25f),
                            Offset(w, h * 0.35f)
                        )

                        val path = Path().apply {
                            moveTo(points[0].x, points[0].y)
                            for (i in 1 until points.size) {
                                val p0 = points[i - 1]
                                val p1 = points[i]
                                cubicTo(
                                    (p0.x + p1.x) / 2, p0.y, (p0.x + p1.x) / 2, p1.y, p1.x, p1.y
                                )
                            }
                        }

                        drawPath(
                            path = path, brush = Brush.horizontalGradient(
                                listOf(
                                    OrangePrimary, OrangeSecondary
                                )
                            ), style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Glowing dots
                        points.forEach { pt ->
                            drawCircle(color = OrangePrimary, radius = 5.dp.toPx(), center = pt)
                            drawCircle(color = Color.White, radius = 2.dp.toPx(), center = pt)
                        }
                    }

                    // Days Row
                    Row(
                        modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                            Text(
                                text = day,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontSize = 9.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Stats Stack (Right 35%)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val workoutsCount = workoutAnalytics?.totalWorkouts ?: 12L
                    val calories = activity?.caloriesBurned?.toInt() ?: 2800
                    val totalDurationSec = workoutAnalytics?.totalDurationSeconds ?: 23400L
                    val durationHoursStr = "${totalDurationSec / 3600}h"

                    ProgressStatItem(
                        icon = Icons.Default.FitnessCenter,
                        value = "$workoutsCount",
                        label = "Workouts",
                        iconColor = OrangePrimary
                    )
                    ProgressStatItem(
                        icon = Icons.Default.LocalFireDepartment,
                        value = if (calories >= 1000) "${calories / 1000}k" else "$calories",
                        label = "Calories",
                        iconColor = OrangeSecondary
                    )
                    ProgressStatItem(
                        icon = Icons.Default.Timer,
                        value = durationHoursStr,
                        label = "Total Time",
                        iconColor = BlueAccent
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressStatItem(
    icon: ImageVector,
    value: String,
    label: String,
    iconColor: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier.size(30.dp).clip(CircleShape)
                .background(iconColor.copy(alpha = 0.2f)), contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(16.dp)
            )
        }
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun PersonalRecordsSection(onSeeAll: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeader(
            title = "Personal Records", onSeeAll = onSeeAll, icon = Icons.Default.EmojiEvents
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            item {
                RecordItemCard("Max Weight", "100 kg", Icons.Default.FitnessCenter)
            }
            item {
                RecordItemCard("Max Reps", "15 reps", Icons.Default.Refresh)
            }
            item {
                RecordItemCard("Best Duration", "1h 20m", Icons.Default.Schedule)
            }
            item {
                RecordItemCard("1RM Estimate", "120 kg", Icons.Default.Analytics)
            }
        }
    }
}

@Composable
private fun RecordItemCard(
    label: String,
    value: String,
    icon: ImageVector,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = CardBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor),
        modifier = Modifier.width(110.dp).height(88.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier.size(28.dp).clip(CircleShape)
                    .background(OrangePrimary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = OrangePrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontSize = 10.sp
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun TodaysChallengeCard(
    recommendedWorkout: com.muscleplay.app.core.network.dto.DashboardResponseDto.WorkoutRecommendation?,
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = CardBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor),
        modifier = modifier.height(135.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.linearGradient(
                    listOf(OrangePrimary.copy(alpha = 0.35f), CardBackground)
                )
            ).padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Today's Challenge",
                        style = MaterialTheme.typography.labelSmall,
                        color = OrangeSecondary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = recommendedWorkout?.name ?: "Complete 5 sets of squats",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2
                    )
                }

                Button(
                    onClick = onStart,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = "Start ->",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun YourNutritionCard(
    nutrition: com.muscleplay.app.core.network.dto.DailyNutritionSummaryResponse?,
    onNutritionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val consumed = nutrition?.consumed?.calories?.toInt() ?: 1850
    val target = nutrition?.target?.dailyCalories?.toInt() ?: 2500

    Surface(
        onClick = onNutritionClick,
        shape = RoundedCornerShape(22.dp),
        color = CardBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor),
        modifier = modifier.height(135.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.linearGradient(
                    listOf(GreenAccent.copy(alpha = 0.15f), CardBackground)
                )
            ).padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f), verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Your Nutrition",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        Surface(
                            color = GreenAccent.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "On Track!",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = GreenAccent,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "$consumed / $target kcal",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Box(
                    modifier = Modifier.size(54.dp), contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.1f), style = Stroke(6.dp.toPx())
                        )
                        drawArc(
                            color = GreenAccent,
                            startAngle = -90f,
                            sweepAngle = 260f,
                            useCenter = false,
                            style = Stroke(6.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Fastfood,
                        contentDescription = null,
                        tint = OrangeSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    onSeeAll: () -> Unit,
    icon: ImageVector? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = OrangePrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }
        TextButton(onClick = onSeeAll) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "See All",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = OrangePrimary
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = OrangePrimary,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
private fun DashboardLoadingSkeleton(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 0.6f, animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing), repeatMode = RepeatMode.Reverse
        ), label = "alpha"
    )

    Column(
        modifier = modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(0.5f).height(30.dp).clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = alpha))
        )
        Box(
            modifier = Modifier.fillMaxWidth().height(110.dp).clip(RoundedCornerShape(24.dp))
                .background(Color.White.copy(alpha = alpha))
        )
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(3) {
                Box(
                    modifier = Modifier.weight(1f).height(90.dp).clip(RoundedCornerShape(18.dp))
                        .background(Color.White.copy(alpha = alpha))
                )
            }
        }
        Box(
            modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(24.dp))
                .background(Color.White.copy(alpha = alpha))
        )
    }
}

@Composable
private fun DashboardErrorScreen(
    message: String = "Unable to load dashboard data right now. Please check your connection and try again.",
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier, contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                tint = OrangePrimary,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Oops! Something went wrong",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Retry", fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun createSampleDashboardDto(): DashboardResponseDto {
    return DashboardResponseDto(
        user = DashboardResponseDto.UserSummary(
            userId = "1", email = "alex@muscleplay.com", name = "Alex", profileCompleted = true
        ),
        activity = com.muscleplay.app.core.network.dto.ActivitySummaryResponseDto(
            activeMinutes = 45,
            caloriesBurned = 2800.0,
            completedActivities = 4,
            dailyGoalMinutes = 60,
            goalProgressPercent = 78
        ),
        nutrition = com.muscleplay.app.core.network.dto.DailyNutritionSummaryResponse(
            target = com.muscleplay.app.core.network.dto.NutritionGoalResponse(
                dailyCalories = 2500.0
            ),
            consumed = com.muscleplay.app.core.network.dto.DailyNutritionSummaryResponse.NutrientTotals(
                calories = 1850.0
            ),
            calorieProgressPercent = 74.0
        ),
        workoutAnalytics = com.muscleplay.app.core.network.dto.WorkoutAnalyticsOverviewResponse(
            totalWorkouts = 12, totalDurationSeconds = 23400, activeDays = 5
        ),
        wallet = DashboardResponseDto.WalletSummary(balanceCoins = 1250),
        notifications = DashboardResponseDto.NotificationSummary(unreadCount = 2),
        recommendedWorkout = DashboardResponseDto.WorkoutRecommendation(
            name = "Complete 5 sets of squats", difficulty = "INTERMEDIATE"
        )
    )
}
