package com.muscleplay.app.feature.auth

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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.muscleplay.app.core.network.NetworkResult
import com.muscleplay.app.core.ui.BackHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

private val OrangePrimary = Color(0xFFFF5722)
private val OrangeSecondary = Color(0xFFFF9800)
private val OrangeGlow = Color(0xFFFF3D00)
private val DarkBackground = Color(0xFF0F0F17)

private const val RESEND_TIMER_SECONDS = 60

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun OtpVerificationScreen(
    userEmail: String = "",
    userPassword: String = "",
    authRepository: AuthRepository? = null,
    onOtpVerified: () -> Unit,
    onBack: () -> Unit,
) {

    var otpText by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var timerSeconds by remember { mutableIntStateOf(RESEND_TIMER_SECONDS) }
    var canResend by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    fun openKeyboard() {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

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

    // Send OTP API call on entrance
    LaunchedEffect(Unit) {
        authRepository?.sendSignupOtp(userEmail)
        delay(300L.milliseconds)
        openKeyboard()
    }

    fun handleVerifyOtp() {
        if (otpText.length != 6 || isVerifying) return
        keyboardController?.hide()
        isVerifying = true
        errorMessage = null
        coroutineScope.launch {
            val verifyResult = authRepository?.verifySignupOtp(userEmail, otpText)
            if (verifyResult is NetworkResult.Error) {
                isVerifying = false
                errorMessage = verifyResult.message
                snackbarHostState.showSnackbar(verifyResult.message)
                // Retain digits on failure - DO NOT clear
            } else if (verifyResult is NetworkResult.Exception) {
                isVerifying = false
                val netErr = "Network connection failed. Please try again."
                errorMessage = netErr
                snackbarHostState.showSnackbar(netErr)
                // Retain digits on failure - DO NOT clear
            } else {
                // Post-Verification (HTTP 200 Success): proceed to loginRegister
                val authResult = authRepository?.authenticateWithEmail(
                    userEmail, userPassword, isSignUp = true
                )
                isVerifying = false
                if (authResult is NetworkResult.Error) {
                    snackbarHostState.showSnackbar(authResult.message)
                } else if (authResult is NetworkResult.Exception) {
                    snackbarHostState.showSnackbar("Connection failed during account registration.")
                } else {
                    onOtpVerified()
                }
            }
        }
    }

    // Auto-Submit on Complete Input (6 Digits)
    LaunchedEffect(otpText) {
        if (otpText.length == 6 && !isVerifying && errorMessage == null) {
            handleVerifyOtp()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "otpPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f, targetValue = 0.45f, animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing), repeatMode = RepeatMode.Reverse
        ), label = "otpPulse"
    )

    Box(
        modifier = Modifier.fillMaxSize().background(DarkBackground)
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
                    colors = listOf(
                        OrangeSecondary.copy(alpha = pulseAlpha * 0.6f), Color.Transparent
                    ), center = Offset(w * 0.15f, h * 0.85f), radius = w * 0.6f
                )
            )
        }

        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
            ) {
                BoxWithConstraints(
                    modifier = Modifier.fillMaxSize().padding(top = 56.dp).imePadding()
                ) {
                    val viewportHeight = maxHeight

                    Column(
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().heightIn(min = viewportHeight),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier.size(80.dp).clip(CircleShape).background(
                                    Brush.linearGradient(
                                        listOf(
                                            OrangePrimary, OrangeSecondary
                                        )
                                    )
                                ), contentAlignment = Alignment.Center
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
                                text = "Verify Email OTP",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Enter the 6-digit verification code sent to",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = userEmail,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = OrangeSecondary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(28.dp))

                            GlassCardContainer {
                                Column(
                                    modifier = Modifier.padding(22.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(20.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        BasicTextField(
                                            value = otpText,
                                            onValueChange = { newValue ->
                                                val filtered =
                                                    newValue.filter { it.isDigit() }.take(6)
                                                otpText = filtered
                                                // Reset error state immediately on edit/backspace
                                                errorMessage = null
                                                if (filtered.length == 6) {
                                                    keyboardController?.hide()
                                                }
                                            },
                                            enabled = !isVerifying,
                                            textStyle = TextStyle(color = Color.Transparent),
                                            cursorBrush = SolidColor(Color.Transparent),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.focusRequester(focusRequester)
                                                .size(1.dp)
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth().clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null,
                                                enabled = !isVerifying
                                            ) { openKeyboard() },
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            (0 until 6).forEach { index ->
                                                val char =
                                                    otpText.getOrNull(index)?.toString() ?: ""
                                                val isFocused =
                                                    otpText.length == index || (index == 5 && otpText.length == 6)

                                                Box(
                                                    modifier = Modifier.weight(1f).height(52.dp)
                                                        .clip(RoundedCornerShape(12.dp)).background(
                                                            when {
                                                                errorMessage != null -> Color(
                                                                    0xFFFF5252
                                                                ).copy(alpha = 0.12f)

                                                                char.isNotEmpty() -> OrangePrimary.copy(
                                                                    alpha = 0.15f
                                                                )

                                                                else -> Color.White.copy(alpha = 0.08f)
                                                            }
                                                        ).border(
                                                            width = if (isFocused || errorMessage != null) 2.dp else 1.dp,
                                                            color = when {
                                                                errorMessage != null -> Color(
                                                                    0xFFFF5252
                                                                )

                                                                isFocused -> OrangePrimary
                                                                char.isNotEmpty() -> OrangeSecondary.copy(
                                                                    alpha = 0.7f
                                                                )

                                                                else -> Color.White.copy(alpha = 0.2f)
                                                            },
                                                            shape = RoundedCornerShape(12.dp)
                                                        ).clickable(enabled = !isVerifying) {
                                                            openKeyboard()
                                                        }, contentAlignment = Alignment.Center
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

                                    errorMessage?.let { error ->
                                        Text(
                                            text = error,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFFFF5252),
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                    }

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
                                                    coroutineScope.launch {
                                                        authRepository?.sendSignupOtp(userEmail)
                                                    }
                                                    timerSeconds = RESEND_TIMER_SECONDS
                                                    canResend = false
                                                    otpText = ""
                                                    errorMessage = null
                                                    snackbarMessage =
                                                        "New OTP code sent to $userEmail"
                                                    openKeyboard()
                                                })
                                        }
                                    }

                                    Button(
                                        onClick = { handleVerifyOtp() },
                                        enabled = otpText.length == 6 && !isVerifying,
                                        modifier = Modifier.fillMaxWidth().height(52.dp),
                                        shape = RoundedCornerShape(26.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = OrangePrimary,
                                            disabledContainerColor = Color.White.copy(alpha = 0.12f)
                                        )
                                    ) {
                                        if (isVerifying) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(22.dp),
                                                color = Color.White,
                                                strokeWidth = 2.dp
                                            )
                                        } else {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "Verify OTP & Continue",
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
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = OrangeSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Check your spam or junk folder if you can't find the email.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }

                IconButton(
                    onClick = onBack,
                    enabled = !isVerifying,
                    modifier = Modifier.padding(start = 12.dp, top = 12.dp).size(40.dp)
                        .clip(CircleShape).background(Color.White.copy(alpha = 0.08f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun GlassCardContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(24.dp)).background(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.12f), Color.White.copy(alpha = 0.04f)
                )
            )
        ).border(
            width = 1.dp, brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.35f),
                    OrangePrimary.copy(alpha = 0.25f),
                    Color.White.copy(alpha = 0.08f)
                )
            ), shape = RoundedCornerShape(24.dp)
        )
    ) {
        content()
    }
}
