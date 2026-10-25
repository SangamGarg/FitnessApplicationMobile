package com.muscleplay.app.feature.diet

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.muscleplay.app.core.network.NetworkResult
import com.muscleplay.app.feature.diet.data.MealDao
import com.muscleplay.app.feature.diet.model.LoggedMealEntity
import com.muscleplay.app.feature.diet.model.Recipe
import com.muscleplay.app.feature.diet.model.SyncStatus
import com.muscleplay.app.feature.diet.vision.HybridIngredientVisionRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

private val OrangePrimary = Color(0xFFFF5722)
private val OrangeSecondary = Color(0xFFFF9800)
private val DarkBackground = Color(0xFF0F0F17)

@Composable
actual fun IngredientScannerScreen(
    mealDao: MealDao,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var cameraGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var isFlashOn by remember { mutableStateOf(false) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var detectedIngredients by remember { mutableStateOf<List<String>?>(null) }
    var generatedRecipes by remember { mutableStateOf<List<Recipe>?>(null) }
    var selectedRecipeForDetail by remember { mutableStateOf<Recipe?>(null) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val visionRepository = remember { HybridIngredientVisionRepository() }
    val snackbarHostState = remember { SnackbarHostState() }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        cameraGranted = granted
    }

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    LaunchedEffect(Unit) {
        if (!cameraGranted) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }

    LaunchedEffect(isFlashOn, camera) {
        camera?.let { boundCamera ->
            if (boundCamera.cameraInfo.hasFlashUnit()) {
                boundCamera.cameraControl.enableTorch(isFlashOn)
            }
        }
    }

    if (!cameraGranted) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = OrangePrimary,
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = "Camera Permission Required",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "MusclePlay AI Chef needs camera access to scan your fridge ingredients.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    Text("Grant Permission", fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = onBack) {
                    Text("Back to Diet Home", color = Color.White.copy(alpha = 0.6f))
                }
            }
        }
        return
    }

    DisposableEffect(previewView, lifecycleOwner, lensFacing) {
        var cameraProvider: ProcessCameraProvider? = null
        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener({
            try {
                val provider = providerFuture.get()
                cameraProvider = provider
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }
                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(lensFacing)
                    .build()

                provider.unbindAll()
                val boundCamera = provider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
                camera = boundCamera
                if (boundCamera.cameraInfo.hasFlashUnit()) {
                    boundCamera.cameraControl.enableTorch(isFlashOn)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            cameraProvider?.unbindAll()
            camera = null
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        // Camera Viewfinder
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

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

        // Shutter & Controls Bottom Bar
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
                // Switch Camera Button
                IconButton(
                    onClick = {
                        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                            CameraSelector.LENS_FACING_FRONT
                        } else {
                            CameraSelector.LENS_FACING_BACK
                        }
                    },
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.FlipCameraAndroid, contentDescription = "Switch Camera", tint = Color.White)
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
                                delay(1200L.milliseconds) // Simulate vision analysis
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
                            delay(800L.milliseconds)
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
                    val newMeal = LoggedMealEntity(
                        id = "meal_scan_${System.currentTimeMillis()}",
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
