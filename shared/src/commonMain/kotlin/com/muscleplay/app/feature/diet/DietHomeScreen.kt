package com.muscleplay.app.feature.diet

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muscleplay.app.feature.diet.data.InMemoryMealDao
import com.muscleplay.app.feature.diet.data.MealDao
import com.muscleplay.app.feature.diet.data.LoggedMealTotals
import com.muscleplay.app.feature.diet.model.*

private val OrangePrimary = Color(0xFFFF5722)
private val OrangeSecondary = Color(0xFFFF9800)
private val OrangeGlow = Color(0xFFFF3D00)
private val DarkBackground = Color(0xFF0F0F17)

val sampleRecipes = listOf(
    Recipe(
        id = "r1",
        title = "High-Protein Scrambled Eggs & Avocado",
        prepTimeMinutes = 12,
        difficulty = "Easy",
        macros = MacroInfo(380, 26, 12, 22),
        dietType = DietaryPreference.EGGETARIAN,
        ingredients = listOf("3 Whole Eggs", "1/2 Avocado", "1/4 Cup Spinach", "1 Toast Slice", "Olive Oil"),
        instructions = listOf(
            "Whisk eggs with a splash of milk and pinch of salt.",
            "Heat olive oil in a non-stick pan over medium heat.",
            "Sauté spinach until wilted, then add whisked eggs.",
            "Serve warm alongside sliced avocado on whole wheat toast."
        )
    ),
    Recipe(
        id = "r2",
        title = "Grilled Chicken Quinoa Muscle Bowl",
        prepTimeMinutes = 20,
        difficulty = "Medium",
        macros = MacroInfo(560, 48, 52, 14),
        dietType = DietaryPreference.NON_VEG,
        ingredients = listOf("200g Chicken Breast", "1 Cup Quinoa", "Cucumber", "Cherry Tomatoes", "Lemon Dressing"),
        instructions = listOf(
            "Season chicken breast with paprika, garlic powder, and black pepper.",
            "Grill chicken on medium heat for 6-8 minutes per side.",
            "Assemble cooked quinoa, sliced chicken, cucumber, and tomatoes in a bowl.",
            "Drizzle with fresh lemon juice and olive oil."
        )
    ),
    Recipe(
        id = "r3",
        title = "Greek Yogurt & Berry Protein Parfait",
        prepTimeMinutes = 8,
        difficulty = "Easy",
        macros = MacroInfo(320, 30, 36, 6),
        dietType = DietaryPreference.VEG,
        ingredients = listOf("1.5 Cups Greek Yogurt", "Mixed Berries", "1 Scoop Whey Protein", "1 tbsp Honey", "Almonds"),
        instructions = listOf(
            "Mix whey protein powder smoothly into Greek yogurt.",
            "Layer yogurt mixture with fresh berries in a glass bowl.",
            "Top with sliced almonds and honey."
        )
    ),
    Recipe(
        id = "r4",
        title = "Tofu & Broccoli High-Fiber Stir-Fry",
        prepTimeMinutes = 15,
        difficulty = "Easy",
        macros = MacroInfo(410, 28, 40, 16),
        dietType = DietaryPreference.VEGAN,
        ingredients = listOf("200g Firm Tofu", "Broccoli Florets", "Soy Sauce", "Sesame Seeds", "Garlic & Ginger"),
        instructions = listOf(
            "Cube tofu and sear in sesame oil until golden brown.",
            "Sauté broccoli with minced garlic and ginger.",
            "Toss tofu and broccoli together with low-sodium soy sauce."
        )
    )
)

