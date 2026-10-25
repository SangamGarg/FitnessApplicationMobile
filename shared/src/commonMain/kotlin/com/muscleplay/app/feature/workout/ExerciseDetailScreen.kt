package com.muscleplay.app.feature.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muscleplay.app.feature.workout.model.Exercise

private val OrangePrimary = Color(0xFFFF5722)
private val OrangeSecondary = Color(0xFFFF9800)
private val DarkBackground = Color(0xFF0F0F17)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExerciseDetailBottomSheet(
    exercise: Exercise,
    onDismissRequest: () -> Unit,
    onStartExerciseManual: (Exercise) -> Unit,
    onStartExerciseAi: (Exercise) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = Color(0xFF181824)
    ) {
        ExerciseDetailContent(
            exercise = exercise,
            onClose = onDismissRequest,
            onStartExerciseManual = { onStartExerciseManual(exercise) },
            onStartExerciseAi = { onStartExerciseAi(exercise) }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExerciseDetailContent(
    exercise: Exercise,
    onClose: () -> Unit = {},
    onStartExerciseManual: () -> Unit = {},
    onStartExerciseAi: () -> Unit = {}
) {
    var isPlayingDemo by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 520.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Large Media Display Area (Video Demonstration Placeholder)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.8f),
                                    OrangePrimary.copy(alpha = 0.25f)
                                )
                            )
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(22.dp))
                        .clickable { isPlayingDemo = !isPlayingDemo },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = OrangePrimary,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isPlayingDemo) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play Demonstration",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Text(
                            text = if (isPlayingDemo) "Demonstration Playing (Loop)" else "Tap to Preview Demo Video",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (exercise.supportsAiTracking) {
                        Surface(
                            color = OrangePrimary,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "✨ AI Camera Ready",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Tags Row (Primary & Secondary muscles, equipment, movement pattern)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Target Muscles & Equipment",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = OrangeSecondary
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Primary Muscle Tag
                        TagChip(label = "Primary: ${exercise.primaryMuscleGroup}", isPrimary = true)

                        // Secondary Muscles Tags
                        exercise.secondaryMuscleGroups.forEach { sec ->
                            TagChip(label = sec, isPrimary = false)
                        }

                        // Equipment Tag
                        TagChip(label = "Eq: ${exercise.equipment.label}", isPrimary = false)

                        // Movement Pattern Tag
                        TagChip(label = "Pattern: ${exercise.movementPattern}", isPrimary = false)
                    }
                }
            }

            // Description
            item {
                Text(
                    text = exercise.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }

            // Step-by-Step Instructions List
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Step-by-Step Instructions",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = OrangeSecondary
                    )

                    exercise.steps.forEachIndexed { index, step ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .padding(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = OrangePrimary.copy(alpha = 0.25f),
                                modifier = Modifier.size(26.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = OrangeSecondary
                                    )
                                }
                            }

                            Text(
                                text = step,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Action Bar at Bottom
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Manual Start Button
                Button(
                    onClick = onStartExerciseManual,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    Text("Start Session", fontWeight = FontWeight.Bold, color = Color.White)
                }

                // AI Camera Mode Button if supported
                if (exercise.supportsAiTracking) {
                    OutlinedButton(
                        onClick = onStartExerciseAi,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, OrangeSecondary),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangeSecondary)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text("AI Camera", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TagChip(label: String, isPrimary: Boolean) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isPrimary) OrangePrimary.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isPrimary) OrangePrimary.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.15f)
        )
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (isPrimary) OrangeSecondary else Color.White.copy(alpha = 0.8f),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}
