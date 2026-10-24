package com.muscleplay.app.feature.workouts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun WorkoutsScreen(
    repository: WorkoutRepository,
) {
    val workoutViewModel: WorkoutViewModel = viewModel {
        WorkoutViewModel(repository)
    }

    val uiState by workoutViewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Workout Session",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(20.dp))

        Text(
            text = uiState.exerciseName,
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(Modifier.height(12.dp))

        Text("${uiState.completedReps} / ${uiState.targetReps} reps")

        Spacer(Modifier.height(12.dp))

        LinearProgressIndicator(
            progress = {
                uiState.completedReps.toFloat() /
                        uiState.targetReps.toFloat()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        )

        Spacer(Modifier.height(20.dp))

        Text(
            text = if (uiState.isCompleted) {
                "Exercise completed!"
            } else {
                "Keep going!"
            }
        )

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = workoutViewModel::completeRep,
            enabled = !uiState.isCompleted
        ) {
            Text("Complete 1 Rep")
        }

        Button(
            onClick = workoutViewModel::resetWorkout
        ) {
            Text("Reset Workout")
        }
    }
}
