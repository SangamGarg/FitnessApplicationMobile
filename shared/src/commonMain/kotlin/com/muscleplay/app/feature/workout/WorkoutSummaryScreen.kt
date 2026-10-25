package com.muscleplay.app.feature.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun WorkoutSummaryScreen(
    summary: WorkoutSessionSummary,
    onDone: () -> Unit,
    onRepeatWorkout: () -> Unit,
) {
    val minutes = summary.durationSeconds / 60
    val seconds = summary.durationSeconds % 60

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (summary.totalExercises > 0 &&
                summary.completedExercises == summary.totalExercises
            ) "Workout Completed!" else "Workout Finished",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Great effort! Here's your session summary.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SummaryRow("Duration", "${minutes.toString().padStart(2, '0')}:" +
                    seconds.toString().padStart(2, '0'))
                SummaryRow("Total repetitions", summary.totalRepetitions.toString())
                SummaryRow(
                    "Exercises completed",
                    "${summary.completedExercises} / ${summary.totalExercises}"
                )
                SummaryRow("Workout completion", "${summary.completionPercentage}%")
            }
        }

        Spacer(Modifier.height(16.dp))
        Text(
            "Exercise breakdown",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        if (summary.exercises.isEmpty()) {
            Text(
                "No exercise results were recorded.",
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f, fill = false).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(summary.exercises, key = { it.exerciseId }) { exercise ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                exercise.exerciseName,
                                style = MaterialTheme.typography.titleSmall
                            )
                            val targetText = exercise.targetReps?.let { " / $it" } ?: ""
                            Text("${exercise.completedReps}$targetText reps")
                            Text(
                                if (exercise.isCompleted) "Completed"
                                else "Target not reached",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onRepeatWorkout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Repeat Workout")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Done")
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.titleMedium)
    }
}
