package com.muscleplay.app.feature.diet

import androidx.compose.runtime.Composable
import com.muscleplay.app.feature.diet.data.MealDao

@Composable
expect fun IngredientScannerScreen(
    mealDao: MealDao,
    onBack: () -> Unit
)
