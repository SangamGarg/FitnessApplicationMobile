package com.muscleplay.app.feature.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

private val OrangePrimary = Color(0xFFFF5722)
private val OrangeSecondary = Color(0xFFFF9800)
private val OrangeGlow = Color(0xFFFF3D00)
private val DarkBackground = Color(0xFF0F0F17)

private val ActivityHeaderRowHeight = 40.dp

data class DayStreakInfo(
    val dayName: String,
    val dateNum: Int,
    val isCompleted: Boolean,
    val isToday: Boolean,
    val caloriesBurned: Int,
    val stepsTaken: Int,
)

@Composable
fun HomeScreen(
    onNavigateToDiet: () -> Unit = {},
    onNavigateToWorkouts: () -> Unit = {},
    onNavigateToGames: () -> Unit = {},
    onNavigateToCalculators: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToScanner: () -> Unit = {},
    onNavigateToCoinsHub: () -> Unit = {},
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    var currentWaterMl by remember { mutableIntStateOf(1750) }
    var isFabMenuExpanded by remember { mutableStateOf(false) }

    val density = LocalDensity.current
    val expandedCardHeight = 240.dp
    val collapsedCardHeight = 92.dp
    val headerTopPadding = 24.dp
    val headerCardSpacing = 20.dp

    val cardRangePx = with(density) { (expandedCardHeight - collapsedCardHeight).toPx() }
    val spacingPx = with(density) { headerCardSpacing.toPx() }

    var headerBlockPx by remember { mutableFloatStateOf(with(density) { 160.dp.toPx() }) }

    val totalRangePx = headerBlockPx + cardRangePx
    val currentTotalRange by rememberUpdatedState(totalRangePx)

    var collapsedPx by remember { mutableFloatStateOf(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                if (delta >= 0f) return Offset.Zero
                val old = collapsedPx
                collapsedPx = (old - delta).coerceIn(0f, currentTotalRange)
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
                collapsedPx = (old - delta).coerceIn(0f, currentTotalRange)
                return Offset(0f, old - collapsedPx)
            }
        }
    }

    val currentCollapsedPx = collapsedPx.coerceIn(0f, totalRangePx)
    val headerShiftPx = currentCollapsedPx.coerceAtMost(headerBlockPx)
    val cardCollapseProgress = if (cardRangePx > 0f) {
        ((currentCollapsedPx - headerBlockPx) / cardRangePx).coerceIn(0f, 1f)
    } else 0f
    val cardHeight = lerp(expandedCardHeight, collapsedCardHeight, cardCollapseProgress)
    val topSectionHeight = with(density) { (headerBlockPx - headerShiftPx).toDp() } + cardHeight

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(OrangeGlow.copy(alpha = pulseAlpha), Color.Transparent),
                    center = Offset(canvasWidth * 0.85f, canvasHeight * 0.15f),
                    radius = canvasWidth * 0.7f * pulseScale
                ),
                center = Offset(canvasWidth * 0.85f, canvasHeight * 0.15f),
                radius = canvasWidth * 0.7f * pulseScale
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        OrangeSecondary.copy(alpha = pulseAlpha * 0.7f),
                        Color.Transparent
                    ),
                    center = Offset(canvasWidth * 0.15f, canvasHeight * 0.75f),
                    radius = canvasWidth * 0.6f
                ),
                center = Offset(canvasWidth * 0.15f, canvasHeight * 0.75f),
                radius = canvasWidth * 0.6f
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .nestedScroll(nestedScrollConnection)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(topSectionHeight)
                    .clipToBounds()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(Alignment.Top, unbounded = true)
                        .offset { IntOffset(0, -headerShiftPx.roundToInt()) }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onSizeChanged { headerBlockPx = it.height + spacingPx }
                            .padding(top = headerTopPadding)
                    ) {
                        GlassHeaderSection(
                            onNavigateToProfile = onNavigateToProfile
                        )
                    }

                    Spacer(modifier = Modifier.height(headerCardSpacing))

                    GlassActivityCard(
                        collapseProgress = cardCollapseProgress,
                        expandedHeight = expandedCardHeight,
                        collapsedHeight = collapsedCardHeight
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
            ) {
                // 1. Dynamic 7-Day Streak Calendar Row
                item {
                    GlassStreakCalendarRow()
                }

                // 2. Quick-Log Glass Hydration Tracker Card
                item {
                    GlassHydrationCard(
                        currentWaterMl = currentWaterMl,
                        onAddWater = { ml ->
                            currentWaterMl = (currentWaterMl + ml).coerceAtMost(4000)
                        }
                    )
                }

                // 3. Smart AI Coach Daily Insight & Recommendation Card
                item {
                    GlassAiCoachInsightCard(
                        onStartWorkout = onNavigateToWorkouts
                    )
                }

                // 4. 3D Muscle Recovery Heatmap Summary Card
                item {
                    GlassMuscleRecoveryCard(
                        onOpenHeatmap = onNavigateToWorkouts
                    )
                }

                // 5. Featured Workout Card
                item {
                    GlassFeaturedWorkoutCard(
                        onStartWorkout = onNavigateToWorkouts
                    )
                }

                // 6. Daily MP Coin Challenge & Leaderboard Preview
                item {
                    GlassDailyCoinChallengeCard(
                        onEnterChallenge = onNavigateToGames
                    )
                }

                // 7. Explore & Play Quick Actions Grid
                item {
                    Text(
                        text = "Explore & Play",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            GlassQuickActionCard(
                                title = "Diet & AI Chef",
                                subtitle = "Smart Recipes & Scan",
                                icon = Icons.Default.RestaurantMenu,
                                gradientColors = listOf(OrangePrimary, OrangeGlow),
                                modifier = Modifier.weight(1f),
                                onClick = onNavigateToDiet
                            )

                            GlassQuickActionCard(
                                title = "Fitness Games",
                                subtitle = "Camera Rep Challenges",
                                icon = Icons.Default.Gamepad,
                                gradientColors = listOf(OrangePrimary, OrangeSecondary),
                                modifier = Modifier.weight(1f),
                                onClick = onNavigateToGames
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            GlassQuickActionCard(
                                title = "Workouts",
                                subtitle = "AI Pose & Exercises",
                                icon = Icons.Default.FitnessCenter,
                                gradientColors = listOf(OrangeGlow, OrangePrimary),
                                modifier = Modifier.weight(1f),
                                onClick = onNavigateToWorkouts
                            )

                            GlassQuickActionCard(
                                title = "Calculators",
                                subtitle = "BMI, BMR & Calorie",
                                icon = Icons.Default.Calculate,
                                gradientColors = listOf(OrangeSecondary, Color(0xFFFFC107)),
                                modifier = Modifier.weight(1f),
                                onClick = onNavigateToCalculators
                            )
                        }
                    }
                }

                // Motivational Footer Glass Card
                item {
                    GlassMotivationalCard()
                }
            }
        }

        // Speed-Logging Glass Floating Hub (FAB)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 20.dp)
        ) {
            GlassFloatingActionHub(
                isExpanded = isFabMenuExpanded,
                onToggleExpand = { isFabMenuExpanded = !isFabMenuExpanded },
                onNavigateToScanner = {
                    isFabMenuExpanded = false
                    onNavigateToScanner()
                },
                onAddWaterQuick = {
                    currentWaterMl = (currentWaterMl + 250).coerceAtMost(4000)
                    isFabMenuExpanded = false
                },
                onNavigateToCoinsHub = {
                    isFabMenuExpanded = false
                    onNavigateToCoinsHub()
                }
            )
        }
    }
}

