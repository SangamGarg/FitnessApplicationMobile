package com.muscleplay.app.feature.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muscleplay.app.feature.onboarding.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

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
    onSetupComplete: (UserOnboardingProfile) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 5 })
    val coroutineScope = rememberCoroutineScope()

    // User Profile State
    var fullNameText by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf(UserGender.MALE) }
    var fitnessGoal by remember { mutableStateOf(UserFitnessGoal.FAT_LOSS) }
    var isImperial by remember { mutableStateOf(false) }

    var heightText by remember { mutableStateOf("175") }
    var weightText by remember { mutableStateOf("70") }
    var targetWeightText by remember { mutableStateOf("65") }
    var ageText by remember { mutableStateOf("25") }

    var activityLevel by remember { mutableStateOf(UserActivityLevel.MODERATELY_ACTIVE) }
    var experienceLevel by remember { mutableStateOf(UserExperienceLevel.INTERMEDIATE) }

    var profileImageName by remember { mutableStateOf<String?>(null) }
    var showNameError by remember { mutableStateOf(false) }
    var isConfettiActive by remember { mutableStateOf(false) }

    // Step Validations
    val isStep1Valid = fullNameText.trim().length >= 2

    val rawHeight = heightText.toDoubleOrNull() ?: 0.0
    val rawWeight = weightText.toDoubleOrNull() ?: 0.0
    val rawTargetWeight = targetWeightText.toDoubleOrNull() ?: 0.0
    val age = ageText.toIntOrNull() ?: 0

    val minH = if (isImperial) 20.0 else 50.0
    val maxH = if (isImperial) 120.0 else 300.0
    val minW = if (isImperial) 44.0 else 20.0
    val maxW = if (isImperial) 660.0 else 300.0

    val heightError = when {
        heightText.isBlank() -> "Height is required"
        rawHeight <= 0 -> "Height cannot be 0"
        rawHeight !in minH..maxH -> "Enter valid height (${minH.toInt()} - ${maxH.toInt()})"
        else -> null
    }

    val weightError = when {
        weightText.isBlank() -> "Weight is required"
        rawWeight <= 0 -> "Weight cannot be 0"
        rawWeight !in minW..maxW -> "Enter valid weight (${minW.toInt()} - ${maxW.toInt()})"
        else -> null
    }

    val targetWeightError = when {
        targetWeightText.isBlank() -> "Target weight is required"
        rawTargetWeight <= 0 -> "Target weight cannot be 0"
        rawTargetWeight !in minW..maxW -> "Enter valid target weight (${minW.toInt()} - ${maxW.toInt()})"
        else -> null
    }

    val ageError = when {
        ageText.isBlank() -> "Age is required"
        age <= 0 -> "Age cannot be 0"
        age !in 10..120 -> "Age must be between 10 and 120"
        else -> null
    }

    val isStep2Valid = heightError == null && weightError == null && targetWeightError == null && ageError == null

    val heightCm = if (isImperial) rawHeight * 2.54 else rawHeight
    val weightKg = if (isImperial) rawWeight / 2.20462 else rawWeight
    val targetWeightKg = if (isImperial) rawTargetWeight / 2.20462 else rawTargetWeight

    val currentProfile = remember(fullNameText, gender, fitnessGoal, heightCm, weightKg, targetWeightKg, age, activityLevel, experienceLevel, profileImageName, isImperial) {
        UserOnboardingProfile(
            fullName = fullNameText.trim(),
            gender = gender,
            fitnessGoal = fitnessGoal,
            heightCm = heightCm,
            weightKg = weightKg,
            targetWeightKg = targetWeightKg,
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
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            }) {
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
                // Step Progress Indicator Bar
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "STEP ${pagerState.currentPage + 1} OF 5",
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
                                3 -> "Profile Picture (Optional)"
                                else -> "AI Plan Computation"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = OrangeSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    LinearProgressIndicator(
                        progress = { (pagerState.currentPage + 1) / 5f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = OrangePrimary,
                        trackColor = Color.White.copy(alpha = 0.1f)
                    )
                }

                // Horizontal Pager for Smooth Swiping between 5 Steps
                HorizontalPager(
                    state = pagerState,
                    userScrollEnabled = isCurrentStepValid,
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
                                                    onValueChange = {
                                                        fullNameText = it
                                                        showNameError = false
                                                    },
                                                    placeholder = { Text("e.g. Alex Johnson", color = Color.White.copy(alpha = 0.4f)) },
                                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = OrangePrimary) },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(16.dp),
                                                    singleLine = true,
                                                    isError = showNameError || (fullNameText.isNotEmpty() && !isStep1Valid),
                                                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                                                    colors = inputColors()
                                                )
                                                if (!isStep1Valid && (showNameError || fullNameText.isNotEmpty())) {
                                                    Text("Please enter a valid full name (at least 2 characters)", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFF5252))
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
                                // STEP 2: MEASUREMENTS
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

                                            // Target Weight Input
                                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                OutlinedTextField(
                                                    value = targetWeightText,
                                                    onValueChange = { input ->
                                                        targetWeightText = input.filter { it.isDigit() || it == '.' }
                                                    },
                                                    label = { Text(if (isImperial) "Target Goal Weight (lbs)" else "Target Goal Weight (kg)", color = OrangePrimary) },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(14.dp),
                                                    singleLine = true,
                                                    isError = targetWeightError != null,
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    colors = inputColors()
                                                )
                                                targetWeightError?.let {
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

                            3 -> {
                                // STEP 4: SKIPPABLE PROFILE PICTURE & 6 NEON GLASS ATHLETIC AVATARS
                                item {
                                    GlassCardContainer {
                                        Column(
                                            modifier = Modifier.padding(22.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            Text("Profile Picture & Neon Avatars", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                            Text(
                                                text = "Upload a photo, capture a selfie, or pick one of 6 Neon Athletic Avatars below!",
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
                                                            text = neonAvatarsList.firstOrNull { it.name == profileImageName }?.emoji ?: "📸",
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
                                            Text("Or Pick a Neon Athlete Avatar ⚡", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = OrangeSecondary)

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

                                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                                            // Camera & Gallery Upload Buttons
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Button(
                                                    onClick = {
                                                        profileImageName = "Selfie Camera ${Random.nextInt(100, 999)}"
                                                    },
                                                    modifier = Modifier.weight(1f).height(44.dp),
                                                    shape = RoundedCornerShape(14.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        Text("Camera 📸", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }

                                                OutlinedButton(
                                                    onClick = {
                                                        profileImageName = "Gallery Photo ${Random.nextInt(100, 999)}"
                                                    },
                                                    modifier = Modifier.weight(1f).height(44.dp),
                                                    shape = RoundedCornerShape(14.dp),
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, OrangeSecondary),
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangeSecondary)
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        Icon(Icons.Default.Collections, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        Text("Gallery 🖼️", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            else -> {
                                // STEP 5: AI COMPUTATION, PLAN SUMMARY & CELEBRATION
                                item {
                                    GlassCardContainer {
                                        Column(
                                            modifier = Modifier.padding(22.dp),
                                            verticalArrangement = Arrangement.spacedBy(14.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(42.dp)
                                                        .clip(CircleShape)
                                                        .background(OrangePrimary.copy(alpha = 0.2f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = OrangeSecondary)
                                                }
                                                Column {
                                                    Text("Your AI Plan is Ready, ${currentProfile.fullName.ifBlank { "Athlete" }}! 🎯", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                                    Text("Computed based on your profile & goal", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                                                }
                                            }

                                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                StatSummaryBox("Daily Target", "${currentProfile.targetDailyCalories} kcal", OrangePrimary, Modifier.weight(1f))
                                                StatSummaryBox("BMR Base", "${currentProfile.bmrCalories} kcal", OrangeSecondary, Modifier.weight(1f))
                                            }

                                            Text("Daily Target Macro Ratios", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White)

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                MacroBox("Protein", "${currentProfile.targetProteinGrams}g", OrangePrimary, Modifier.weight(1f))
                                                MacroBox("Carbs", "${currentProfile.targetCarbsGrams}g", OrangeSecondary, Modifier.weight(1f))
                                                MacroBox("Fats", "${currentProfile.targetFatGrams}g", Color(0xFF2196F3), Modifier.weight(1f))
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

                            if (pagerState.currentPage < 4) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            } else {
                                // Step 5: Celebration Confetti Burst before completing setup
                                isConfettiActive = true
                                coroutineScope.launch {
                                    delay(1400L)
                                    onSetupComplete(currentProfile)
                                }
                            }
                        },
                        enabled = isCurrentStepValid,
                        modifier = Modifier
                            .weight(if (pagerState.currentPage > 0) 1.2f else 1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrangePrimary,
                            disabledContainerColor = Color.White.copy(alpha = 0.12f)
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = if (pagerState.currentPage < 4) "Next Step" else "Complete Setup 🚀",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                            if (pagerState.currentPage < 4) {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
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
private fun StatSummaryBox(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Color.White)
        }
    }
}

@Composable
private fun MacroBox(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, color = Color.White)
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
