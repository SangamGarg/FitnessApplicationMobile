package com.muscleplay.app.feature.workout.components

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muscleplay.app.feature.workout.model.*

private val OrangePrimary = Color(0xFFFF5722)
private val OrangeSecondary = Color(0xFFFF9800)

@Composable
fun MuscleHeatmapVisualizer(
    modifier: Modifier = Modifier,
    statuses: Map<MuscleGroup, MuscleStatus> = defaultMuscleStatuses,
    selectedMuscle: MuscleGroup? = null,
    onMuscleSelected: (MuscleGroup?) -> Unit = {}
) {
    var isFrontView by remember { mutableStateOf(true) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
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
                shape = RoundedCornerShape(24.dp)
            )
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Title & Front/Back View Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(OrangePrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AccessibilityNew, contentDescription = null, tint = OrangeSecondary, modifier = Modifier.size(20.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("3D Muscle Heatmap", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Color.White, maxLines = 1)
                        Text("Tap any muscle to filter exercises", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f), maxLines = 1)
                    }
                }

                // Front / Back View Toggle
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .padding(2.dp)
                ) {
                    Surface(
                        onClick = { isFrontView = true },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isFrontView) OrangePrimary else Color.Transparent
                    ) {
                        Text(
                            text = "Front",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    Surface(
                        onClick = { isFrontView = false },
                        shape = RoundedCornerShape(10.dp),
                        color = if (!isFrontView) OrangePrimary else Color.Transparent
                    ) {
                        Text(
                            text = "Back",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Legend Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendChip("Recovered", Color(0xFF4CAF50))
                LegendChip("Moderate", Color(0xFFFFD54F))
                LegendChip("High Strain 🔥", Color(0xFFFF5722))
            }

            // Anatomical Silhouette Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentAlignment = Alignment.Center
            ) {
                AnatomicalBodyCanvas(
                    isFrontView = isFrontView,
                    statuses = statuses,
                    selectedMuscle = selectedMuscle,
                    onMuscleTapped = { muscle ->
                        onMuscleSelected(if (selectedMuscle == muscle) null else muscle)
                    }
                )
            }

            // Detail Card for Selected Muscle
            selectedMuscle?.let { muscle ->
                val status = statuses[muscle] ?: MuscleStatus(muscle, FatigueLevel.RECOVERED, "Not trained recently")

                Surface(
                    color = Color.White.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OrangePrimary.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(muscle.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                Surface(
                                    color = status.fatigueLevel.color.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        status.fatigueLevel.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = status.fatigueLevel.color,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text("Last activity: ${status.lastTrainedText}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
                        }

                        IconButton(onClick = { onMuscleSelected(null) }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear Selection", tint = Color.White.copy(alpha = 0.6f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendChip(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
    }
}

@Composable
private fun AnatomicalBodyCanvas(
    isFrontView: Boolean,
    statuses: Map<MuscleGroup, MuscleStatus>,
    selectedMuscle: MuscleGroup?,
    onMuscleTapped: (MuscleGroup) -> Unit
) {
    Canvas(
        modifier = Modifier
            .fillMaxHeight()
            .aspectRatio(0.6f)
            .pointerInput(isFrontView) {
                detectTapGestures { offset ->
                    val width = size.width
                    val height = size.height

                    val tapped = if (isFrontView) {
                        when {
                            // Head / Neck area (Top 15%)
                            offset.y < height * 0.15f -> null
                            // Shoulders (15% - 28% height, Left/Right outer edges)
                            offset.y in (height * 0.15f)..(height * 0.28f) && (offset.x < width * 0.32f || offset.x > width * 0.68f) -> MuscleGroup.SHOULDERS
                            // Chest (18% - 35% height, Center body)
                            offset.y in (height * 0.18f)..(height * 0.35f) && offset.x in (width * 0.32f)..(width * 0.68f) -> MuscleGroup.CHEST
                            // Biceps (28% - 45% height, Left/Right outer arms)
                            offset.y in (height * 0.28f)..(height * 0.45f) && (offset.x < width * 0.30f || offset.x > width * 0.70f) -> MuscleGroup.BICEPS
                            // Abs / Core (35% - 55% height, Center body)
                            offset.y in (height * 0.35f)..(height * 0.55f) && offset.x in (width * 0.32f)..(width * 0.68f) -> MuscleGroup.ABS
                            // Quads (55% - 80% height, Legs)
                            offset.y in (height * 0.55f)..(height * 0.80f) -> MuscleGroup.QUADS
                            else -> null
                        }
                    } else {
                        when {
                            // Back (18% - 48% height, Center back)
                            offset.y in (height * 0.18f)..(height * 0.48f) && offset.x in (width * 0.30f)..(width * 0.70f) -> MuscleGroup.BACK
                            // Triceps (25% - 45% height, Outer arms back)
                            offset.y in (height * 0.25f)..(height * 0.45f) && (offset.x < width * 0.30f || offset.x > width * 0.70f) -> MuscleGroup.TRICEPS
                            // Glutes (48% - 62% height, Center hips)
                            offset.y in (height * 0.48f)..(height * 0.62f) -> MuscleGroup.GLUTES
                            // Hamstrings (62% - 80% height, Upper legs back)
                            offset.y in (height * 0.62f)..(height * 0.80f) -> MuscleGroup.HAMSTRINGS
                            // Calves (80% - 98% height, Lower legs back)
                            offset.y in (height * 0.80f)..(height * 0.98f) -> MuscleGroup.CALVES
                            else -> null
                        }
                    }

                    tapped?.let { onMuscleTapped(it) }
                }
            }
    ) {
        val w = size.width
        val h = size.height

        // 1. Draw Background Body Silhouette
        val bodyColor = Color.White.copy(alpha = 0.08f)
        val strokeColor = Color.White.copy(alpha = 0.20f)

        // Head
        drawCircle(
            color = bodyColor,
            radius = w * 0.12f,
            center = Offset(w * 0.5f, h * 0.08f)
        )
        drawCircle(
            color = strokeColor,
            radius = w * 0.12f,
            center = Offset(w * 0.5f, h * 0.08f),
            style = Stroke(width = 2f)
        )

        if (isFrontView) {
            // FRONT VIEW MUSCLE GROUPS

            // Chest Path
            val chestColor = getMuscleColor(MuscleGroup.CHEST, statuses)
            val isChestSelected = selectedMuscle == MuscleGroup.CHEST
            drawRoundRect(
                color = chestColor,
                topLeft = Offset(w * 0.32f, h * 0.18f),
                size = Size(w * 0.36f, h * 0.15f),
                cornerRadius = CornerRadius(16f, 16f)
            )
            if (isChestSelected) {
                drawRoundRect(
                    color = OrangeSecondary,
                    topLeft = Offset(w * 0.32f, h * 0.18f),
                    size = Size(w * 0.36f, h * 0.15f),
                    cornerRadius = CornerRadius(16f, 16f),
                    style = Stroke(width = 4f)
                )
            }

            // Shoulders (Left & Right)
            val shoulderColor = getMuscleColor(MuscleGroup.SHOULDERS, statuses)
            val isShoulderSelected = selectedMuscle == MuscleGroup.SHOULDERS
            drawCircle(color = shoulderColor, radius = w * 0.10f, center = Offset(w * 0.22f, h * 0.21f))
            drawCircle(color = shoulderColor, radius = w * 0.10f, center = Offset(w * 0.78f, h * 0.21f))
            if (isShoulderSelected) {
                drawCircle(color = OrangeSecondary, radius = w * 0.10f, center = Offset(w * 0.22f, h * 0.21f), style = Stroke(width = 4f))
                drawCircle(color = OrangeSecondary, radius = w * 0.10f, center = Offset(w * 0.78f, h * 0.21f), style = Stroke(width = 4f))
            }

            // Biceps (Left & Right)
            val bicepColor = getMuscleColor(MuscleGroup.BICEPS, statuses)
            val isBicepSelected = selectedMuscle == MuscleGroup.BICEPS
            drawRoundRect(color = bicepColor, topLeft = Offset(w * 0.14f, h * 0.29f), size = Size(w * 0.12f, h * 0.16f), cornerRadius = CornerRadius(12f, 12f))
            drawRoundRect(color = bicepColor, topLeft = Offset(w * 0.74f, h * 0.29f), size = Size(w * 0.12f, h * 0.16f), cornerRadius = CornerRadius(12f, 12f))
            if (isBicepSelected) {
                drawRoundRect(color = OrangeSecondary, topLeft = Offset(w * 0.14f, h * 0.29f), size = Size(w * 0.12f, h * 0.16f), cornerRadius = CornerRadius(12f, 12f), style = Stroke(width = 4f))
                drawRoundRect(color = OrangeSecondary, topLeft = Offset(w * 0.74f, h * 0.29f), size = Size(w * 0.12f, h * 0.16f), cornerRadius = CornerRadius(12f, 12f), style = Stroke(width = 4f))
            }

            // Abs / Core
            val absColor = getMuscleColor(MuscleGroup.ABS, statuses)
            val isAbsSelected = selectedMuscle == MuscleGroup.ABS
            drawRoundRect(color = absColor, topLeft = Offset(w * 0.35f, h * 0.35f), size = Size(w * 0.30f, h * 0.18f), cornerRadius = CornerRadius(12f, 12f))
            if (isAbsSelected) {
                drawRoundRect(color = OrangeSecondary, topLeft = Offset(w * 0.35f, h * 0.35f), size = Size(w * 0.30f, h * 0.18f), cornerRadius = CornerRadius(12f, 12f), style = Stroke(width = 4f))
            }

            // Quads (Left & Right)
            val quadColor = getMuscleColor(MuscleGroup.QUADS, statuses)
            val isQuadSelected = selectedMuscle == MuscleGroup.QUADS
            drawRoundRect(color = quadColor, topLeft = Offset(w * 0.28f, h * 0.55f), size = Size(w * 0.20f, h * 0.25f), cornerRadius = CornerRadius(16f, 16f))
            drawRoundRect(color = quadColor, topLeft = Offset(w * 0.52f, h * 0.55f), size = Size(w * 0.20f, h * 0.25f), cornerRadius = CornerRadius(16f, 16f))
            if (isQuadSelected) {
                drawRoundRect(color = OrangeSecondary, topLeft = Offset(w * 0.28f, h * 0.55f), size = Size(w * 0.20f, h * 0.25f), cornerRadius = CornerRadius(16f, 16f), style = Stroke(width = 4f))
                drawRoundRect(color = OrangeSecondary, topLeft = Offset(w * 0.52f, h * 0.55f), size = Size(w * 0.20f, h * 0.25f), cornerRadius = CornerRadius(16f, 16f), style = Stroke(width = 4f))
            }

        } else {
            // BACK VIEW MUSCLE GROUPS

            // Upper & Lower Back
            val backColor = getMuscleColor(MuscleGroup.BACK, statuses)
            val isBackSelected = selectedMuscle == MuscleGroup.BACK
            val backPath = Path().apply {
                moveTo(w * 0.25f, h * 0.18f)
                lineTo(w * 0.75f, h * 0.18f)
                lineTo(w * 0.65f, h * 0.48f)
                lineTo(w * 0.35f, h * 0.48f)
                close()
            }
            drawPath(path = backPath, color = backColor)
            if (isBackSelected) {
                drawPath(path = backPath, color = OrangeSecondary, style = Stroke(width = 4f))
            }

            // Triceps (Left & Right)
            val tricepColor = getMuscleColor(MuscleGroup.TRICEPS, statuses)
            val isTricepSelected = selectedMuscle == MuscleGroup.TRICEPS
            drawRoundRect(color = tricepColor, topLeft = Offset(w * 0.14f, h * 0.24f), size = Size(w * 0.12f, h * 0.18f), cornerRadius = CornerRadius(12f, 12f))
            drawRoundRect(color = tricepColor, topLeft = Offset(w * 0.74f, h * 0.24f), size = Size(w * 0.12f, h * 0.18f), cornerRadius = CornerRadius(12f, 12f))
            if (isTricepSelected) {
                drawRoundRect(color = OrangeSecondary, topLeft = Offset(w * 0.14f, h * 0.24f), size = Size(w * 0.12f, h * 0.18f), cornerRadius = CornerRadius(12f, 12f), style = Stroke(width = 4f))
                drawRoundRect(color = OrangeSecondary, topLeft = Offset(w * 0.74f, h * 0.24f), size = Size(w * 0.12f, h * 0.18f), cornerRadius = CornerRadius(12f, 12f), style = Stroke(width = 4f))
            }

            // Glutes
            val gluteColor = getMuscleColor(MuscleGroup.GLUTES, statuses)
            val isGluteSelected = selectedMuscle == MuscleGroup.GLUTES
            drawRoundRect(color = gluteColor, topLeft = Offset(w * 0.30f, h * 0.49f), size = Size(w * 0.40f, h * 0.13f), cornerRadius = CornerRadius(14f, 14f))
            if (isGluteSelected) {
                drawRoundRect(color = OrangeSecondary, topLeft = Offset(w * 0.30f, h * 0.49f), size = Size(w * 0.40f, h * 0.13f), cornerRadius = CornerRadius(14f, 14f), style = Stroke(width = 4f))
            }

            // Hamstrings
            val hamColor = getMuscleColor(MuscleGroup.HAMSTRINGS, statuses)
            val isHamSelected = selectedMuscle == MuscleGroup.HAMSTRINGS
            drawRoundRect(color = hamColor, topLeft = Offset(w * 0.28f, h * 0.63f), size = Size(w * 0.20f, h * 0.18f), cornerRadius = CornerRadius(14f, 14f))
            drawRoundRect(color = hamColor, topLeft = Offset(w * 0.52f, h * 0.63f), size = Size(w * 0.20f, h * 0.18f), cornerRadius = CornerRadius(14f, 14f))
            if (isHamSelected) {
                drawRoundRect(color = OrangeSecondary, topLeft = Offset(w * 0.28f, h * 0.63f), size = Size(w * 0.20f, h * 0.18f), cornerRadius = CornerRadius(14f, 14f), style = Stroke(width = 4f))
                drawRoundRect(color = OrangeSecondary, topLeft = Offset(w * 0.52f, h * 0.63f), size = Size(w * 0.20f, h * 0.18f), cornerRadius = CornerRadius(14f, 14f), style = Stroke(width = 4f))
            }

            // Calves
            val calfColor = getMuscleColor(MuscleGroup.CALVES, statuses)
            val isCalfSelected = selectedMuscle == MuscleGroup.CALVES
            drawRoundRect(color = calfColor, topLeft = Offset(w * 0.30f, h * 0.82f), size = Size(w * 0.18f, h * 0.15f), cornerRadius = CornerRadius(12f, 12f))
            drawRoundRect(color = calfColor, topLeft = Offset(w * 0.52f, h * 0.82f), size = Size(w * 0.18f, h * 0.15f), cornerRadius = CornerRadius(12f, 12f))
            if (isCalfSelected) {
                drawRoundRect(color = OrangeSecondary, topLeft = Offset(w * 0.30f, h * 0.82f), size = Size(w * 0.18f, h * 0.15f), cornerRadius = CornerRadius(12f, 12f), style = Stroke(width = 4f))
                drawRoundRect(color = OrangeSecondary, topLeft = Offset(w * 0.52f, h * 0.82f), size = Size(w * 0.18f, h * 0.15f), cornerRadius = CornerRadius(12f, 12f), style = Stroke(width = 4f))
            }
        }
    }
}

private fun getMuscleColor(muscle: MuscleGroup, statuses: Map<MuscleGroup, MuscleStatus>): Color {
    val status = statuses[muscle] ?: return Color(0xFF4CAF50).copy(alpha = 0.5f)
    return status.fatigueLevel.color.copy(alpha = 0.80f)
}
