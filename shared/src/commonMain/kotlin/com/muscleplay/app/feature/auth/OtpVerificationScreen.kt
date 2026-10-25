package com.muscleplay.app.feature.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muscleplay.app.core.ui.BackHandler
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

private val OrangePrimary = Color(0xFFFF5722)
private val OrangeSecondary = Color(0xFFFF9800)
private val OrangeGlow = Color(0xFFFF3D00)
private val DarkBackground = Color(0xFF0F0F17)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun OtpVerificationScreen(
    userEmail: String = "user@muscleplay.com",
    onOtpVerified: () -> Unit,
    onBack: () -> Unit
) {
    var otpText by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var timerSeconds by remember { mutableIntStateOf(45) }
    var canResend by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Focuses the hidden OTP field AND forces the keyboard to show.
    fun openKeyboard() {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    // Don't let the user leave the screen while the code is being verified
    BackHandler(enabled = isVerifying) {
        snackbarMessage = "Please wait while we verify your code..."
    }

    // Countdown Timer Effect
    LaunchedEffect(timerSeconds) {
        if (timerSeconds > 0) {
            delay(1000L.milliseconds)
            timerSeconds--
        } else {
            canResend = true
        }
    }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }

    // Auto focus OTP field on entrance and open the keyboard
    LaunchedEffect(Unit) {
        delay(300L.milliseconds)
        openKeyboard()
    }

    // Ambient Glowing Background Pulsing Animation
    val infiniteTransition = rememberInfiniteTransition(label = "otpPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "otpPulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Glowing Ambient Orbs
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
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
            ) {
                // Main content: centered when there's room, scrollable when the keyboard is open
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 56.dp) // keeps content clear of the back button
                        .imePadding()
                ) {
                    val viewportHeight = maxHeight

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = viewportHeight),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Header Icon & Title
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(OrangePrimary, OrangeSecondary))),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MarkEmailRead,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(42.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = "Verify Your Email ✉️",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "We sent a 6-digit verification code to",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                text = userEmail,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = OrangeSecondary
                            )

                            Spacer(modifier = Modifier.height(28.dp))

                            // Glass Card containing Segmented 6-Digit OTP Field
                            GlassCardContainer {
                                Column(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(20.dp)
                                ) {
                                    Text(
                                        text = "Enter 6-Digit Code",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = OrangePrimary,
                                        letterSpacing = 1.2.sp
                                    )

                                    // 6-Digit Segmented Responsive Input Boxes
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        BasicTextField(
                                            value = otpText,
                                            onValueChange = { newValue ->
                                                val filtered = newValue.filter { it.isDigit() }.take(6)
                                                otpText = filtered
                                                errorMessage = null
                                                if (filtered.length == 6) {
                                                    keyboardController?.hide()
                                                    isVerifying = true
                                                }
                                            },
                                            enabled = !isVerifying,
                                            textStyle = TextStyle(color = Color.Transparent),
                                            cursorBrush = SolidColor(Color.Transparent),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier
                                                .focusRequester(focusRequester)
                                                .size(1.dp)
                                        )

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = null,
                                                    enabled = !isVerifying
                                                ) { openKeyboard() },
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            (0 until 6).forEach { index ->
                                                val char = otpText.getOrNull(index)?.toString() ?: ""
                                                val isFocused =
                                                    otpText.length == index || (index == 5 && otpText.length == 6)

                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(52.dp)
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(
                                                            if (char.isNotEmpty()) OrangePrimary.copy(alpha = 0.15f)
                                                            else Color.White.copy(alpha = 0.08f)
                                                        )
                                                        .border(
                                                            width = if (isFocused) 2.dp else 1.dp,
                                                            color = when {
                                                                errorMessage != null -> Color(0xFFFF5252)
                                                                isFocused -> OrangePrimary
                                                                char.isNotEmpty() -> OrangeSecondary.copy(alpha = 0.7f)
                                                                else -> Color.White.copy(alpha = 0.2f)
                                                            },
                                                            shape = RoundedCornerShape(12.dp)
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = char,
                                                        style = MaterialTheme.typography.titleLarge,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Inline Error State
                                    errorMessage?.let { error ->
                                        Text(
                                            text = error,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFFFF5252),
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                    }

                                    // Countdown Timer & Resend Option
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        if (!canResend) {
                                            Text(
                                                text = "Resend code in ",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White.copy(alpha = 0.6f)
                                            )
                                            Text(
                                                text = "${timerSeconds}s",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = OrangeSecondary
                                            )
                                        } else {
                                            Text(
                                                text = "Didn't receive code? ",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White.copy(alpha = 0.7f)
                                            )
                                            Text(
                                                text = "Resend Code",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = OrangePrimary,
                                                modifier = Modifier.clickable(enabled = !isVerifying) {
                                                    timerSeconds = 45
                                                    canResend = false
                                                    otpText = ""
                                                    errorMessage = null
                                                    snackbarMessage = "New 6-digit OTP code sent to $userEmail"
                                                    openKeyboard()
                                                }
                                            )
                                        }
                                    }

                                    // Verify & Continue Button
                                    Button(
                                        onClick = {
                                            if (otpText.length < 6) {
                                                errorMessage = "Please enter all 6 digits."
                                            } else {
                                                keyboardController?.hide()
                                                isVerifying = true
                                            }
                                        },
                                        enabled = !isVerifying,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp),
                                        shape = RoundedCornerShape(26.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                                    ) {
                                        if (isVerifying) {
                                            LaunchedEffect(Unit) {
                                                delay(1200L.milliseconds)
                                                isVerifying = false
                                                if (otpText == "123456" || otpText.length == 6) {
                                                    onOtpVerified()
                                                } else {
                                                    errorMessage = "Invalid verification code. Please try again."
                                                }
                                            }
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(24.dp),
                                                color = Color.White,
                                                strokeWidth = 2.5.dp
                                            )
                                        } else {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "Verify & Continue",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
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
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Notice
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(OrangeSecondary.copy(alpha = 0.12f))
                                    .border(
                                        width = 1.dp,
                                        color = OrangeSecondary.copy(alpha = 0.35f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = OrangeSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Kindly do not press back or close the app while your code is being verified.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }

                // Back button
                IconButton(
                    onClick = onBack,
                    enabled = !isVerifying,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 8.dp, top = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = if (isVerifying) Color.White.copy(alpha = 0.3f) else Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun GlassCardContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
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
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        content()
    }
}
