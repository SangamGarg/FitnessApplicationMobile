package com.muscleplay.app.feature.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

private val OrangePrimary = Color(0xFFFF5722)
private val OrangeSecondary = Color(0xFFFF9800)
private val OrangeGlow = Color(0xFFFF3D00)
private val DarkBackground = Color(0xFF0F0F17)

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
) {
    // Pulse and Glow Animation
    val infiniteTransition = rememberInfiniteTransition(label = "splashGlow")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.8f, animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse
        ), label = "glowPulse"
    )

    // 3D -Z Axis Zoom Animation (Scales from 0.1f deep background up to 1.15f screen cover)
    var startAnimation by remember { mutableStateOf(false) }
    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1.15f else 0.1f,
        animationSpec = tween(
            durationMillis = 2200,
            easing = FastOutSlowInEasing
        ),
        label = "scaleAnim3D"
    )

    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(1200),
        label = "alphaAnim"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2400.milliseconds) // Display splash screen for 2.4s before navigating
        onSplashFinished()
    }

    Box(
        modifier = Modifier.fillMaxSize().background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        // Glowing Ambient Radial Orbs
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Center Primary Glow Expansion
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(OrangeGlow.copy(alpha = glowPulse * 0.85f), Color.Transparent),
                    center = Offset(w * 0.5f, h * 0.45f),
                    radius = w * 0.8f * scaleAnim
                )
            )

            // Bottom Secondary Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        OrangeSecondary.copy(alpha = glowPulse * 0.45f), Color.Transparent
                    ), center = Offset(w * 0.5f, h * 0.85f), radius = w * 0.65f
                )
            )
        }

        // Main Splash Brand Content (Zooming in from 3D -Z Depth)
        Column(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scaleAnim
                    scaleY = scaleAnim
                    alpha = alphaAnim
                }
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Glassmorphic Logo Container
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.20f),
                                Color.White.copy(alpha = 0.05f)
                            )
                        )
                    )
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.6f),
                                OrangePrimary.copy(alpha = 0.5f),
                                Color.White.copy(alpha = 0.1f)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(OrangePrimary, OrangeSecondary)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = "MusclePlay Logo",
                        tint = Color.White,
                        modifier = Modifier.size(50.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // App Brand Name with Glass Tag
            Surface(
                color = OrangePrimary.copy(alpha = 0.25f),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, OrangePrimary.copy(alpha = 0.6f)
                )
            ) {
                Text(
                    text = "NEXT-GEN FITNESS ENGINE",
                    style = MaterialTheme.typography.labelSmall,
                    color = OrangeSecondary,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    letterSpacing = 2.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "MUSCLEPLAY",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 38.sp,
                color = Color.White,
                letterSpacing = 3.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "AI Pose Tracking & Motion Gaming",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
                letterSpacing = 0.5.sp
            )
        }

        // Bottom Build Version & Status Badge
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 50.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                color = Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OrangePrimary.copy(alpha = 0.3f))
            ) {
                Text(
                    text = "MusclePlay v1.0.0 • Build 104 • AI Engine Ready",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            // Bottom Progress Loading Bar
            Box(
                modifier = Modifier
                    .width(160.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.15f))
            ) {
                val progressWidth by animateFloatAsState(
                    targetValue = if (startAnimation) 1f else 0f,
                    animationSpec = tween(2200, easing = LinearEasing),
                    label = "progressWidth"
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progressWidth)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(OrangePrimary, OrangeSecondary)
                            )
                        )
                )
            }
        }
    }
}
