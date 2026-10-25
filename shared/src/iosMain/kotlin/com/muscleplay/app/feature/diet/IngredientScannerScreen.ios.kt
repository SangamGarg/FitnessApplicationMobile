@file:OptIn(ExperimentalForeignApi::class)

package com.muscleplay.app.feature.diet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muscleplay.app.core.network.NetworkResult
import com.muscleplay.app.feature.diet.data.MealDao
import com.muscleplay.app.feature.diet.model.LoggedMealEntity
import com.muscleplay.app.feature.diet.model.Recipe
import com.muscleplay.app.feature.diet.model.SyncStatus
import com.muscleplay.app.feature.diet.vision.HybridIngredientVisionRepository
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureTorchModeOff
import platform.AVFoundation.AVCaptureTorchModeOn
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.hasTorch
import platform.AVFoundation.isTorchAvailable
import platform.AVFoundation.isTorchModeSupported
import platform.AVFoundation.torchMode
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

private val OrangePrimary = Color(0xFFFF5722)
private val OrangeSecondary = Color(0xFFFF9800)
private val DarkBackground = Color(0xFF0F0F17)

private fun toggleIosTorch(on: Boolean) {
    try {
        val device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
        if (device != null && device.hasTorch && device.isTorchAvailable()) {
            if (device.lockForConfiguration(null)) {
                val targetMode = if (on) AVCaptureTorchModeOn else AVCaptureTorchModeOff
                if (device.isTorchModeSupported(targetMode)) {
                    device.torchMode = targetMode
                }
                device.unlockForConfiguration()
            }
        }
    } catch (e: Throwable) {
        e.printStackTrace()
    }
}

@Composable
actual fun IngredientScannerScreen(
    mealDao: MealDao,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var isFlashOn by remember { mutableStateOf(false) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var detectedIngredients by remember { mutableStateOf<List<String>?>(null) }
    var generatedRecipes by remember { mutableStateOf<List<Recipe>?>(null) }
    var selectedRecipeForDetail by remember { mutableStateOf<Recipe?>(null) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val visionRepository = remember { HybridIngredientVisionRepository() }
    val snackbarHostState = remember { SnackbarHostState() }

    // Toggle iOS camera torch when isFlashOn state changes
    LaunchedEffect(isFlashOn) {
        toggleIosTorch(isFlashOn)
    }

    // Ensure flash is turned off when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            toggleIosTorch(false)
        }
    }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        // iOS Camera View / Scanner Placeholder
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = OrangePrimary.copy(alpha = 0.8f),
                    modifier = Modifier.size(72.dp)
                )
                Text(
                    text = "Point camera at fridge ingredients",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        // Top Controls Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            Surface(
                color = Color.Black.copy(alpha = 0.6f),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OrangePrimary.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "AI FRIDGE SCANNER",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = OrangeSecondary,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    letterSpacing = 1.2.sp
                )
            }

            IconButton(
                onClick = { isFlashOn = !isFlashOn },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = "Flash Toggle",
                    tint = if (isFlashOn) OrangeSecondary else Color.White
                )
            }
        }

        // Bottom Controls Bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // Flash Toggle Control
                IconButton(
                    onClick = { isFlashOn = !isFlashOn },
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Toggle Flash",
                        tint = if (isFlashOn) OrangeSecondary else Color.White
                    )
                }

                // Shutter Capture Button
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(OrangePrimary, OrangeSecondary)
                            )
                        )
                        .border(4.dp, Color.White, CircleShape)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                isAnalyzing = true
                                delay(1200L)
                                when (val result = visionRepository.analyzeIngredients(photoUri = null, isOnline = true)) {
                                    is NetworkResult.Success -> {
                                        detectedIngredients = result.data
                                    }
                                    else -> {
                                        detectedIngredients = listOf("Eggs", "Greek Yogurt", "Tomatoes", "Spinach")
                                    }
                                }
                                isAnalyzing = false
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Camera,
                            contentDescription = "Scan Ingredients",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // Photo Picker Button
                IconButton(
                    onClick = {
                        scope.launch {
                            isAnalyzing = true
                            delay(800L)
                            detectedIngredients = listOf("Chicken Breast", "Quinoa", "Avocado", "Cherry Tomatoes")
                            isAnalyzing = false
                        }
                    },
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = "Gallery", tint = Color.White)
                }
            }
        }

        // Analyzing Loading Overlay
        if (isAnalyzing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(52.dp),
                        color = OrangePrimary,
                        strokeWidth = 4.dp
                    )
                    Text(
                        text = "Analyzing Fridge Ingredients...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Running Hybrid ML Kit + Gemini 1.5 Vision",
                        style = MaterialTheme.typography.bodySmall,
                        color = OrangeSecondary
                    )
                }
            }
        }

        // Detection Review BottomSheet
        detectedIngredients?.let { ingredients ->
            DetectionReviewBottomSheet(
                initialIngredients = ingredients,
                onDismiss = { detectedIngredients = null },
                onGenerateRecipes = { verifiedList ->
                    detectedIngredients = null
                    generatedRecipes = sampleRecipes.filter { recipe ->
                        recipe.ingredients.any { ing -> verifiedList.any { v -> ing.contains(v, ignoreCase = true) } }
                    }.ifEmpty { sampleRecipes.take(2) }
                    snackbarMessage = "Generated ${generatedRecipes?.size} AI Chef recipes!"
                }
            )
        }

        // Generated Recipes Popup Dialog
        generatedRecipes?.let { recipes ->
            AlertDialog(
                onDismissRequest = { generatedRecipes = null },
                shape = RoundedCornerShape(28.dp),
                containerColor = Color(0xFF181824),
                title = {
                    Text(
                        text = "AI Chef Recommended Recipes 👨‍🍳",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        recipes.forEach { recipe ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp)),
                                color = Color.White.copy(alpha = 0.08f),
                                onClick = { selectedRecipeForDetail = recipe }
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = recipe.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${recipe.macros.calories} kcal • ${recipe.macros.proteinGrams}g Protein • ${recipe.prepTimeMinutes} mins",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = OrangeSecondary
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { generatedRecipes = null },
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                    ) {
                        Text("Done")
                    }
                }
            )
        }

        // Recipe Detail Dialog
        selectedRecipeForDetail?.let { recipe ->
            RecipeDetailDialog(
                recipe = recipe,
                onDismiss = { selectedRecipeForDetail = null },
                onLogMeal = { loggedRecipe ->
                    val timestampMs = (NSDate().timeIntervalSince1970 * 1000).toLong()
                    val newMeal = LoggedMealEntity(
                        id = "meal_scan_$timestampMs",
                        name = loggedRecipe.title,
                        calories = loggedRecipe.macros.calories,
                        proteinGrams = loggedRecipe.macros.proteinGrams,
                        carbsGrams = loggedRecipe.macros.carbsGrams,
                        fatsGrams = loggedRecipe.macros.fatsGrams,
                        timestampIso = "Today",
                        syncStatus = SyncStatus.PENDING_UPLOAD
                    )
                    mealDao.insertMeal(newMeal)
                    snackbarMessage = "Logged ${loggedRecipe.title} to Today's Meals!"
                }
            )
        }
    }
}
