package com.muscleplay.app.feature.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private val OrangePrimary = Color(0xFFFF5722)
private val OrangeSecondary = Color(0xFFFF9800)
private val OrangeGlow = Color(0xFFFF3D00)
private val DarkBackground = Color(0xFF0F0F17)

data class OnboardingPageData(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val badgeText: String
)

val onboardingPages = listOf(
    OnboardingPageData(
        title = "AI Pose Tracking & Motion",
        description = "Real-time camera pose estimation tracks your squats, push-ups, and form precision on-device.",
        icon = Icons.Default.AutoAwesome,
        badgeText = "STEP 1 OF 3"
    ),
    OnboardingPageData(
        title = "Visual Progress & Streaks",
        description = "Capture side-by-side transformation photos and maintain your daily workout streak to stay consistent.",
        icon = Icons.Default.LocalFireDepartment,
        badgeText = "STEP 2 OF 3"
    ),
    OnboardingPageData(
        title = "Smart AI Diagnostics & Rewards",
        description = "Get instant health analytics, 5-in-1 fitness calculators, and earn redeemable MP Coins.",
        icon = Icons.Default.Psychology,
        badgeText = "STEP 3 OF 3"
    )
)

@Composable
fun OnboardingScreen(
    onOnboardingFinished: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val coroutineScope = rememberCoroutineScope()

    val infiniteTransition = rememberInfiniteTransition(label = "onboardingPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val targetProgress = when (pagerState.currentPage) {
        0 -> 0.333f
        1 -> 0.666f
        else -> 1.0f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "animatedProgress"
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
                    radius = w * 0.7f
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(OrangeSecondary.copy(alpha = pulseAlpha * 0.6f), Color.Transparent),
                    center = Offset(w * 0.15f, h * 0.85f),
                    radius = w * 0.6f
                )
            )
        }

        Scaffold(
            containerColor = Color.Transparent
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Top Bar: Brand Tag & Skip Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = OrangePrimary.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, OrangePrimary.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "MUSCLEPLAY",
                            style = MaterialTheme.typography.labelSmall,
                            color = OrangePrimary,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            letterSpacing = 1.2.sp
                        )
                    }

                    TextButton(onClick = onOnboardingFinished) {
                        Text(
                            text = "Skip",
                            color = Color.White.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                // Main Onboarding Pager
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) { pageIndex ->
                    val pageData = onboardingPages[pageIndex]
                    OnboardingPageItem(pageIndex = pageIndex, pageData = pageData)
                }

                // Bottom Section: Progress Ring / Get Started Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = pagerState.currentPage == onboardingPages.size - 1,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                        },
                        label = "bottomAction"
                    ) { isLastPage ->
                        if (isLastPage) {
                            Button(
                                onClick = onOnboardingFinished,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp),
                                shape = RoundedCornerShape(27.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Get Started",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    onboardingPages.indices.forEach { index ->
                                        Box(
                                            modifier = Modifier
                                                .size(if (index == pagerState.currentPage) 24.dp else 8.dp, 8.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (index == pagerState.currentPage) OrangePrimary
                                                    else Color.White.copy(alpha = 0.2f)
                                                )
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier.size(76.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val strokeWidth = 4.dp.toPx()
                                        val diameter = size.minDimension - strokeWidth
                                        val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

                                        drawArc(
                                            color = Color.White.copy(alpha = 0.15f),
                                            startAngle = -90f,
                                            sweepAngle = 360f,
                                            useCenter = false,
                                            topLeft = topLeft,
                                            size = Size(diameter, diameter),
                                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                        )

                                        drawArc(
                                            brush = Brush.sweepGradient(
                                                colors = listOf(OrangePrimary, OrangeSecondary, OrangePrimary)
                                            ),
                                            startAngle = -90f,
                                            sweepAngle = 360f * animatedProgress,
                                            useCenter = false,
                                            topLeft = topLeft,
                                            size = Size(diameter, diameter),
                                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                            }
                                        },
                                        modifier = Modifier
                                            .size(58.dp)
                                            .clip(CircleShape)
                                            .background(
                                                brush = Brush.linearGradient(
                                                    colors = listOf(OrangePrimary, OrangeSecondary)
                                                )
                                            )
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = "Next",
                                            tint = Color.White,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageItem(pageIndex: Int, pageData: OnboardingPageData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (pageIndex == 0) {
            // Interactive Animated Skeleton Keypoint Pose Preview on Slide 1
            InteractiveSkeletonPosePreview()
        } else {
            // Hero Icon Circle for Slides 2 and 3
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.12f),
                                Color.White.copy(alpha = 0.04f)
                            )
                        )
                    )
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.4f),
                                OrangePrimary.copy(alpha = 0.35f),
                                Color.White.copy(alpha = 0.1f)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(OrangePrimary, OrangeSecondary)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = pageData.icon,
                        contentDescription = pageData.title,
                        tint = Color.White,
                        modifier = Modifier.size(60.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Step Badge
        Surface(
            color = Color.White.copy(alpha = 0.08f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = pageData.badgeText,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = OrangeSecondary,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Title
        Text(
            text = pageData.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Description
        Text(
            text = pageData.description,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.75f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp),
            lineHeight = 22.sp
        )
    }
}

@Composable
private fun InteractiveSkeletonPosePreview() {
    val infiniteTransition = rememberInfiniteTransition(label = "poseAnim")
    val squatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "squatOffset"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            color = Color.Black.copy(alpha = 0.6f),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4CAF50))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF4CAF50)))
                Text("AI Camera Pose Tracking (99% Accuracy)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
            }
        }

        Box(
            modifier = Modifier
                .width(220.dp)
                .height(180.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Black.copy(alpha = 0.85f))
                .border(1.5.dp, OrangePrimary.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val yShift = squatOffset

                val head = Offset(w * 0.5f, h * 0.20f + yShift)
                val neck = Offset(w * 0.5f, h * 0.30f + yShift)
                val leftShoulder = Offset(w * 0.35f, h * 0.32f + yShift)
                val rightShoulder = Offset(w * 0.65f, h * 0.32f + yShift)
                val leftElbow = Offset(w * 0.25f, h * 0.45f + yShift)
                val rightElbow = Offset(w * 0.75f, h * 0.45f + yShift)
                val spine = Offset(w * 0.5f, h * 0.55f + yShift)
                val leftHip = Offset(w * 0.40f, h * 0.58f + yShift)
                val rightHip = Offset(w * 0.60f, h * 0.58f + yShift)
                val leftKnee = Offset(w * 0.35f, h * 0.75f)
                val rightKnee = Offset(w * 0.65f, h * 0.75f)
                val leftAnkle = Offset(w * 0.35f, h * 0.90f)
                val rightAnkle = Offset(w * 0.65f, h * 0.90f)

                val lineStroke = Stroke(width = 4f, cap = StrokeCap.Round)
                val skeletalColor = Color(0xFF4CAF50)

                // Skeletal Lines
                drawLine(skeletalColor, head, neck, strokeWidth = 4f)
                drawLine(skeletalColor, neck, leftShoulder, strokeWidth = 4f)
                drawLine(skeletalColor, neck, rightShoulder, strokeWidth = 4f)
                drawLine(skeletalColor, leftShoulder, leftElbow, strokeWidth = 4f)
                drawLine(skeletalColor, rightShoulder, rightElbow, strokeWidth = 4f)
                drawLine(skeletalColor, neck, spine, strokeWidth = 4f)
                drawLine(skeletalColor, spine, leftHip, strokeWidth = 4f)
                drawLine(skeletalColor, spine, rightHip, strokeWidth = 4f)
                drawLine(skeletalColor, leftHip, leftKnee, strokeWidth = 4f)
                drawLine(skeletalColor, rightHip, rightKnee, strokeWidth = 4f)
                drawLine(skeletalColor, leftKnee, leftAnkle, strokeWidth = 4f)
                drawLine(skeletalColor, rightKnee, rightAnkle, strokeWidth = 4f)

                // Glowing Keypoint Joint Circles
                listOf(head, neck, leftShoulder, rightShoulder, leftElbow, rightElbow, leftHip, rightHip, leftKnee, rightKnee, leftAnkle, rightAnkle).forEach { joint ->
                    drawCircle(color = OrangePrimary, radius = 6f, center = joint)
                    drawCircle(color = Color.White, radius = 3f, center = joint)
                }
            }
        }
    }
}
