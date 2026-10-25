package com.muscleplay.app.feature.diet.sync

import com.muscleplay.app.feature.diet.data.MealDao
import com.muscleplay.app.feature.diet.model.SyncStatus

class MealSyncManager(
    private val mealDao: MealDao
) {
    suspend fun syncPendingMeals(): Int {
        val unsynced = mealDao.getUnsyncedMeals()
        var syncedCount = 0
        unsynced.forEach { meal ->
            mealDao.updateSyncStatus(meal.id, SyncStatus.SYNCED)
            syncedCount++
        }
        return syncedCount
    }
}
