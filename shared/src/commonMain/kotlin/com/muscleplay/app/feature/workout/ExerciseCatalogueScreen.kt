package com.muscleplay.app.feature.workout

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.muscleplay.app.feature.workout.components.MuscleHeatmapVisualizer
import com.muscleplay.app.feature.workout.model.*

private val OrangePrimary = Color(0xFFFF5722)
private val OrangeSecondary = Color(0xFFFF9800)
private val DarkBackground = Color(0xFF0F0F17)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseCatalogueScreen(
    onSelectExercise: (Exercise) -> Unit,
    onBack: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedDifficulty by remember { mutableStateOf(ExerciseDifficulty.ALL) }
    var selectedEquipment by remember { mutableStateOf(ExerciseEquipment.ALL) }
    var selectedMuscleFilter by remember { mutableStateOf<MuscleGroup?>(null) }
    var showAiOnly by remember { mutableStateOf(false) }
    var isHeatmapExpanded by remember { mutableStateOf(false) }

    val isAnyFilterActive = remember(searchQuery, selectedDifficulty, selectedEquipment, selectedMuscleFilter, showAiOnly) {
        searchQuery.isNotEmpty() ||
            selectedDifficulty != ExerciseDifficulty.ALL ||
            selectedEquipment != ExerciseEquipment.ALL ||
            selectedMuscleFilter != null ||
            showAiOnly
    }

    val filteredExercises = remember(searchQuery, selectedDifficulty, selectedEquipment, selectedMuscleFilter, showAiOnly) {
        mockExercisesList.filter { exercise ->
            val matchesQuery = searchQuery.isBlank() ||
                exercise.name.contains(searchQuery, ignoreCase = true) ||
                exercise.primaryMuscleGroup.contains(searchQuery, ignoreCase = true)

            val matchesDifficulty = selectedDifficulty == ExerciseDifficulty.ALL ||
                exercise.difficulty == selectedDifficulty

            val matchesEquipment = selectedEquipment == ExerciseEquipment.ALL ||
                exercise.equipment == selectedEquipment

            val matchesMuscle = selectedMuscleFilter == null ||
                exercise.primaryMuscleGroup.contains(selectedMuscleFilter!!.displayName, ignoreCase = true)

            val matchesAi = !showAiOnly || exercise.supportsAiTracking

            matchesQuery && matchesDifficulty && matchesEquipment && matchesMuscle && matchesAi
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 20.dp, bottom = 90.dp)
        ) {
            // 1. Styled Header Section
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = OrangePrimary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, OrangePrimary.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "AI WORKOUT CATALOG",
                                style = MaterialTheme.typography.labelSmall,
                                color = OrangePrimary,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                letterSpacing = 1.sp
                            )
                        }

                        // 3D Heatmap Visualizer Toggle Button
                        Button(
                            onClick = { isHeatmapExpanded = !isHeatmapExpanded },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isHeatmapExpanded) OrangeSecondary else OrangePrimary.copy(alpha = 0.25f)
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.AccessibilityNew, contentDescription = "3D Heatmap", tint = Color.White, modifier = Modifier.size(16.dp))
                                Text(if (isHeatmapExpanded) "Hide Heatmap" else "3D Heatmap", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Train & Elevate 🏋️‍♂️",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    Text(
                        text = "Discover exercises, master form, and train with AI camera pose tracking.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            // 2. Expandable 3D Muscle Heatmap Visualizer
            if (isHeatmapExpanded) {
                item {
                    MuscleHeatmapVisualizer(
                        selectedMuscle = selectedMuscleFilter,
                        onMuscleSelected = { muscle ->
                            selectedMuscleFilter = muscle
                        }
                    )
                }
            }

            // 3. Compact Search Bar with Clear Button
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    placeholder = {
                        Text("Search exercise or muscle group...", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp)
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = OrangeSecondary, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.08f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                        focusedBorderColor = OrangePrimary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }

            // 4. Filter Chips Section: Difficulty, Equipment & Muscle Filter
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Clear All Filters Button
                    if (isAnyFilterActive) {
                        item {
                            Surface(
                                onClick = {
                                    searchQuery = ""
                                    selectedDifficulty = ExerciseDifficulty.ALL
                                    selectedEquipment = ExerciseEquipment.ALL
                                    selectedMuscleFilter = null
                                    showAiOnly = false
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFF5252).copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FilterAltOff,
                                        contentDescription = "Clear Filters",
                                        tint = Color(0xFFFF5252),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Clear Filters",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFF5252)
                                    )
                                }
                            }
                        }
                    }

                    // Active Selected Muscle Filter Chip
                    selectedMuscleFilter?.let { muscle ->
                        item {
                            FilterChip(
                                selected = true,
                                onClick = { selectedMuscleFilter = null },
                                label = { Text("Muscle: ${muscle.displayName} ✕", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = OrangeSecondary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    // AI Filter Toggle Chip
                    item {
                        FilterChip(
                            selected = showAiOnly,
                            onClick = { showAiOnly = !showAiOnly },
                            label = { Text("✨ AI Supported", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OrangePrimary,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White.copy(alpha = 0.08f),
                                labelColor = Color.White.copy(alpha = 0.8f)
                            )
                        )
                    }

                    // Difficulty Chips
                    items(ExerciseDifficulty.entries) { diff ->
                        FilterChip(
                            selected = diff == selectedDifficulty,
                            onClick = { selectedDifficulty = diff },
                            label = { Text("Level: ${diff.label}", fontWeight = FontWeight.Medium, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OrangeSecondary,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White.copy(alpha = 0.08f),
                                labelColor = Color.White.copy(alpha = 0.8f)
                            )
                        )
                    }

                    // Equipment Chips
                    items(ExerciseEquipment.entries) { equip ->
                        FilterChip(
                            selected = equip == selectedEquipment,
                            onClick = { selectedEquipment = equip },
                            label = { Text("Eq: ${equip.label}", fontWeight = FontWeight.Medium, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OrangeSecondary.copy(alpha = 0.8f),
                                selectedLabelColor = Color.White,
                                containerColor = Color.White.copy(alpha = 0.08f),
                                labelColor = Color.White.copy(alpha = 0.8f)
                            )
                        )
                    }
                }
            }

            // 5. Exercise Cards List Items
            if (filteredExercises.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.3f),
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No exercises found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try adjusting your search or clear all filters.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    searchQuery = ""
                                    selectedDifficulty = ExerciseDifficulty.ALL
                                    selectedEquipment = ExerciseEquipment.ALL
                                    selectedMuscleFilter = null
                                    showAiOnly = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Reset All Filters", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(filteredExercises, key = { it.id }) { exercise ->
                    ExerciseCardItem(
                        exercise = exercise,
                        onClick = { onSelectExercise(exercise) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ExerciseCardItem(
    exercise: Exercise,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
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
                        Color.White.copy(alpha = 0.25f),
                        OrangePrimary.copy(alpha = 0.20f)
                    )
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                OrangePrimary.copy(alpha = 0.3f),
                                OrangeSecondary.copy(alpha = 0.15f)
                            )
                        )
                    )
                    .border(1.dp, OrangePrimary.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (exercise.equipment) {
                        ExerciseEquipment.DUMBBELL -> Icons.Default.FitnessCenter
                        ExerciseEquipment.BARBELL -> Icons.Default.FitnessCenter
                        ExerciseEquipment.BODYWEIGHT -> Icons.Default.AccessibilityNew
                        else -> Icons.Default.SportsGymnastics
                    },
                    contentDescription = exercise.name,
                    tint = OrangePrimary,
                    modifier = Modifier.size(25.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = exercise.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = exercise.primaryMuscleGroup,
                            style = MaterialTheme.typography.labelSmall,
                            color = OrangeSecondary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        color = Color.White.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = exercise.difficulty.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f),
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                if (exercise.supportsAiTracking) {
                    Surface(
                        color = OrangePrimary.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, OrangePrimary.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = OrangeSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "AI Camera Supported",
                                style = MaterialTheme.typography.labelSmall,
                                color = OrangeSecondary,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            Button(
                onClick = onClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Start", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                    Icon(Icons.Default.PlayArrow, contentDescription = "Start Active Session", tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}
