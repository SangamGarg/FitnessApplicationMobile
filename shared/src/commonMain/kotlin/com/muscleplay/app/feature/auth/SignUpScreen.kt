package com.muscleplay.app.feature.auth

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

private val OrangePrimary = Color(0xFFFF5722)
private val OrangeSecondary = Color(0xFFFF9800)
private val OrangeGlow = Color(0xFFFF3D00)
private val DarkBackground = Color(0xFF0F0F17)

@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onNavigateToSignIn: () -> Unit,
) {
    val fullName = remember { FieldState() }
    val email = remember { FieldState() }
    val password = remember { FieldState() }
    val confirmPassword = remember { FieldState() }
    var isPasswordVisible by remember { mutableStateOf(false) }
    // Consent must be given explicitly, so the box starts unchecked
    var termsAccepted by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    // ---- Validation ----
    val nameError = AuthValidator.nameError(fullName.text)
    val emailError = AuthValidator.emailError(email.text)
    val passwordRules = AuthValidator.passwordRules(password.text)
    val passwordValid = AuthValidator.isPasswordValid(password.text)
    val confirmError = AuthValidator.confirmPasswordError(password.text, confirmPassword.text)

    // Errors only appear after the user has left the field (or pressed the button)
    val visibleNameError = if (fullName.touched) nameError else null
    val visibleEmailError = if (email.touched) emailError else null
    val visiblePasswordError =
        if (password.touched && !passwordValid) "Password doesn't meet all requirements" else null
    val visibleConfirmError = if (confirmPassword.touched) confirmError else null

    // Button stays disabled until every field is filled and terms are accepted
    val canSubmit = fullName.text.isNotBlank() &&
            email.text.isNotBlank() &&
            password.text.isNotEmpty() &&
            confirmPassword.text.isNotEmpty() &&
            termsAccepted

    fun submit() {
        listOf(fullName, email, password, confirmPassword).forEach { it.touched = true }
        val hasErrors = nameError != null ||
                emailError != null ||
                !passwordValid ||
                confirmError != null
        if (hasErrors) {
            snackbarMessage = "Please fix the highlighted fields."
        } else {
            onSignUpSuccess()
        }
    }

    // Ambient Glowing Background Pulsing Animation
    val infiniteTransition = rememberInfiniteTransition(label = "signUpPulse")
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                contentPadding = PaddingValues(vertical = 32.dp)
            ) {
                // Header Branding
                item {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(OrangePrimary, OrangeSecondary))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Join MusclePlay 🔥",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Create your account for AI pose tracking",
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
                            // Full Name
                            AuthTextField(
                                value = fullName.text,
                                onValueChange = {
                                    if (it.length <= AuthValidator.MAX_NAME_LENGTH) fullName.text = it
                                },
                                label = "Full Name",
                                leadingIcon = Icons.Default.Person,
                                errorText = visibleNameError,
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Words,
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next
                                ),
                                onFocusChanged = { fullName.onFocusChanged(it) }
                            )

                            // Email Input (email only, no username)
                            AuthTextField(
                                value = email.text,
                                onValueChange = { input ->
                                    email.text = input
                                        .filter { !it.isWhitespace() }
                                        .take(AuthValidator.MAX_EMAIL_LENGTH)
                                },
                                label = "Email Address",
                                leadingIcon = Icons.Default.Email,
                                errorText = visibleEmailError,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Next
                                ),
                                onFocusChanged = { email.onFocusChanged(it) }
                            )

                            // Password Input
                            AuthTextField(
                                value = password.text,
                                onValueChange = { input ->
                                    // Spaces aren't allowed in passwords
                                    password.text = input
                                        .filter { !it.isWhitespace() }
                                        .take(AuthValidator.MAX_PASSWORD_LENGTH)
                                },
                                label = "Password",
                                leadingIcon = Icons.Default.Lock,
                                errorText = visiblePasswordError,
                                trailingIcon = {
                                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
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
                                    imeAction = ImeAction.Next
                                ),
                                onFocusChanged = { password.onFocusChanged(it) }
                            )

                            // Live password checklist - ticks as each condition is satisfied
                            PasswordRequirementsList(
                                rules = passwordRules,
                                showFailures = password.touched
                            )

                            // Confirm Password Input
                            AuthTextField(
                                value = confirmPassword.text,
                                onValueChange = { input ->
                                    confirmPassword.text = input
                                        .filter { !it.isWhitespace() }
                                        .take(AuthValidator.MAX_PASSWORD_LENGTH)
                                },
                                label = "Confirm Password",
                                leadingIcon = Icons.Default.LockReset,
                                errorText = visibleConfirmError,
                                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                onFocusChanged = { confirmPassword.onFocusChanged(it) }
                            )

                            // Terms Checkbox & Clickable Label
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = termsAccepted,
                                    onCheckedChange = { termsAccepted = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = OrangePrimary,
                                        uncheckedColor = Color.White.copy(alpha = 0.5f)
                                    )
                                )
                                Row {
                                    Text(
                                        text = "I agree to ",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                    Text(
                                        text = "Terms & Privacy Policy",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = OrangeSecondary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.clickable { showTermsDialog = true }
                                    )
                                }
                            }

                            // Sign Up Button (disabled until all fields are filled)
                            Button(
                                onClick = { submit() },
                                enabled = canSubmit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
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
                                        "Create Account",
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

                // Switch to Sign In
                item {
                    Spacer(modifier = Modifier.height(28.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Already have an account? ", color = Color.White.copy(alpha = 0.7f))
                        Text(
                            text = "Sign In",
                            color = OrangeSecondary,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.clickable(onClick = onNavigateToSignIn)
                        )
                    }
                }
            }

            // Terms & Privacy Policy Pop-up Dialog
            if (showTermsDialog) {
                AlertDialog(
                    onDismissRequest = { showTermsDialog = false },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = null,
                            tint = OrangePrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    },
                    title = {
                        Text(
                            text = "Terms & Privacy Policy",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 350.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Text(
                                    text = "1. Terms of Service",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = OrangeSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "By creating an account on MusclePlay, you agree to follow our community guidelines. Exercises provided in the app should be performed safely at your own pace.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }

                            item {
                                Text(
                                    text = "2. AI Camera & Pose Tracking",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = OrangeSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Camera video feeds for rep tracking and squat/push-up master games are processed on-device in real-time. MusclePlay does NOT upload or record video recordings to cloud servers.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }

                            item {
                                Text(
                                    text = "3. Privacy Policy",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = OrangeSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Your account details and fitness stats are encrypted and protected. We do not sell or share your personal health data with third-party advertisers.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                termsAccepted = true
                                showTermsDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                        ) {
                            Text("I Accept", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showTermsDialog = false }) {
                            Text("Close")
                        }
                    }
                )
            }
        }
    }
}

/** Checklist that shows which password conditions are already satisfied. */
@Composable
private fun PasswordRequirementsList(
    rules: List<PasswordRule>,
    showFailures: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Your password must contain:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.8f)
        )
        rules.forEach { rule ->
            PasswordRequirementRow(rule = rule, showFailure = showFailures)
        }
    }
}

@Composable
private fun PasswordRequirementRow(
    rule: PasswordRule,
    showFailure: Boolean,
) {
    val failed = showFailure && !rule.isMet

    val tint by animateColorAsState(
        targetValue = when {
            rule.isMet -> AuthSuccessGreen
            failed -> AuthErrorRed
            else -> Color.White.copy(alpha = 0.4f)
        },
        label = "ruleTint"
    )
    val textColor by animateColorAsState(
        targetValue = if (rule.isMet) Color.White else Color.White.copy(alpha = 0.65f),
        label = "ruleText"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Crossfade(targetState = rule.isMet, label = "ruleIcon") { met ->
            Icon(
                imageVector = when {
                    met -> Icons.Default.CheckCircle
                    failed -> Icons.Default.Cancel
                    else -> Icons.Default.RadioButtonUnchecked
                },
                contentDescription = if (met) "Requirement met" else "Requirement not met",
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            text = rule.label,
            style = MaterialTheme.typography.bodySmall,
            color = textColor
        )
    }
}

@Composable
private fun GlassCardContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.12f), Color.White.copy(alpha = 0.04f)
                    )
                )
            )
            .border(
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