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
        initialValue = 0.3f, targetValue = 0.7f, animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse
        ), label = "glowPulse"
    )

    // Entrance Scale & Alpha Animation
    var startAnimation by remember { mutableStateOf(false) }
    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.6f, animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow
        ), label = "scaleAnim"
    )
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(1000),
        label = "alphaAnim"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2000.milliseconds) // Display splash screen for 2.2s before navigating
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

            // Center Primary Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(OrangeGlow.copy(alpha = glowPulse * 0.8f), Color.Transparent),
                    center = Offset(w * 0.5f, h * 0.45f),
                    radius = w * 0.7f
                )
            )

            // Bottom Secondary Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        OrangeSecondary.copy(alpha = glowPulse * 0.4f), Color.Transparent
                    ), center = Offset(w * 0.5f, h * 0.85f), radius = w * 0.6f
                )
            )
        }

        // Main Splash Brand Content
        Column(
            modifier = Modifier.graphicsLayer {
                scaleX = scaleAnim
                scaleY = scaleAnim
                alpha = alphaAnim
            }.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Glassmorphic Logo Container
            Box(
                modifier = Modifier.size(110.dp).clip(CircleShape).background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.05f)
                        )
                    )
                ).border(
                    width = 2.dp, brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.5f),
                            OrangePrimary.copy(alpha = 0.4f),
                            Color.White.copy(alpha = 0.1f)
                        )
                    ), shape = CircleShape
                ), contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.size(80.dp).clip(CircleShape).background(
                        brush = Brush.linearGradient(
                            colors = listOf(OrangePrimary, OrangeSecondary)
                        )
                    ), contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = "MusclePlay Logo",
                        tint = Color.White,
                        modifier = Modifier.size(46.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // App Brand Name with Glass Tag
            Surface(
                color = OrangePrimary.copy(alpha = 0.2f),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, OrangePrimary.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text = "NEXT-GEN FITNESS",
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
                fontSize = 36.sp,
                color = Color.White,
                letterSpacing = 3.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "AI Pose Tracking & Motion Gaming",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                letterSpacing = 0.5.sp
            )
        }

        // Bottom Loading Bar / Progress Track
        Box(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 60.dp).width(140.dp)
                .height(4.dp).clip(RoundedCornerShape(2.dp))
                .background(Color.White.copy(alpha = 0.15f))
        ) {
            val progressWidth by animateFloatAsState(
                targetValue = if (startAnimation) 1f else 0f,
                animationSpec = tween(2000, easing = LinearEasing),
                label = "progressWidth"
            )
            Box(
                modifier = Modifier.fillMaxHeight().fillMaxWidth(progressWidth).background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(OrangePrimary, OrangeSecondary)
                    )
                )
            )
        }
    }
}
