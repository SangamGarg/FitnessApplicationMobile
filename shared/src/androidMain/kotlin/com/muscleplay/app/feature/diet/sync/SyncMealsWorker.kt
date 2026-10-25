package com.muscleplay.app.feature.diet.sync

import android.content.Context
import com.muscleplay.app.feature.diet.data.InMemoryMealDao
import com.muscleplay.app.feature.diet.data.MealDao

class SyncMealsWorker(
    private val context: Context,
    private val mealDao: MealDao = InMemoryMealDao()
) {
    private val syncManager = MealSyncManager(mealDao)

    suspend fun doWork(): Boolean {
        val count = syncManager.syncPendingMeals()
        return count >= 0
    }
}
