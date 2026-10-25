package com.muscleplay.app.feature.workout

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
fun WorkoutScreen(
    repository: WorkoutRepository,
    onDone: () -> Unit = {},
) {
    val workoutViewModel: WorkoutViewModel = viewModel {
        WorkoutViewModel(repository)
    }

    val uiState by workoutViewModel.uiState.collectAsStateWithLifecycle()
    val summary by workoutViewModel.summary.collectAsStateWithLifecycle()

    val finishedSummary = summary
    if (finishedSummary != null) {
        WorkoutSummaryScreen(
            summary = finishedSummary,
            onDone = {
                workoutViewModel.resetWorkout()
                onDone()
            },
            onRepeatWorkout = workoutViewModel::startAnotherWorkout
        )
        return
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Workout Session", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(20.dp))

        Text(uiState.exerciseName, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        Text("${uiState.completedReps} / ${uiState.targetReps} reps")
        Spacer(Modifier.height(12.dp))

        LinearProgressIndicator(
            progress = {
                if (uiState.targetReps > 0) {
                    (uiState.completedReps.toFloat() / uiState.targetReps)
                        .coerceIn(0f, 1f)
                } else {
                    0f
                }
            },
            modifier = Modifier.fillMaxWidth().height(8.dp)
        )

        Spacer(Modifier.height(20.dp))
        Text(
            if (uiState.isCompleted) "Exercise completed!"
            else "Keep going!"
        )
        Spacer(Modifier.height(12.dp))

        Button(
            onClick = workoutViewModel::completeRep,
            enabled = !uiState.isCompleted,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Complete 1 Rep")
        }

        Spacer(Modifier.height(8.dp))
        Button(
            onClick = workoutViewModel::finishWorkout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Finish Workout")
        }

        Spacer(Modifier.height(8.dp))
        Button(
            onClick = workoutViewModel::resetWorkout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Reset Workout")
        }
    }
}
