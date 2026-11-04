package com.muscleplay.app.feature.onboarding

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muscleplay.app.core.network.NetworkResult
import com.muscleplay.app.feature.auth.AuthRepository
import com.muscleplay.app.feature.auth.AuthValidator
import com.muscleplay.app.feature.onboarding.model.ActivityLevel
import com.muscleplay.app.feature.onboarding.model.FitnessGoal
import com.muscleplay.app.feature.onboarding.model.Gender
import com.muscleplay.app.feature.onboarding.model.UserActivityLevel
import com.muscleplay.app.feature.onboarding.model.UserExperienceLevel
import com.muscleplay.app.feature.onboarding.model.UserFitnessGoal
import com.muscleplay.app.feature.onboarding.model.UserGender
import com.muscleplay.app.feature.onboarding.model.UserOnboardingProfile
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

private val OrangePrimary = Color(0xFFFF5722)
private val OrangeSecondary = Color(0xFFFF9800)
private val DarkBackground = Color(0xFF0F0F17)

private data class NeonAvatar(val name: String, val emoji: String, val badgeColor: Color)

private val neonAvatarsList = listOf(
    NeonAvatar("Cyber Titan", "⚡", Color(0xFFFF5722)),
    NeonAvatar("Neon Runner", "🏃", Color(0xFF2196F3)),
    NeonAvatar("Alpha Lifter", "🏋️", Color(0xFFFF9800)),
    NeonAvatar("Solar Athlete", "☀️", Color(0xFFFFC107)),
    NeonAvatar("Pulse Champion", "🏆", Color(0xFF4CAF50)),
    NeonAvatar("Apex Warrior", "🔥", Color(0xFFE91E63))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileSetupScreen(
    authRepository: AuthRepository? = null,
    onSetupComplete: (UserOnboardingProfile) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()

    // User Profile State
    var fullNameText by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf(Gender.MALE) }
    var fitnessGoal by remember { mutableStateOf(FitnessGoal.WEIGHT_LOSS) }
    var isImperial by remember { mutableStateOf(false) }

    var heightText by remember { mutableStateOf("175") }
    var weightText by remember { mutableStateOf("70") }
    var ageText by remember { mutableStateOf("25") }

    var activityLevel by remember { mutableStateOf(ActivityLevel.LEVEL_3) }
    var experienceLevel by remember { mutableStateOf(UserExperienceLevel.INTERMEDIATE) }

    var profileImageName by remember { mutableStateOf<String?>(null) }
    var showNameError by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var submitErrorMsg by remember { mutableStateOf<String?>(null) }
    var isConfettiActive by remember { mutableStateOf(false) }

    // Step Validations
    val fullNameError = AuthValidator.nameError(fullNameText)
    val isStep1Valid = fullNameError == null

    val rawHeight = heightText.toDoubleOrNull() ?: 0.0
    val rawWeight = weightText.toDoubleOrNull() ?: 0.0
    val age = ageText.toIntOrNull() ?: 0

    val heightCm = if (isImperial) rawHeight * 2.54 else rawHeight
    val weightKg = if (isImperial) rawWeight / 2.20462 else rawWeight

    val heightError = when {
        heightText.isBlank() -> "Height is required"
        rawHeight <= 0 -> "Height cannot be 0"
        heightCm !in 50.0..250.0 -> if (isImperial) "Enter valid height (20 - 98 in)" else "Enter valid height (50.0 - 250.0 cm)"
        else -> null
    }

    val weightError = when {
        weightText.isBlank() -> "Weight is required"
        rawWeight <= 0 -> "Weight cannot be 0"
        weightKg !in 10.0..500.0 -> if (isImperial) "Enter valid weight (22 - 1102 lbs)" else "Enter valid weight (10.0 - 500.0 kg)"
        else -> null
    }

    val ageError = when {
        ageText.isBlank() -> "Age is required"
        age < 13 || age > 120 -> "Age must be between 13 and 120"
        else -> null
    }

    val isStep2Valid = heightError == null && weightError == null && ageError == null

    val currentProfile = remember(fullNameText, gender, fitnessGoal, heightCm, weightKg, age, activityLevel, experienceLevel, profileImageName, isImperial) {
        UserOnboardingProfile(
            fullName = fullNameText.trim(),
            gender = gender,
            fitnessGoal = fitnessGoal,
            heightCm = heightCm,
            weightKg = weightKg,
            targetWeightKg = weightKg,
            age = age,
            activityLevel = activityLevel,
            experienceLevel = experienceLevel,
            profileImageUri = profileImageName,
            isImperialUnits = isImperial
        )
    }

    val isCurrentStepValid = when (pagerState.currentPage) {
        0 -> isStep1Valid
        1 -> isStep2Valid
        else -> true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Profile & Goal Setup", fontWeight = FontWeight.ExtraBold, color = Color.White) },
                    navigationIcon = {
                        if (pagerState.currentPage > 0) {
                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                    }
                                },
                                enabled = !isSubmitting
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Step", tint = Color.White)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .imePadding()
            ) {
                // Step Progress Indicator Bar (Updated to 4 steps)
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "STEP ${pagerState.currentPage + 1} OF 4",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = OrangePrimary,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = when (pagerState.currentPage) {
                                0 -> "Name, Gender & Goal"
                                1 -> "Body Measurements"
                                2 -> "Activity & Experience"
                                else -> "Profile Picture & Avatars"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = OrangeSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    LinearProgressIndicator(
                        progress = { (pagerState.currentPage + 1) / 4f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = OrangePrimary,
                        trackColor = Color.White.copy(alpha = 0.1f)
                    )
                }

                // Horizontal Pager for Smooth Swiping between 4 Steps
                HorizontalPager(
                    state = pagerState,
                    userScrollEnabled = isCurrentStepValid && !isSubmitting,
                    modifier = Modifier.weight(1f)
                ) { page ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        when (page) {
                            0 -> {
                                // STEP 1: FULL NAME, GENDER & FITNESS GOAL
                                item {
                                    GlassCardContainer {
                                        Column(
                                            modifier = Modifier.padding(20.dp),
                                            verticalArrangement = Arrangement.spacedBy(14.dp)
                                        ) {
                                            Surface(
                                                color = OrangePrimary.copy(alpha = 0.18f),
                                                shape = RoundedCornerShape(14.dp),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, OrangePrimary.copy(alpha = 0.4f))
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(12.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = OrangeSecondary, modifier = Modifier.size(22.dp))
                                                    Text(
                                                        text = "Please take a moment to fill in these details. This helps our AI Coach understand your body better and generate a personalized, AI-optimized fitness plan for you! 🎯",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = Color.White.copy(alpha = 0.9f),
                                                        lineHeight = 18.sp
                                                    )
                                                }
                                            }

                                            // Full Name Field (Positioned Above Gender)
                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text("Full Name *", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                                OutlinedTextField(
                                                    value = fullNameText,
                                                    onValueChange = { input ->
                                                        if (input.length <= AuthValidator.MAX_NAME_LENGTH) {
                                                            fullNameText = input
                                                            showNameError = false
                                                        }
                                                    },
                                                    placeholder = { Text("e.g. Alex Johnson", color = Color.White.copy(alpha = 0.4f)) },
                                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = OrangePrimary) },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(16.dp),
                                                    singleLine = true,
                                                    isError = fullNameError != null && (showNameError || fullNameText.isNotEmpty()),
                                                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                                                    colors = inputColors()
                                                )
                                                if (fullNameError != null && (showNameError || fullNameText.isNotEmpty())) {
                                                    Text(fullNameError, style = MaterialTheme.typography.labelSmall, color = Color(0xFFFF5252))
                                                }
                                            }

                                            Text("Select Your Gender", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)

                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                UserGender.entries.forEach { g ->
                                                    FilterChip(
                                                        selected = gender == g,
                                                        onClick = { gender = g },
                                                        label = { Text("${g.icon} ${g.label}", fontWeight = FontWeight.Bold) },
                                                        colors = FilterChipDefaults.filterChipColors(
                                                            selectedContainerColor = OrangePrimary,
                                                            selectedLabelColor = Color.White,
                                                            containerColor = Color.White.copy(alpha = 0.08f),
                                                            labelColor = Color.White.copy(alpha = 0.7f)
                                                        ),
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Text("Primary Fitness Goal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)

                                            UserFitnessGoal.entries.forEach { goal ->
                                                val isSelected = fitnessGoal == goal
                                                Surface(
                                                    onClick = { fitnessGoal = goal },
                                                    color = if (isSelected) OrangePrimary.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f),
                                                    shape = RoundedCornerShape(16.dp),
                                                    border = androidx.compose.foundation.BorderStroke(
                                                        1.dp,
                                                        if (isSelected) OrangePrimary else Color.White.copy(alpha = 0.15f)
                                                    ),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(14.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                                    ) {
                                                        Text(goal.emoji, fontSize = 24.sp)
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(goal.title, fontWeight = FontWeight.Bold, color = Color.White)
                                                            Text(goal.description, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                                                        }
                                                        if (isSelected) {
                                                            Icon(Icons.Default.Check, contentDescription = null, tint = OrangeSecondary)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            1 -> {
                                // STEP 2: MEASUREMENTS (Height, Weight, Age)
                                item {
                                    GlassCardContainer {
                                        Column(
                                            modifier = Modifier.padding(20.dp),
                                            verticalArrangement = Arrangement.spacedBy(14.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("Physical Measurements", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)

                                                Surface(
                                                    onClick = { isImperial = !isImperial },
                                                    color = Color.White.copy(alpha = 0.08f),
                                                    shape = RoundedCornerShape(12.dp),
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, OrangePrimary.copy(alpha = 0.4f))
                                                ) {
                                                    Text(
                                                        text = if (isImperial) "Imperial (lbs/in)" else "Metric (kg/cm)",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = OrangeSecondary,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }

                                            // Height Input
                                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                OutlinedTextField(
                                                    value = heightText,
                                                    onValueChange = { input ->
                                                        heightText = input.filter { it.isDigit() || it == '.' }
                                                    },
                                                    label = { Text(if (isImperial) "Height (inches)" else "Height (cm)", color = OrangePrimary) },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(14.dp),
                                                    singleLine = true,
                                                    isError = heightError != null,
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    colors = inputColors()
                                                )
                                                heightError?.let {
                                                    Text(it, style = MaterialTheme.typography.labelSmall, color = Color(0xFFFF5252))
                                                }
                                            }

                                            // Weight Input
                                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                OutlinedTextField(
                                                    value = weightText,
                                                    onValueChange = { input ->
                                                        weightText = input.filter { it.isDigit() || it == '.' }
                                                    },
                                                    label = { Text(if (isImperial) "Current Weight (lbs)" else "Current Weight (kg)", color = OrangePrimary) },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(14.dp),
                                                    singleLine = true,
                                                    isError = weightError != null,
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    colors = inputColors()
                                                )
                                                weightError?.let {
                                                    Text(it, style = MaterialTheme.typography.labelSmall, color = Color(0xFFFF5252))
                                                }
                                            }

                                            // Age Input
                                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                OutlinedTextField(
                                                    value = ageText,
                                                    onValueChange = { input ->
                                                        ageText = input.filter { it.isDigit() }
                                                    },
                                                    label = { Text("Age (years)", color = OrangePrimary) },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(14.dp),
                                                    singleLine = true,
                                                    isError = ageError != null,
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    colors = inputColors()
                                                )
                                                ageError?.let {
                                                    Text(it, style = MaterialTheme.typography.labelSmall, color = Color(0xFFFF5252))
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            2 -> {
                                // STEP 3: ACTIVITY LEVEL & EXPERIENCE
                                item {
                                    GlassCardContainer {
                                        Column(
                                            modifier = Modifier.padding(20.dp),
                                            verticalArrangement = Arrangement.spacedBy(14.dp)
                                        ) {
                                            Text("Activity & Training Level", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)

                                            Surface(
                                                color = Color.White.copy(alpha = 0.08f),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Text(
                                                    text = "You're almost set to go! 🚀",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = OrangeSecondary,
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                                )
                                            }

                                            UserActivityLevel.entries.forEach { act ->
                                                val isSelected = activityLevel == act
                                                Surface(
                                                    onClick = { activityLevel = act },
                                                    color = if (isSelected) OrangePrimary.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f),
                                                    shape = RoundedCornerShape(14.dp),
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) OrangePrimary else Color.White.copy(alpha = 0.15f)),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(12.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(act.title, fontWeight = FontWeight.Bold, color = Color.White)
                                                            Text(act.description, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                                                        }
                                                        if (isSelected) {
                                                            Icon(Icons.Default.Check, contentDescription = null, tint = OrangeSecondary)
                                                        }
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Text("Training Experience", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)

                                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                UserExperienceLevel.entries.forEach { exp ->
                                                    val isSelected = experienceLevel == exp
                                                    Surface(
                                                        onClick = { experienceLevel = exp },
                                                        color = if (isSelected) OrangePrimary.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f),
                                                        shape = RoundedCornerShape(14.dp),
                                                        border = androidx.compose.foundation.BorderStroke(
                                                            1.dp,
                                                            if (isSelected) OrangePrimary else Color.White.copy(alpha = 0.15f)
                                                        ),
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.SpaceBetween
                                                        ) {
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                            ) {
                                                                Text(
                                                                    text = when (exp) {
                                                                        UserExperienceLevel.BEGINNER -> "🌱"
                                                                        UserExperienceLevel.INTERMEDIATE -> "⚡"
                                                                        UserExperienceLevel.ADVANCED -> "🔥"
                                                                    },
                                                                    fontSize = 18.sp
                                                                )
                                                                Text(
                                                                    text = exp.title,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = Color.White,
                                                                    style = MaterialTheme.typography.bodyMedium
                                                                )
                                                            }
                                                            if (isSelected) {
                                                                Icon(Icons.Default.Check, contentDescription = null, tint = OrangeSecondary, modifier = Modifier.size(18.dp))
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            else -> {
                                // STEP 4: PROFILE PICTURE & 6 NEON GLASS ATHLETIC AVATARS (FINAL STEP)
                                item {
                                    GlassCardContainer {
                                        Column(
                                            modifier = Modifier.padding(22.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            Text("Profile Picture & Neon Avatars", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                            Text(
                                                text = "Pick one of 6 Neon Athletic Avatars below to personalize your profile!",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White.copy(alpha = 0.7f)
                                            )

                                            // Profile Image Circle Preview
                                            Box(
                                                modifier = Modifier
                                                    .size(100.dp)
                                                    .clip(CircleShape)
                                                    .background(OrangePrimary.copy(alpha = 0.2f))
                                                    .border(2.dp, OrangePrimary, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (profileImageName != null) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Text(
                                                            text = neonAvatarsList.firstOrNull { it.name == profileImageName }?.emoji ?: "👤",
                                                            fontSize = 36.sp
                                                        )
                                                    }
                                                } else {
                                                    Icon(Icons.Default.Person, contentDescription = "Default Profile", tint = OrangeSecondary, modifier = Modifier.size(56.dp))
                                                }
                                            }

                                            if (profileImageName != null) {
                                                Surface(
                                                    color = Color.White.copy(alpha = 0.08f),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Text("Avatar: $profileImageName", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White)
                                                        IconButton(onClick = { profileImageName = null }, modifier = Modifier.size(20.dp)) {
                                                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.Red, modifier = Modifier.size(16.dp))
                                                        }
                                                    }
                                                }
                                            }

                                            // 6 Neon Glass Athletic Avatars Selection Grid
                                            Text("Pick a Neon Athlete Avatar ⚡", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = OrangeSecondary)

                                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                val chunkedAvatars = neonAvatarsList.chunked(2)
                                                chunkedAvatars.forEach { pair ->
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        pair.forEach { avatar ->
                                                            val isSelected = profileImageName == avatar.name
                                                            Surface(
                                                                onClick = { profileImageName = avatar.name },
                                                                color = if (isSelected) avatar.badgeColor.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f),
                                                                shape = RoundedCornerShape(14.dp),
                                                                border = androidx.compose.foundation.BorderStroke(
                                                                    1.dp,
                                                                    if (isSelected) avatar.badgeColor else Color.White.copy(alpha = 0.15f)
                                                                ),
                                                                modifier = Modifier.weight(1f)
                                                            ) {
                                                                Row(
                                                                    modifier = Modifier.padding(10.dp),
                                                                    verticalAlignment = Alignment.CenterVertically,
                                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                                ) {
                                                                    Text(avatar.emoji, fontSize = 20.sp)
                                                                    Text(avatar.name, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            if (submitErrorMsg != null) {
                                                Surface(
                                                    color = Color(0x33FF5252),
                                                    shape = RoundedCornerShape(12.dp),
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252))
                                                ) {
                                                    Text(
                                                        text = submitErrorMsg ?: "",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = Color(0xFFFF5252),
                                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
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

                // Bottom Navigation Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (pagerState.currentPage > 0) {
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            },
                            enabled = !isSubmitting,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                                Text("Previous", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (pagerState.currentPage == 0 && !isStep1Valid) {
                                showNameError = true
                                return@Button
                            }

                            if (pagerState.currentPage < 3) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            } else {
                                // Step 4 (Final Step): Submit Profile Completion API to Backend
                                coroutineScope.launch {
                                    isSubmitting = true
                                    submitErrorMsg = null
                                    val result = authRepository?.submitProfileCompletion(currentProfile)
                                    if (result == null || result is NetworkResult.Success) {
                                        isConfettiActive = true
                                        delay(1200L.milliseconds)
                                        onSetupComplete(currentProfile)
                                    } else if (result is NetworkResult.Error) {
                                        submitErrorMsg = result.message
                                    } else if (result is NetworkResult.Exception) {
                                        submitErrorMsg = result.throwable.message ?: "An error occurred during profile setup"
                                    }
                                    isSubmitting = false
                                }
                            }
                        },
                        enabled = isCurrentStepValid && !isSubmitting,
                        modifier = Modifier
                            .weight(if (pagerState.currentPage > 0) 1.2f else 1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrangePrimary,
                            disabledContainerColor = Color.White.copy(alpha = 0.12f)
                        )
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = if (pagerState.currentPage < 3) "Next Step" else "Complete Setup 🚀",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                                if (pagerState.currentPage < 3) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Celebration Confetti Particle Burst Canvas Overlay
        if (isConfettiActive) {
            ConfettiBurstOverlay()
        }
    }
}

@Composable
private fun ConfettiBurstOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val animProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confettiProg"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val confettiColors = listOf(
            Color(0xFFFF5722), Color(0xFFFF9800), Color(0xFF2196F3),
            Color(0xFF4CAF50), Color(0xFFFFC107), Color(0xFFE91E63)
        )

        val randomPositions = listOf(
            Offset(w * 0.15f, h * (0.15f + animProgress * 0.7f)),
            Offset(w * 0.30f, h * (0.05f + animProgress * 0.8f)),
            Offset(w * 0.45f, h * (0.02f + animProgress * 0.85f)),
            Offset(w * 0.60f, h * (0.10f + animProgress * 0.8f)),
            Offset(w * 0.75f, h * (0.08f + animProgress * 0.75f)),
            Offset(w * 0.90f, h * (0.20f + animProgress * 0.7f)),
            Offset(w * 0.20f, h * (0.25f + animProgress * 0.6f)),
            Offset(w * 0.50f, h * (0.18f + animProgress * 0.65f)),
            Offset(w * 0.80f, h * (0.22f + animProgress * 0.6f))
        )

        randomPositions.forEachIndexed { index, pos ->
            val color = confettiColors[index % confettiColors.size]
            drawCircle(color = color, radius = 12f, center = pos)
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
private fun GlassCardContainer(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
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