// 1. Dynamic 7-Day Streak Calendar Row Component (Fixes Image 1)
@Composable
private fun GlassStreakCalendarRow() {
    var selectedDayInfo by remember { mutableStateOf<DayStreakInfo?>(null) }

    val daysList = remember {
        listOf(
            DayStreakInfo(
                "Mon",
                13,
                isCompleted = true,
                isToday = false,
                caloriesBurned = 450,
                stepsTaken = 7200
            ),
            DayStreakInfo(
                "Tue",
                14,
                isCompleted = true,
                isToday = false,
                caloriesBurned = 510,
                stepsTaken = 8100
            ),
            DayStreakInfo(
                "Wed",
                15,
                isCompleted = true,
                isToday = false,
                caloriesBurned = 380,
                stepsTaken = 6400
            ),
            DayStreakInfo(
                "Thu",
                16,
                isCompleted = true,
                isToday = false,
                caloriesBurned = 420,
                stepsTaken = 6420
            ),
            DayStreakInfo(
                "Fri",
                17,
                isCompleted = true,
                isToday = true,
                caloriesBurned = 420,
                stepsTaken = 6420
            ),
            DayStreakInfo(
                "Sat",
                18,
                isCompleted = false,
                isToday = false,
                caloriesBurned = 0,
                stepsTaken = 0
            ),
            DayStreakInfo(
                "Sun",
                19,
                isCompleted = false,
                isToday = false,
                caloriesBurned = 0,
                stepsTaken = 0
            )
        )
    }

    GlassContainer(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(OrangePrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = OrangeSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            "ACTIVE STREAK",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = OrangePrimary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            "12 Days & Counting 🔥",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Surface(
                    color = OrangeSecondary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        "2 Freezes ❄️",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = OrangeSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        maxLines = 1
                    )
                }
            }

            // 7 Days Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                daysList.forEach { day ->
                    Surface(
                        onClick = { selectedDayInfo = day },
                        color = when {
                            day.isToday -> OrangePrimary
                            day.isCompleted -> Color.White.copy(alpha = 0.12f)
                            else -> Color.White.copy(alpha = 0.04f)
                        },
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (day.isToday) OrangeSecondary else if (day.isCompleted) OrangePrimary.copy(
                                alpha = 0.4f
                            ) else Color.White.copy(alpha = 0.1f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                day.dayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (day.isToday) Color.White else Color.White.copy(alpha = 0.6f),
                                fontSize = 10.sp
                            )
                            Text(
                                "${day.dateNum}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                            Icon(
                                imageVector = if (day.isCompleted) Icons.Default.LocalFireDepartment else Icons.Default.Circle,
                                contentDescription = null,
                                tint = if (day.isCompleted) (if (day.isToday) Color.White else OrangeSecondary) else Color.White.copy(
                                    alpha = 0.2f
                                ),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    selectedDayInfo?.let { info ->
        AlertDialog(
            onDismissRequest = { selectedDayInfo = null },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color(0xFF181824),
            icon = {
                Icon(
                    Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = OrangeSecondary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    "${info.dayName}, ${info.dateNum} Summary",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Burned Calories: ${info.caloriesBurned} kcal", color = Color.White)
                    Text("Steps Recorded: ${info.stepsTaken} steps", color = Color.White)
                    Text(
                        "Status: ${if (info.isCompleted) "Completed 🔥" else "Rest Day"}",
                        fontWeight = FontWeight.Bold,
                        color = OrangeSecondary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedDayInfo = null },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

// 2. Quick-Log Glass Hydration Tracker (Fixes Image 2)
@Composable
private fun GlassHydrationCard(
    currentWaterMl: Int,
    onAddWater: (Int) -> Unit,
) {
    val targetWaterMl = 3000
    val progressFraction = (currentWaterMl.toFloat() / targetWaterMl).coerceIn(0f, 1f)

    GlassContainer(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2196F3).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "HYDRATION TRACKER",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2196F3),
                        letterSpacing = 1.2.sp,
                        maxLines = 1
                    )
                }

                Text(
                    text = "${(currentWaterMl / 1000.0).toString().take(3)}L / 3.0L Target",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    maxLines = 1
                )
            }

            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF2196F3),
                trackColor = Color.White.copy(alpha = 0.1f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onAddWater(250) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3).copy(
                                alpha = 0.25f
                            )
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "+250ml",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Button(
                        onClick = { onAddWater(500) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "+500ml",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// 3. Smart AI Coach Daily Insight Card (Fixes Image 3)
@Composable
private fun GlassAiCoachInsightCard(
    onStartWorkout: () -> Unit,
) {
    GlassContainer(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
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
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = OrangeSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "AI COACH DAILY INSIGHT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = OrangeSecondary,
                        letterSpacing = 1.sp,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    color = Color(0xFF4CAF50).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "100% Legs Fresh",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        maxLines = 1
                    )
                }
            }

            Text(
                text = "💡 Your Legs & Quads are 100% recovered today! Ideal time for the Lower Body & Squat Blitz challenge.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f),
                lineHeight = 20.sp
            )

            Button(
                onClick = onStartWorkout,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                modifier = Modifier.fillMaxWidth().height(44.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        "Start Recommended Workout",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// 4. Daily MP Coin Challenge & Leaderboard Preview Card (Fixes Image 4)
@Composable
private fun GlassDailyCoinChallengeCard(
    onEnterChallenge: () -> Unit,
) {
    GlassContainer(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = OrangeSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        "DAILY MP COIN CHALLENGE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = OrangePrimary,
                        letterSpacing = 1.sp,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    color = OrangeSecondary,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "+50 MP Reward",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        maxLines = 1
                    )
                }
            }

            Text(
                "🔥 30 Push-Ups Liveness Challenge",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            // Mini Leaderboard Preview Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LeaderboardBadge("🥇 Rahul S.", "150 MP", Modifier.weight(1f))
                LeaderboardBadge("🥈 Priya K.", "120 MP", Modifier.weight(1f))
                LeaderboardBadge("🥉 Alex F.", "90 MP", Modifier.weight(1f))
            }

            Button(
                onClick = onEnterChallenge,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                modifier = Modifier.fillMaxWidth().height(44.dp)
            ) {
                Text("Enter Challenge ▶", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
private fun LeaderboardBadge(name: String, score: String, modifier: Modifier = Modifier) {
    Surface(
        color = Color.White.copy(alpha = 0.08f),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                name,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1
            )
            Text(
                score,
                style = MaterialTheme.typography.labelSmall,
                color = OrangeSecondary,
                maxLines = 1
            )
        }
    }
}

// 5. Speed-Logging Glass Floating Hub (FAB)
@Composable
private fun GlassFloatingActionHub(
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onNavigateToScanner: () -> Unit,
    onAddWaterQuick: () -> Unit,
    onNavigateToCoinsHub: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FabActionPill("📸 Scan Meal", Icons.Default.CameraAlt, onNavigateToScanner)
                FabActionPill("💧 Add Water (+250ml)", Icons.Default.WaterDrop, onAddWaterQuick)
                FabActionPill("🪙 MP Coins Hub", Icons.Default.MonetizationOn, onNavigateToCoinsHub)
            }
        }

        FloatingActionButton(
            onClick = onToggleExpand,
            containerColor = OrangePrimary,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.Add,
                contentDescription = "Quick Hub"
            )
        }
    }
}

@Composable
private fun FabActionPill(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        color = Color(0xFF181824),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, OrangePrimary.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = OrangeSecondary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun GlassHeaderSection(
    onNavigateToProfile: () -> Unit = {},
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Surface(
                color = OrangePrimary.copy(alpha = 0.2f),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    OrangePrimary.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(OrangePrimary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "MUSCLEPLAY",
                        style = MaterialTheme.typography.labelMedium,
                        color = OrangePrimary,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Let's crush it! 🔥",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Text(
                text = "Your daily fitness streak is waiting.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
        }

        GlassContainer(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .clickable(onClick = onNavigateToProfile)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = OrangePrimary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun GlassActivityCard(
    collapseProgress: Float,
    expandedHeight: Dp,
    collapsedHeight: Dp,
) {
    val p = collapseProgress

    val cardHeight = lerp(expandedHeight, collapsedHeight, p)
    val cardPadding = lerp(22.dp, 12.dp, p)
    val cornerRadius = lerp(28.dp, 20.dp, p)

    val headerHeight = ActivityHeaderRowHeight * (1f - p)
    val headerGap = 20.dp * (1f - p)
    val headerAlpha = (1f - p * 2.5f).coerceIn(0f, 1f)

    val contentRowHeight = lerp(130.dp, 64.dp, p)
    val ringSize = lerp(110.dp, 64.dp, p)
    val ringStroke = lerp(12.dp, 6.dp, p)
    val ringStatsGap = lerp(12.dp, 8.dp, p)

    val expandedStatsAlpha = (1f - p * 2f).coerceIn(0f, 1f)
    val compactStatsAlpha = (p * 2f - 1f).coerceIn(0f, 1f)

    GlassContainer(
        modifier = Modifier
            .fillMaxWidth()
            .height(cardHeight),
        shape = RoundedCornerShape(cornerRadius)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(cardPadding),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(headerHeight)
                    .clipToBounds()
                    .graphicsLayer { this.alpha = headerAlpha }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(Alignment.Top, unbounded = true),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "DAILY GOAL PROGRESS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = OrangePrimary,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = "Active & Moving",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Surface(
                        color = Color.White.copy(alpha = 0.1f),
                        shape = CircleShape
                    ) {
                        Text(
                            text = "78%",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = OrangeSecondary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(headerGap))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(contentRowHeight),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ActivityRing(
                    ringSize = ringSize,
                    strokeWidth = ringStroke,
                    collapseProgress = p
                )

                Spacer(modifier = Modifier.width(ringStatsGap))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .wrapContentHeight(Alignment.CenterVertically, unbounded = true)
                            .graphicsLayer { this.alpha = expandedStatsAlpha },
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        ActivityStatRow(
                            icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                            value = "6,420",
                            unit = "steps",
                            color = OrangeSecondary
                        )

                        ActivityStatRow(
                            icon = Icons.Default.Timer,
                            value = "35",
                            unit = "active min",
                            color = OrangePrimary
                        )

                        ActivityStatRow(
                            icon = Icons.Default.FitnessCenter,
                            value = "3",
                            unit = "workouts done",
                            color = Color(0xFFFFB74D)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer { this.alpha = compactStatsAlpha },
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CompactActivityStat(
                            icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                            value = "6,420",
                            label = "steps",
                            color = OrangeSecondary
                        )
                        CompactActivityStat(
                            icon = Icons.Default.Timer,
                            value = "35",
                            label = "min",
                            color = OrangePrimary
                        )
                        CompactActivityStat(
                            icon = Icons.Default.FitnessCenter,
                            value = "3",
                            label = "workouts",
                            color = Color(0xFFFFB74D)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityRing(
    ringSize: Dp,
    strokeWidth: Dp,
    collapseProgress: Float,
) {
    val expandedContentAlpha = (1f - collapseProgress * 2f).coerceIn(0f, 1f)
    val compactContentAlpha = (collapseProgress * 2f - 1f).coerceIn(0f, 1f)

    Box(
        modifier = Modifier.size(ringSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            val arcSize = Size(size.width - strokePx, size.height - strokePx)

            drawArc(
                color = Color.White.copy(alpha = 0.1f),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = Offset(strokePx / 2, strokePx / 2),
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            drawArc(
                brush = Brush.horizontalGradient(listOf(OrangePrimary, OrangeSecondary)),
                startAngle = 135f,
                sweepAngle = 210f,
                useCenter = false,
                topLeft = Offset(strokePx / 2, strokePx / 2),
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }

        Column(
            modifier = Modifier.graphicsLayer { this.alpha = expandedContentAlpha },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = null,
                tint = OrangePrimary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "420",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = "kcal",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.6f)
            )
        }

        Column(
            modifier = Modifier.graphicsLayer { this.alpha = compactContentAlpha },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "420",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = "kcal",
                fontSize = 9.sp,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun ActivityStatRow(
    icon: ImageVector,
    value: String,
    unit: String,
    color: Color,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun CompactActivityStat(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.6f),
            maxLines = 1
        )
    }
}

@Composable
private fun GlassFeaturedWorkoutCard(
    onStartWorkout: () -> Unit = {},
) {
    GlassContainer(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(OrangePrimary.copy(alpha = 0.15f), Color.Transparent)
                    )
                )
            }

            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = OrangePrimary,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "TODAY'S WORKOUT",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = "15 Mins • Intermediate",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                Text(
                    text = "💪 Lower Body & Squat Blitz",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Text(
                    text = "High-energy AI pose tracking challenge to build core strength and leg endurance.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )

                Button(
                    onClick = onStartWorkout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrangePrimary
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Start Workout Now",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GlassQuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    GlassContainer(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(gradientColors)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun GlassMuscleRecoveryCard(
    onOpenHeatmap: () -> Unit,
) {
    GlassContainer(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenHeatmap),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(OrangePrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AccessibilityNew,
                            contentDescription = null,
                            tint = OrangeSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "MUSCLE RECOVERY HEATMAP",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = OrangePrimary,
                            letterSpacing = 1.sp,
                            maxLines = 1
                        )
                        Text(
                            text = "Chest & Triceps Strained 🔥",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                item { MuscleRecoveryPill("Chest", "High Strain", OrangePrimary) }
                item { MuscleRecoveryPill("Triceps", "High Strain", OrangePrimary) }
                item { MuscleRecoveryPill("Quads", "Fresh", Color(0xFF4CAF50)) }
            }
        }
    }
}

@Composable
private fun MuscleRecoveryPill(muscle: String, status: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(color))
            Text(
                text = "$muscle ($status)",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun GlassMotivationalCard() {
    GlassContainer(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "⚡ Fuel your fire. One rep at a time.",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = OrangeSecondary
            )
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
