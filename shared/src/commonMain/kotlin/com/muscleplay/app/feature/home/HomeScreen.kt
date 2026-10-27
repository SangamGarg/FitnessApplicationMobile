package com.muscleplay.app.feature.home

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

// Vibrant Orange Theme Palette for Glassmorphism
private val OrangePrimary = Color(0xFFFF5722) // Energetic Vibrant Orange
private val OrangeSecondary = Color(0xFFFF9800) // Warm Amber Orange
private val OrangeGlow = Color(0xFFFF3D00) // Glowing Deep Orange
private val DarkBackground = Color(0xFF0F0F17) // Deep Obsidian Midnight

// Height of the "DAILY GOAL PROGRESS / Active & Moving / 78%" row inside the activity card
private val ActivityHeaderRowHeight = 40.dp

@Composable
fun HomeScreen(
    onNavigateToDiet: () -> Unit = {},
    onNavigateToWorkouts: () -> Unit = {},
    onNavigateToGames: () -> Unit = {},
    onNavigateToCalculators: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    // Ambient glowing pulse animation
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

    // ---- Collapsing top section (header + activity card) ----
    val density = LocalDensity.current
    val expandedCardHeight = 240.dp
    val collapsedCardHeight = 92.dp
    val headerTopPadding = 24.dp
    val headerCardSpacing = 20.dp

    val cardRangePx = with(density) { (expandedCardHeight - collapsedCardHeight).toPx() }
    val spacingPx = with(density) { headerCardSpacing.toPx() }

    // Header block = top padding + greeting header + gap to the card (measured at runtime)
    var headerBlockPx by remember { mutableFloatStateOf(with(density) { 160.dp.toPx() }) }

    // Scroll budget: first the greeting header slides away, then the card shrinks
    val totalRangePx = headerBlockPx + cardRangePx
    val currentTotalRange by rememberUpdatedState(totalRangePx)

    // 0f = fully expanded, totalRangePx = header gone + card fully collapsed
    var collapsedPx by remember { mutableFloatStateOf(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            // Scrolling DOWN the page (finger moves up): collapse the top section first
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                if (delta >= 0f) return Offset.Zero
                val old = collapsedPx
                collapsedPx = (old - delta).coerceIn(0f, currentTotalRange)
                return Offset(0f, -(collapsedPx - old))
            }

            // Scrolling back UP: the list reaches its top first, then the top section expands
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
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
        // Glowing Background Orbs for Glassmorphic Depth Effect
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Top-right Orange Glow Orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(OrangeGlow.copy(alpha = pulseAlpha), Color.Transparent),
                    center = Offset(canvasWidth * 0.85f, canvasHeight * 0.15f),
                    radius = canvasWidth * 0.7f * pulseScale
                ),
                center = Offset(canvasWidth * 0.85f, canvasHeight * 0.15f),
                radius = canvasWidth * 0.7f * pulseScale
            )

            // Bottom-left Amber Glow Orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(OrangeSecondary.copy(alpha = pulseAlpha * 0.7f), Color.Transparent),
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
            // Top section: greeting header slides away, activity card shrinks and stays pinned
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
                    // Header Section
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

                    // Daily Activity Rings Card (collapsible)
                    GlassActivityCard(
                        collapseProgress = cardCollapseProgress,
                        expandedHeight = expandedCardHeight,
                        collapsedHeight = collapsedCardHeight
                    )
                }
            }

            // Scrollable Home Screen View
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(top = 20.dp, bottom = 90.dp)
            ) {
                // Muscle Recovery Status Card
                item {
                    GlassMuscleRecoveryCard(
                        onOpenHeatmap = onNavigateToWorkouts
                    )
                }

                // Featured Workout Card
                item {
                    GlassFeaturedWorkoutCard(
                        onStartWorkout = onNavigateToWorkouts
                    )
                }

                // Explore Quick Actions Grid
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
    }
}

@Composable
private fun GlassHeaderSection(
    onNavigateToProfile: () -> Unit = {}
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
                border = androidx.compose.foundation.BorderStroke(1.dp, OrangePrimary.copy(alpha = 0.5f))
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

        // User Avatar / Streak Badge
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

/**
 * Daily activity card. [collapseProgress] goes from 0f (full card) to 1f (compact card):
 * the title row fades out, the ring shrinks to the left with the kcal inside it, and the
 * steps / active min / workouts stats slide into a compact row on the right.
 */
@Composable
private fun GlassActivityCard(
    collapseProgress: Float,
    expandedHeight: Dp,
    collapsedHeight: Dp
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
            // Title row (fades out and shrinks while collapsing)
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

            // Progress ring (always on the left) + stats (always on the right)
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
                    // Expanded: vertical list of stats
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

                    // Collapsed: compact row of stats
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

/** Glass ring arc with the kcal count inside. Resizes smoothly between expanded and compact. */
@Composable
private fun ActivityRing(
    ringSize: Dp,
    strokeWidth: Dp,
    collapseProgress: Float
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

            // Background track arc
            drawArc(
                color = Color.White.copy(alpha = 0.1f),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = Offset(strokePx / 2, strokePx / 2),
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Glowing Orange active progress arc
            drawArc(
                brush = Brush.horizontalGradient(listOf(OrangePrimary, OrangeSecondary)),
                startAngle = 135f,
                sweepAngle = 210f, // 78% of 270 degrees
                useCenter = false,
                topLeft = Offset(strokePx / 2, strokePx / 2),
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }

        // Expanded center: fire icon + kcal
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

        // Compact center: kcal only
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
    color: Color
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

/** Small vertical stat (icon, value, label) used in the collapsed activity card. */
@Composable
private fun CompactActivityStat(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
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
    onStartWorkout: () -> Unit = {}
) {
    GlassContainer(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Background subtle gradient glow overlay
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
    onClick: () -> Unit = {}
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
    onOpenHeatmap: () -> Unit
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(OrangePrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AccessibilityNew, contentDescription = null, tint = OrangeSecondary, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(
                            text = "MUSCLE RECOVERY HEATMAP",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = OrangePrimary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Chest & Triceps Strained 🔥",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }

                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "View", tint = OrangeSecondary)
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
            Text(text = "$muscle ($status)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 10.sp)
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

/**
 * Reusable Glassmorphism Container with translucent background,
 * subtle white/orange border highlights, and soft frosted depth.
 */
@Composable
fun GlassContainer(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(24.dp),
    content: @Composable () -> Unit
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