@Composable
fun DietHomeScreen(
    mealDao: MealDao = remember { InMemoryMealDao() },
    onNavigateToScanner: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    var selectedDietaryPref by remember { mutableStateOf(DietaryPreference.ALL) }
    var selectedGoal by remember { mutableStateOf(FitnessGoal.MUSCLE_GAIN) }
    var selectedRecipeForDetail by remember { mutableStateOf<Recipe?>(null) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val loggedMeals by mealDao.getLoggedMealsFlow().collectAsState()
    val totals = remember(loggedMeals) { mealDao.getDailyTotals() }

    val filteredRecipes = remember(selectedDietaryPref) {
        if (selectedDietaryPref == DietaryPreference.ALL) sampleRecipes
        else sampleRecipes.filter { it.dietType == selectedDietaryPref || it.dietType == DietaryPreference.ALL }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    // Ambient Glowing Background Pulsing Animation
    val infiniteTransition = rememberInfiniteTransition(label = "dietPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
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
        // Glowing Ambient Background Orbs
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
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.padding(bottom = 88.dp, start = 16.dp, end = 16.dp)
                ) { data ->
                    Snackbar(
                        snackbarData = data,
                        shape = RoundedCornerShape(16.dp),
                        containerColor = Color(0xFF181824),
                        contentColor = Color.White,
                        actionColor = OrangeSecondary,
                        dismissActionContentColor = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
            ) {
                // Header with Back Button
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            onClick = onBack,
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Diet & AI Chef 🥗",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "Smart macros, recipes & fridge scanner",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // Daily Macro Progress Tracker Card
                item {
                    GlassDailyMacroTrackerCard(totals = totals)
                }

                // AI Fridge Scanner Hero Card
                item {
                    GlassAiScannerHeroCard(onNavigateToScanner = onNavigateToScanner)
                }

                // Preferences & Goals Filters
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Dietary Preference",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(DietaryPreference.entries) { pref ->
                                FilterChip(
                                    selected = pref == selectedDietaryPref,
                                    onClick = { selectedDietaryPref = pref },
                                    label = { Text(pref.label, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = OrangePrimary,
                                        selectedLabelColor = Color.White,
                                        containerColor = Color.White.copy(alpha = 0.08f),
                                        labelColor = Color.White.copy(alpha = 0.8f)
                                    )
                                )
                            }
                        }

                        Text(
                            text = "Fitness Goal",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(FitnessGoal.entries) { goal ->
                                FilterChip(
                                    selected = goal == selectedGoal,
                                    onClick = { selectedGoal = goal },
                                    label = { Text(goal.label, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = OrangeSecondary,
                                        selectedLabelColor = Color.White,
                                        containerColor = Color.White.copy(alpha = 0.08f),
                                        labelColor = Color.White.copy(alpha = 0.8f)
                                    )
                                )
                            }
                        }
                    }
                }

                // Recommended Recipes Title
                item {
                    Text(
                        text = "Smart Recipes for ${selectedGoal.label}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Recipe Cards List
                items(filteredRecipes) { recipe ->
                    GlassRecipeCard(
                        recipe = recipe,
                        onClick = { selectedRecipeForDetail = recipe }
                    )
                }

                // Today's Logged Meals
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Today's Meals",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${loggedMeals.size} Logged",
                                style = MaterialTheme.typography.bodySmall,
                                color = OrangeSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        loggedMeals.forEach { meal ->
                            GlassLoggedMealRow(meal = meal)
                        }
                    }
                }
            }

            // Recipe Detail Popup Dialog
            selectedRecipeForDetail?.let { recipe ->
                RecipeDetailDialog(
                    recipe = recipe,
                    onDismiss = { selectedRecipeForDetail = null },
                    onLogMeal = { loggedRecipe ->
                        val newMeal = LoggedMealEntity(
                            id = "meal_logged_${loggedMeals.size + 1}",
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
}

@Composable
private fun GlassDailyMacroTrackerCard(totals: LoggedMealTotals) {
    GlassContainer(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "DAILY MACRO TARGETS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = OrangePrimary,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "${totals.totalCalories} / ${totals.targetCalories} kcal",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }

                Surface(
                    color = OrangePrimary.copy(alpha = 0.2f),
                    shape = CircleShape
                ) {
                    Text(
                        text = "${(totals.totalCalories * 100 / totals.targetCalories)}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = OrangeSecondary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Linear Macro Progress Bars
            MacroProgressBar(
                label = "Protein",
                current = totals.totalProtein,
                target = totals.targetProtein,
                unit = "g",
                color = OrangePrimary
            )
            MacroProgressBar(
                label = "Carbs",
                current = totals.totalCarbs,
                target = totals.targetCarbs,
                unit = "g",
                color = OrangeSecondary
            )
            MacroProgressBar(
                label = "Fats",
                current = totals.totalFats,
                target = totals.targetFats,
                unit = "g",
                color = Color(0xFFFFB74D)
            )
        }
    }
}

@Composable
private fun MacroProgressBar(
    label: String,
    current: Int,
    target: Int,
    unit: String,
    color: Color
) {
    val progress = (current.toFloat() / target.toFloat()).coerceIn(0f, 1f)

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
            Text("$current / $target $unit", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = color)
        }

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = color,
            trackColor = Color.White.copy(alpha = 0.1f)
        )
    }
}

@Composable
private fun GlassAiScannerHeroCard(onNavigateToScanner: () -> Unit) {
    GlassContainer(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(OrangePrimary, OrangeSecondary))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "AI Fridge & Ingredient Scanner 📸",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Scan ingredients & generate custom recipes",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            Button(
                onClick = onNavigateToScanner,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Scan Ingredients & Cook", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun GlassRecipeCard(recipe: Recipe, onClick: () -> Unit) {
    GlassContainer(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = OrangePrimary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = recipe.dietType.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = OrangeSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = "${recipe.prepTimeMinutes} mins • ${recipe.difficulty}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            Text(
                text = recipe.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("${recipe.macros.calories} kcal", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.White)
                Text("${recipe.macros.proteinGrams}g Protein", style = MaterialTheme.typography.bodySmall, color = OrangePrimary, fontWeight = FontWeight.Bold)
                Text("${recipe.macros.carbsGrams}g Carbs", style = MaterialTheme.typography.bodySmall, color = OrangeSecondary)
                Text("${recipe.macros.fatsGrams}g Fats", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
private fun GlassLoggedMealRow(meal: LoggedMealEntity) {
    GlassContainer(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(OrangePrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = OrangePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = meal.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${meal.calories} kcal • ${meal.proteinGrams}g P",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            // Sync Status Non-blocking Icon Indicator
            when (meal.syncStatus) {
                SyncStatus.SYNCED -> Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Synced",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(20.dp)
                )
                SyncStatus.PENDING_UPLOAD -> Icon(
                    imageVector = Icons.Default.CloudSync,
                    contentDescription = "Pending Upload",
                    tint = OrangeSecondary,
                    modifier = Modifier.size(20.dp)
                )
                SyncStatus.FAILED -> Icon(
                    imageVector = Icons.Default.SyncProblem,
                    contentDescription = "Sync Failed",
                    tint = Color(0xFFFF5252),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun GlassContainer(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(24.dp),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
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
                shape = shape
            )
    ) {
        content()
    }
}
