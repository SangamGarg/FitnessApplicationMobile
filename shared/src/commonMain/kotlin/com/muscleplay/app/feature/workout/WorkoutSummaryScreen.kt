package com.muscleplay.app.feature.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Timer
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
import com.muscleplay.app.feature.workout.model.WorkoutSummaryData

private val OrangePrimary = Color(0xFFFF5722)
private val OrangeSecondary = Color(0xFFFF9800)
private val DarkBackground = Color(0xFF0F0F17)

@Composable
fun WorkoutSummaryScreen(
    summary: WorkoutSummaryData,
    onDone: () -> Unit
) {
    val minutes = summary.durationSeconds / 60
    val seconds = summary.durationSeconds % 60
    val durationText = "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 80.dp)
        ) {
            // Header Badge
            item {
                Surface(
                    color = OrangePrimary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OrangePrimary.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = OrangeSecondary, modifier = Modifier.size(16.dp))
                        Text(
                            text = "WORKOUT COMPLETED 🎉",
                            style = MaterialTheme.typography.labelSmall,
                            color = OrangeSecondary,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp
                        )
                    }
                }
            }

            // Title & Subtitle
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Great Job, MusclePlay Athlete!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Here is your overall session performance summary.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            // Stat Cards Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricHighlightCard(
                            title = "DURATION",
                            value = durationText,
                            icon = Icons.Default.Timer,
                            modifier = Modifier.weight(1f)
                        )
                        MetricHighlightCard(
                            title = "VOLUME",
                            value = "${summary.totalVolumeKg.toInt()} kg",
                            icon = Icons.Default.FitnessCenter,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricHighlightCard(
                            title = "TOTAL REPS",
                            value = "${summary.totalReps}",
                            icon = Icons.Default.FitnessCenter,
                            modifier = Modifier.weight(1f)
                        )
                        MetricHighlightCard(
                            title = "SETS DONE",
                            value = "${summary.completedSetsCount} / ${summary.totalSetsCount}",
                            icon = Icons.Default.CheckCircle,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Targeted Muscle Recovery Impact Card
            item {
                GlassSummaryCardContainer {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Targeted Muscle Strain",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Chest & Triceps updated to High Strain 🔥",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                        Surface(
                            color = OrangePrimary.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "Heatmap Updated",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = OrangeSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Exercise Breakdown Section Header
            item {
                Text(
                    text = "Exercise & Set Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OrangeSecondary,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Exercise & Sets Items
            items(summary.exercises, key = { it.exercise.id }) { sessionEx ->
                GlassSummaryCardContainer {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = sessionEx.exercise.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        sessionEx.sets.forEach { setLog ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Set ${setLog.setNumber}: ${setLog.actualWeightKg} kg × ${setLog.actualReps} reps",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )

                                Surface(
                                    color = if (setLog.isCompleted) Color(0xFF4CAF50).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = if (setLog.isCompleted) "Completed ✓" else "Skipped",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (setLog.isCompleted) Color(0xFF4CAF50) else Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Save & Return Home Button (Inside scroll list for guaranteed visibility)
            item {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onDone,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    Text("Save & Return to Home 🏠", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun MetricHighlightCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    GlassSummaryCardContainer(modifier = modifier) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(icon, contentDescription = null, tint = OrangeSecondary, modifier = Modifier.size(16.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun GlassSummaryCardContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.10f),
                        Color.White.copy(alpha = 0.03f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.30f),
                        OrangePrimary.copy(alpha = 0.20f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        content()
    }
}
