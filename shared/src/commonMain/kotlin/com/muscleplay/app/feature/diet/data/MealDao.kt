package com.muscleplay.app.feature.diet.data

import com.muscleplay.app.feature.diet.model.LoggedMealEntity
import com.muscleplay.app.feature.diet.model.SyncStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface MealDao {
    fun getLoggedMealsFlow(): StateFlow<List<LoggedMealEntity>>
    fun getLoggedMeals(): List<LoggedMealEntity>
    fun getUnsyncedMeals(): List<LoggedMealEntity>
    fun insertMeal(meal: LoggedMealEntity)
    fun updateSyncStatus(mealId: String, newStatus: SyncStatus)
    fun getDailyTotals(): LoggedMealTotals
}

data class LoggedMealTotals(
    val totalCalories: Int,
    val totalProtein: Int,
    val totalCarbs: Int,
    val totalFats: Int,
    val targetCalories: Int = 2200,
    val targetProtein: Int = 150,
    val targetCarbs: Int = 220,
    val targetFats: Int = 65
)

class InMemoryMealDao : MealDao {
    private val initialMeals = mutableListOf(
        LoggedMealEntity(
            id = "meal_101",
            name = "Greek Yogurt & Protein Oats Bowl",
            calories = 420,
            proteinGrams = 32,
            carbsGrams = 48,
            fatsGrams = 8,
            timestampIso = "2025-02-21T08:30:00Z",
            syncStatus = SyncStatus.SYNCED
        ),
        LoggedMealEntity(
            id = "meal_102",
            name = "Grilled Chicken & Quinoa Salad",
            calories = 580,
            proteinGrams = 48,
            carbsGrams = 42,
            fatsGrams = 14,
            timestampIso = "2025-02-21T13:15:00Z",
            syncStatus = SyncStatus.SYNCED
        ),
        LoggedMealEntity(
            id = "meal_103",
            name = "AI Chef Scrambled Eggs & Avocado Toast",
            calories = 360,
            proteinGrams = 22,
            carbsGrams = 28,
            fatsGrams = 16,
            timestampIso = "2025-02-21T17:45:00Z",
            syncStatus = SyncStatus.PENDING_UPLOAD
        )
    )

    private val _mealsFlow = MutableStateFlow<List<LoggedMealEntity>>(initialMeals.toList())

    override fun getLoggedMealsFlow(): StateFlow<List<LoggedMealEntity>> = _mealsFlow.asStateFlow()

    override fun getLoggedMeals(): List<LoggedMealEntity> = _mealsFlow.value

    override fun getUnsyncedMeals(): List<LoggedMealEntity> {
        return _mealsFlow.value.filter { it.syncStatus == SyncStatus.PENDING_UPLOAD }
    }

    override fun insertMeal(meal: LoggedMealEntity) {
        val current = _mealsFlow.value.toMutableList()
        current.add(0, meal)
        _mealsFlow.value = current
    }

    override fun updateSyncStatus(mealId: String, newStatus: SyncStatus) {
        val current = _mealsFlow.value.map { meal ->
            if (meal.id == mealId) meal.copy(syncStatus = newStatus) else meal
        }
        _mealsFlow.value = current
    }

    override fun getDailyTotals(): LoggedMealTotals {
        val meals = _mealsFlow.value
        return LoggedMealTotals(
            totalCalories = meals.sumOf { it.calories },
            totalProtein = meals.sumOf { it.proteinGrams },
            totalCarbs = meals.sumOf { it.carbsGrams },
            totalFats = meals.sumOf { it.fatsGrams }
        )
    }
}
