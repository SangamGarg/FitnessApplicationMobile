
package com.muscleplay.app.core.di

import com.muscleplay.app.feature.workouts.InMemoryWorkoutRepository
import com.muscleplay.app.feature.workouts.WorkoutRepository

class AppContainer {

    val workoutRepository: WorkoutRepository =
        InMemoryWorkoutRepository()
}
