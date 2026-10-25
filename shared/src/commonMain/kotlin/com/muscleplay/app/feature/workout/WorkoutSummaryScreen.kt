package com.muscleplay.app.feature.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val OrangePrimary = Color(0xFFFF5722)
private val OrangeSecondary = Color(0xFFFF9800)
private val DarkBackground = Color(0xFF0F0F17)

@Composable
fun WorkoutSummaryScreen(
    summary: WorkoutSessionSummary,
    onDone: () -> Unit,
    onRepeatWorkout: () -> Unit,
) {
    val minutes = summary.durationSeconds / 60
    val seconds = summary.durationSeconds % 60

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                color = OrangePrimary.copy(alpha = 0.2f),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OrangePrimary.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "WORKOUT SESSION SUMMARY",
                    style = MaterialTheme.typography.labelSmall,
                    color = OrangePrimary,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    letterSpacing = 1.sp
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = if (summary.totalExercises > 0 &&
                    summary.completedExercises == summary.totalExercises
                ) "Workout Completed! 🎉" else "Workout Finished",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Spacer(Modifier.height(4.dp))

            Text(
                "Great effort! Here's your performance breakdown.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )

            Spacer(Modifier.height(20.dp))

            GlassCardContainer {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    SummaryRow("Duration", "${minutes.toString().padStart(2, '0')}:" +
                        seconds.toString().padStart(2, '0'))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    SummaryRow("Total Repetitions", summary.totalRepetitions.toString())
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    SummaryRow(
                        "Exercises Completed",
                        "${summary.completedExercises} / ${summary.totalExercises}"
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    SummaryRow("Workout Completion", "${summary.completionPercentage}%")
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                "Exercise Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = OrangeSecondary,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            if (summary.exercises.isEmpty()) {
                Text(
                    "No exercise results were recorded.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(summary.exercises, key = { it.exerciseId }) { exercise ->
                        GlassCardContainer {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    exercise.exerciseName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                val targetText = exercise.targetReps?.let { " / $it" } ?: ""
                                Text("${exercise.completedReps}$targetText reps", color = OrangeSecondary, fontWeight = FontWeight.SemiBold)
                                Text(
                                    if (exercise.isCompleted) "Completed"
                                    else "Target not reached",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (exercise.isCompleted) Color(0xFF4CAF50) else Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = onRepeatWorkout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
            ) {
                Text("Repeat Workout", fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick = onDone,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OrangeSecondary.copy(alpha = 0.8f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangeSecondary)
            ) {
                Text("Done", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))
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
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Color.White)
    }
}

@Composable
private fun GlassCardContainer(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
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
                shape = RoundedCornerShape(22.dp)
            )
    ) {
        content()
    }
}
