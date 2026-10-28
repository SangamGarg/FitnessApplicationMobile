package com.muscleplay.app.feature.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val OrangePrimary = Color(0xFFFF5722)
private val OrangeSecondary = Color(0xFFFF9800)
private val OrangeGlow = Color(0xFFFF3D00)
private val DarkBackground = Color(0xFF0F0F17)

@Composable
fun SignInScreen(
    onSignInSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit,
) {
    val email = remember { FieldState() }
    var passwordText by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    // Validation
    val emailError = AuthValidator.emailError(email.text)
    val visibleEmailError = if (email.touched) emailError else null

    // Button stays disabled until every field has something in it
    val canSubmit = email.text.isNotBlank() && passwordText.isNotEmpty()

    fun submit() {
        email.touched = true
        if (emailError != null) {
            snackbarMessage = "Please enter a valid email address."
        } else {
            onSignInSuccess()
        }
    }

    // Ambient Glowing Background Pulsing Animation
    val infiniteTransition = rememberInfiniteTransition(label = "authPulse")
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .imePadding()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    contentPadding = PaddingValues(vertical = 32.dp)
                ) {
                    // Header Branding
                    item {
                        Box(
                            modifier = Modifier.size(72.dp).clip(CircleShape).background(
                                Brush.linearGradient(
                                    listOf(
                                        OrangePrimary, OrangeSecondary
                                    )
                                )
                            ), contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(38.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = "Welcome Back 🔥",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Sign in to continue your fitness journey",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.height(28.dp))
                    }

                    // Glass Inputs Card
                    item {
                        GlassCardContainer {
                            Column(
                                modifier = Modifier.padding(22.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Email Input (email only, no username)
                                AuthTextField(
                                    value = email.text,
                                    onValueChange = { input ->
                                        email.text = input.filter { !it.isWhitespace() }
                                            .take(AuthValidator.MAX_EMAIL_LENGTH)
                                    },
                                    label = "Email Address",
                                    leadingIcon = Icons.Default.Email,
                                    errorText = visibleEmailError,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Email, imeAction = ImeAction.Next
                                    ),
                                    onFocusChanged = { email.onFocusChanged(it) })

                                // Password Input
                                AuthTextField(
                                    value = passwordText,
                                    onValueChange = {
                                        passwordText = it.take(AuthValidator.MAX_PASSWORD_LENGTH)
                                    },
                                    label = "Password",
                                    leadingIcon = Icons.Default.Lock,
                                    errorText = null,
                                    trailingIcon = {
                                        IconButton(onClick = {
                                            isPasswordVisible = !isPasswordVisible
                                        }) {
                                            Icon(
                                                imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                                                tint = Color.White.copy(alpha = 0.6f)
                                            )
                                        }
                                    },
                                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password, imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(onDone = {
                                        focusManager.clearFocus()
                                        if (canSubmit) submit()
                                    })
                                )

                                // Forgot Password Link
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    TextButton(onClick = { showForgotPasswordDialog = true }) {
                                        Text(
                                            "Forgot Password?",
                                            color = OrangeSecondary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                // Sign In Button (disabled until all fields are filled)
                                Button(
                                    onClick = { submit() },
                                    enabled = canSubmit,
                                    modifier = Modifier.fillMaxWidth().height(52.dp),
                                    shape = RoundedCornerShape(26.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = OrangePrimary,
                                        contentColor = Color.White,
                                        disabledContainerColor = Color.White.copy(alpha = 0.12f),
                                        disabledContentColor = Color.White.copy(alpha = 0.4f)
                                    )
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            "Sign In",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Switch to Sign Up
                    item {
                        Spacer(modifier = Modifier.height(28.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Don't have an account? ", color = Color.White.copy(alpha = 0.7f))
                            Text(
                                text = "Sign Up",
                                color = OrangeSecondary,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.clickable(onClick = onNavigateToSignUp)
                            )
                        }
                    }
                }
            }

            // Dark Glassmorphic Forgot Password & OTP Verification Dialog
            if (showForgotPasswordDialog) {
                var resetEmailText by remember { mutableStateOf(email.text.trim()) }
                var isOtpSent by remember { mutableStateOf(false) }
                var enteredOtpCode by remember { mutableStateOf("") }
                var isOtpVerified by remember { mutableStateOf(false) }
                var otpStatusMessage by remember { mutableStateOf<String?>(null) }

                val resetEmailError = AuthValidator.emailError(resetEmailText)
                val visibleResetError = if (resetEmailText.isNotEmpty()) resetEmailError else null

                AlertDialog(
                    onDismissRequest = { showForgotPasswordDialog = false },
                    shape = RoundedCornerShape(28.dp),
                    containerColor = Color(0xFF181824),
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(OrangePrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LockReset,
                                contentDescription = null,
                                tint = OrangeSecondary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    },
                    title = {
                        Text(
                            text = "Reset Password 🔑",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Enter your registered email address and request an OTP to reset your password.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )

                            // 1. Email Input Field
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Email Address", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                OutlinedTextField(
                                    value = resetEmailText,
                                    onValueChange = { input ->
                                        resetEmailText = input.filter { !it.isWhitespace() }.take(AuthValidator.MAX_EMAIL_LENGTH)
                                        isOtpSent = false
                                        isOtpVerified = false
                                        enteredOtpCode = ""
                                        otpStatusMessage = null
                                    },
                                    placeholder = { Text("your.email@domain.com", color = Color.White.copy(alpha = 0.4f)) },
                                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = OrangePrimary) },
                                    isError = visibleResetError != null,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    singleLine = true,
                                    colors = inputColors()
                                )
                                visibleResetError?.let { msg ->
                                    Text(msg, style = MaterialTheme.typography.labelSmall, color = Color(0xFFFF5252))
                                }
                            }

                            // 2. Small "Send OTP" Button below Email Address Field
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = {
                                        if (resetEmailError == null && resetEmailText.isNotBlank()) {
                                            isOtpSent = true
                                            otpStatusMessage = "📩 Demo OTP sent: 123456"
                                        }
                                    },
                                    enabled = resetEmailError == null && resetEmailText.isNotBlank(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = OrangePrimary.copy(alpha = 0.25f),
                                        contentColor = OrangeSecondary,
                                        disabledContainerColor = Color.White.copy(alpha = 0.08f)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Default.MarkEmailRead, contentDescription = null, tint = OrangeSecondary, modifier = Modifier.size(16.dp))
                                        Text(
                                            text = if (isOtpSent) "Resend OTP" else "Send OTP 📩",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = OrangeSecondary
                                        )
                                    }
                                }
                            }

                            // 3. OTP Fill View (Revealed after clicking Send OTP, positioned ABOVE Send Link button)
                            AnimatedVisibility(visible = isOtpSent) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("Enter 6-Digit OTP Code", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                    OutlinedTextField(
                                        value = enteredOtpCode,
                                        onValueChange = { input ->
                                            enteredOtpCode = input.filter { it.isDigit() }.take(6)
                                            if (enteredOtpCode == "123456") {
                                                isOtpVerified = true
                                                otpStatusMessage = "✅ OTP Verified Successfully!"
                                            } else {
                                                isOtpVerified = false
                                                if (enteredOtpCode.length == 6) {
                                                    otpStatusMessage = "❌ Incorrect OTP code. Try 123456"
                                                }
                                            }
                                        },
                                        placeholder = { Text("e.g. 123456", color = Color.White.copy(alpha = 0.4f)) },
                                        leadingIcon = { Icon(Icons.Default.Pin, contentDescription = null, tint = OrangePrimary) },
                                        trailingIcon = {
                                            if (isOtpVerified) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = "Verified", tint = Color(0xFF4CAF50))
                                            }
                                        },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = inputColors()
                                    )

                                    otpStatusMessage?.let { msg ->
                                        Text(
                                            text = msg,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isOtpVerified) Color(0xFF4CAF50) else OrangeSecondary
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showForgotPasswordDialog = false
                                snackbarMessage = "Password reset link sent to ${resetEmailText.trim()}"
                            },
                            enabled = isOtpVerified,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OrangePrimary,
                                disabledContainerColor = Color.White.copy(alpha = 0.12f)
                            )
                        ) {
                            Text("Send Link", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showForgotPasswordDialog = false }) {
                            Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun inputColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = OrangePrimary,
    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
    errorBorderColor = Color(0xFFFF5252),
    errorLabelColor = Color(0xFFFF5252)
)

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
