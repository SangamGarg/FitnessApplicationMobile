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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muscleplay.app.core.network.NetworkResult
import kotlinx.coroutines.launch

private val OrangePrimary = Color(0xFFFF5722)
private val OrangeSecondary = Color(0xFFFF9800)
private val OrangeGlow = Color(0xFFFF3D00)
private val DarkBackground = Color(0xFF0F0F17)

private enum class ForgotPasswordStep {
    ENTER_EMAIL,
    ENTER_OTP
}

@Composable
fun SignInScreen(
    authRepository: AuthRepository? = null,
    onSignInSuccess: (isProfileComplete: Boolean) -> Unit,
    onNavigateToSignUp: () -> Unit,
    onNavigateToResetPassword: (email: String, verificationToken: String) -> Unit = { _, _ -> },
) {
    val email = remember { FieldState() }
    var passwordText by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()

    var isSubmitting by remember { mutableStateOf(false) }

    // Validation
    val emailError = AuthValidator.emailError(email.text)
    val visibleEmailError = if (email.touched) emailError else null

    // Button stays disabled until every field has something in it
    val canSubmit = email.text.isNotBlank() && passwordText.isNotEmpty()

    fun submit() {
        focusManager.clearFocus()
        keyboardController?.hide()
        if (isSubmitting) return
        email.touched = true
        if (emailError != null) {
            snackbarMessage = "Please enter a valid email address."
        } else {
            isSubmitting = true
            coroutineScope.launch {
                val res = authRepository?.authenticateWithEmail(
                    email.text,
                    passwordText,
                    isSignUp = false
                )
                isSubmitting = false
                when (res) {
                    is NetworkResult.Error -> {
                        snackbarMessage =
                            res.message.ifBlank { "Sign-in failed. Please check credentials or connection." }
                    }

                    is NetworkResult.Exception -> {
                        snackbarMessage =
                            "Connection timed out. Please try again."
                    }

                    is NetworkResult.Success -> {
                        val isProfileComplete = res.data
                        onSignInSuccess(isProfileComplete)
                    }

                    else -> {
                        onSignInSuccess(true)
                    }
                }
            }
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
                                // Email Input
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
                                        keyboardType = KeyboardType.Email,
                                        imeAction = ImeAction.Next
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
                                        keyboardType = KeyboardType.Password,
                                        imeAction = ImeAction.Done
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

                                // Sign In Button
                                Button(
                                    onClick = { submit() },
                                    enabled = canSubmit && !isSubmitting,
                                    modifier = Modifier.fillMaxWidth().height(52.dp),
                                    shape = RoundedCornerShape(26.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = OrangePrimary,
                                        contentColor = Color.White,
                                        disabledContainerColor = Color.White.copy(alpha = 0.12f),
                                        disabledContentColor = Color.White.copy(alpha = 0.4f)
                                    )
                                ) {
                                    if (isSubmitting) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(22.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
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
                var currentDialogStep by remember { mutableStateOf(ForgotPasswordStep.ENTER_EMAIL) }
                var resetEmailText by remember { mutableStateOf(email.text.trim()) }
                var otpInputText by remember { mutableStateOf("") }
                var timerSeconds by remember { mutableIntStateOf(60) }
                var canResendOtp by remember { mutableStateOf(false) }

                var isDialogLoading by remember { mutableStateOf(false) }
                var dialogErrorMsg by remember { mutableStateOf<String?>(null) }

                val resetEmailError = AuthValidator.emailError(resetEmailText)

                // 60-Second Resend OTP Timer Effect
                LaunchedEffect(currentDialogStep, timerSeconds) {
                    if (currentDialogStep == ForgotPasswordStep.ENTER_OTP && timerSeconds > 0) {
                        delay(1000L.milliseconds)
                        timerSeconds--
                    } else if (timerSeconds == 0) {
                        canResendOtp = true
                    }
                }

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
                                imageVector = when (currentDialogStep) {
                                    ForgotPasswordStep.ENTER_EMAIL -> Icons.Default.LockReset
                                    ForgotPasswordStep.ENTER_OTP -> Icons.Default.MarkEmailRead
                                },
                                contentDescription = null,
                                tint = OrangeSecondary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    },
                    title = {
                        Text(
                            text = when (currentDialogStep) {
                                ForgotPasswordStep.ENTER_EMAIL -> "Forgot Password 🔑"
                                ForgotPasswordStep.ENTER_OTP -> "Verify Email OTP 📩"
                            },
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = when (currentDialogStep) {
                                    ForgotPasswordStep.ENTER_EMAIL -> "Enter your registered email address to receive a 6-digit OTP code."
                                    ForgotPasswordStep.ENTER_OTP -> "Enter the 6-digit OTP code sent to $resetEmailText."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )

                            when (currentDialogStep) {
                                ForgotPasswordStep.ENTER_EMAIL -> {
                                    // STEP 1: ENTER EMAIL
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            "Email Address",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        OutlinedTextField(
                                            value = resetEmailText,
                                            onValueChange = { input ->
                                                resetEmailText = input.filter { !it.isWhitespace() }
                                                    .take(AuthValidator.MAX_EMAIL_LENGTH)
                                                dialogErrorMsg = null
                                            },
                                            placeholder = {
                                                Text(
                                                    "your.email@domain.com",
                                                    color = Color.White.copy(alpha = 0.4f)
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.Email,
                                                    contentDescription = null,
                                                    tint = OrangePrimary
                                                )
                                            },
                                            isError = resetEmailText.isNotEmpty() && resetEmailError != null,
                                            keyboardOptions = KeyboardOptions(
                                                keyboardType = KeyboardType.Email,
                                                imeAction = ImeAction.Done
                                            ),
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(14.dp),
                                            singleLine = true,
                                            colors = inputColors()
                                        )
                                        if (resetEmailText.isNotEmpty() && resetEmailError != null) {
                                            Text(
                                                resetEmailError,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFFFF5252)
                                            )
                                        }
                                    }
                                }

                                ForgotPasswordStep.ENTER_OTP -> {
                                    // STEP 2: ENTER OTP
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            "Mail: $resetEmailText",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = OrangeSecondary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "Change Email",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.White.copy(alpha = 0.6f),
                                            modifier = Modifier.clickable(onClick = {
                                                currentDialogStep = ForgotPasswordStep.ENTER_EMAIL
                                            }
                                            ))

                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            "6-Digit OTP Code",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        OutlinedTextField(
                                            value = otpInputText,
                                            onValueChange = { input ->
                                                otpInputText = input.filter { it.isDigit() }.take(6)
                                                dialogErrorMsg = null
                                            },
                                            placeholder = {
                                                Text(
                                                    "e.g. 123456",
                                                    color = Color.White.copy(alpha = 0.4f)
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.Pin,
                                                    contentDescription = null,
                                                    tint = OrangePrimary
                                                )
                                            },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(14.dp),
                                            singleLine = true,
                                            colors = inputColors()
                                        )
                                    }

                                    // 60-Second Resend Timer Indicator
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (!canResendOtp) {
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
                                            TextButton(
                                                onClick = {
                                                    coroutineScope.launch {
                                                        authRepository?.sendForgotPasswordOtp(
                                                            resetEmailText
                                                        )
                                                    }
                                                    timerSeconds = 60
                                                    canResendOtp = false
                                                    snackbarMessage =
                                                        "New OTP sent to $resetEmailText"
                                                }
                                            ) {
                                                Text(
                                                    "Resend OTP 📩",
                                                    color = OrangePrimary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            dialogErrorMsg?.let { msg ->
                                Text(
                                    msg,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFFF5252)
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    when (currentDialogStep) {
                                        ForgotPasswordStep.ENTER_EMAIL -> {
                                            if (resetEmailError == null && resetEmailText.isNotBlank()) {
                                                isDialogLoading = true
                                                dialogErrorMsg = null
                                                val res = authRepository?.sendForgotPasswordOtp(
                                                    resetEmailText
                                                )
                                                isDialogLoading = false
                                                if (res is NetworkResult.Error) {
                                                    dialogErrorMsg = res.message
                                                } else if (res is NetworkResult.Success) {
                                                    snackbarMessage = res.data
                                                    timerSeconds = 60
                                                    canResendOtp = false
                                                    currentDialogStep = ForgotPasswordStep.ENTER_OTP
                                                }
                                            }
                                        }

                                        ForgotPasswordStep.ENTER_OTP -> {
                                            if (otpInputText.length == 6) {
                                                isDialogLoading = true
                                                dialogErrorMsg = null
                                                val res = authRepository?.verifyForgotPasswordOtp(
                                                    resetEmailText,
                                                    otpInputText
                                                )
                                                isDialogLoading = false
                                                if (res is NetworkResult.Success) {
                                                    val token = res.data
                                                    showForgotPasswordDialog = false
                                                    onNavigateToResetPassword(resetEmailText, token)
                                                } else if (res is NetworkResult.Error) {
                                                    dialogErrorMsg = res.message
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            enabled = !isDialogLoading && when (currentDialogStep) {
                                ForgotPasswordStep.ENTER_EMAIL -> resetEmailError == null && resetEmailText.isNotBlank()
                                ForgotPasswordStep.ENTER_OTP -> otpInputText.length == 6
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OrangePrimary,
                                disabledContainerColor = Color.White.copy(alpha = 0.12f)
                            )
                        ) {
                            if (isDialogLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = when (currentDialogStep) {
                                        ForgotPasswordStep.ENTER_EMAIL -> "Send OTP 📩"
                                        ForgotPasswordStep.ENTER_OTP -> "Verify OTP ✅"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                            }
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
    errorTextColor = Color.White,
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
