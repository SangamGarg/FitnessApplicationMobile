package com.muscleplay.app.feature.workout

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muscleplay.app.feature.workout.components.LiveMetronomeBar
import com.muscleplay.app.feature.workout.model.ActiveWorkoutSession
import com.muscleplay.app.feature.workout.model.CoachPersona
import com.muscleplay.app.feature.workout.model.SetLog
import com.muscleplay.app.feature.workout.model.WorkoutSummaryData
import kotlinx.coroutines.delay

private val OrangePrimary = Color(0xFFFF5722)
private val OrangeSecondary = Color(0xFFFF9800)
private val DarkBackground = Color(0xFF0F0F17)

enum class PlayerTrackingMode {
    MANUAL,
    AI_TRACKER
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutPlayerScreen(
    session: ActiveWorkoutSession,
    initialTrackingMode: PlayerTrackingMode = PlayerTrackingMode.MANUAL,
    onFinishSession: (WorkoutSummaryData) -> Unit,
    onCancelSession: () -> Unit
) {
    var trackingMode by remember { mutableStateOf(initialTrackingMode) }
    var elapsedSeconds by remember { mutableLongStateOf(0L) }
    var isPaused by remember { mutableStateOf(false) }

    // Voice Coach & Metronome State
    var isVoiceMuted by remember { mutableStateOf(false) }
    var activePersona by remember { mutableStateOf(CoachPersona.MOTIVATIONAL) }

    // Rest Timer State
    var showRestTimerSheet by remember { mutableStateOf(false) }
    var restTimerSeconds by remember { mutableIntStateOf(60) }

    // AI Tracker Live State
    var aiLiveReps by remember { mutableIntStateOf(0) }
    var aiFeedbackMessage by remember { mutableStateOf("Position body inside the camera frame") }
    var aiConfidenceScore by remember { mutableIntStateOf(98) }

    // Live Workout Timer
    LaunchedEffect(isPaused) {
        while (!isPaused) {
            delay(1000L)
            elapsedSeconds++
        }
    }

    // Active Rest Countdown
    LaunchedEffect(showRestTimerSheet, restTimerSeconds) {
        if (showRestTimerSheet && restTimerSeconds > 0) {
            delay(1000L)
            restTimerSeconds--
            if (restTimerSeconds <= 0) {
                showRestTimerSheet = false
            }
        }
    }

    val currentExercise = session.exercises.getOrNull(session.currentExerciseIndex)
    val totalSets = currentExercise?.sets?.size ?: 0
    val completedSets = currentExercise?.sets?.count { it.isCompleted } ?: 0
    val progressFraction = if (totalSets > 0) completedSets.toFloat() / totalSets else 0f

    val formattedTime = remember(elapsedSeconds) {
        val mins = elapsedSeconds / 60
        val secs = elapsedSeconds % 60
        "${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Sticky Top Bar: Timer, Progress & Finish/Cancel Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cancel Action Button
                IconButton(
                    onClick = onCancelSession,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Cancel Workout", tint = Color.White)
                }

                // Workout Timer Badge
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
                        Icon(Icons.Default.Timer, contentDescription = null, tint = OrangeSecondary, modifier = Modifier.size(16.dp))
                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }

                // Finish Workout Button
                Button(
                    onClick = {
                        val totalVol = session.exercises.sumOf { ex ->
                            ex.sets.filter { it.isCompleted }.sumOf { it.actualWeightKg * it.actualReps }
                        }
                        val totalReps = session.exercises.sumOf { ex ->
                            ex.sets.filter { it.isCompleted }.sumOf { it.actualReps }
                        }
                        val summary = WorkoutSummaryData(
                            workoutTitle = session.routineTitle,
                            durationSeconds = elapsedSeconds,
                            totalVolumeKg = totalVol,
                            totalReps = totalReps,
                            completedSetsCount = completedSets,
                            totalSetsCount = totalSets,
                            exercises = session.exercises
                        )
                        onFinishSession(summary)
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    Text("Finish", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Scrollable Active Session Body
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 60.dp)
            ) {
                // Session Progress Bar
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Session Progress",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "$completedSets / $totalSets Sets Completed",
                                style = MaterialTheme.typography.labelSmall,
                                color = OrangeSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = OrangePrimary,
                            trackColor = Color.White.copy(alpha = 0.1f)
                        )
                    }
                }

                // Current Exercise Title Header
                if (currentExercise != null) {
                    item {
                        Column {
                            Text(
                                text = currentExercise.exercise.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "Target: ${currentExercise.exercise.defaultSets} Sets × ${currentExercise.exercise.defaultTargetReps} Reps",
                                style = MaterialTheme.typography.bodySmall,
                                color = OrangeSecondary
                            )
                        }
                    }
                }

                // AI Live Voice Coach & Haptic Pace Metronome Bar
                item {
                    LiveMetronomeBar(
                        isMuted = isVoiceMuted,
                        selectedPersona = activePersona,
                        onToggleMute = { isVoiceMuted = !isVoiceMuted },
                        onSelectPersona = { activePersona = it }
                    )
                }

                // Mode Toggle Switch
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            onClick = { trackingMode = PlayerTrackingMode.MANUAL },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            color = if (trackingMode == PlayerTrackingMode.MANUAL) OrangePrimary else Color.Transparent
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.EditNote, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    Text("Manual Set Logger", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                }
                            }
                        }

                        Surface(
                            onClick = { trackingMode = PlayerTrackingMode.AI_TRACKER },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            color = if (trackingMode == PlayerTrackingMode.AI_TRACKER) OrangePrimary else Color.Transparent
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    Text("AI Camera Tracker", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }

                // Main Content Items (Sets or AI Camera Viewfinder)
                if (trackingMode == PlayerTrackingMode.MANUAL) {
                    if (currentExercise != null) {
                        itemsIndexed(currentExercise.sets) { _, setLog ->
                            ManualSetRowCard(
                                setLog = setLog,
                                onSetCompletedChange = { isDone ->
                                    setLog.isCompleted = isDone
                                    if (isDone) {
                                        restTimerSeconds = 60
                                        showRestTimerSheet = true
                                    }
                                }
                            )
                        }
                    }
                } else {
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(Color.Black.copy(alpha = 0.9f))
                                    .border(1.5.dp, OrangePrimary.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val stroke = Stroke(
                                        width = 3f,
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                                    )
                                    drawRect(
                                        color = OrangePrimary.copy(alpha = 0.4f),
                                        style = stroke
                                    )
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(14.dp),
                                    modifier = Modifier.padding(20.dp)
                                ) {
                                    Surface(
                                        color = Color.Black.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(16.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4CAF50))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF4CAF50))
                                            )
                                            Text(
                                                text = "Pose Tracking: Active ($aiConfidenceScore% Confidence)",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF4CAF50),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Text(
                                        text = "$aiLiveReps",
                                        fontSize = 72.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "COMPLETED REPS",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = OrangeSecondary,
                                        letterSpacing = 2.sp
                                    )

                                    Surface(
                                        color = OrangePrimary.copy(alpha = 0.25f),
                                        shape = RoundedCornerShape(16.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, OrangePrimary)
                                    ) {
                                        Text(
                                            text = "💡 $aiFeedbackMessage",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    aiLiveReps++
                                    val feedbacks = listOf(
                                        "Good Depth!",
                                        "Keep chest up!",
                                        "Slow down on descent",
                                        "Perfect Form 🔥",
                                        "Drive through heels!"
                                    )
                                    aiFeedbackMessage = feedbacks.random()
                                    if (currentExercise != null) {
                                        val incompleteSet = currentExercise.sets.firstOrNull { !it.isCompleted }
                                        incompleteSet?.let {
                                            it.actualReps = aiLiveReps
                                            if (aiLiveReps >= it.targetReps) {
                                                it.isCompleted = true
                                                restTimerSeconds = 60
                                                showRestTimerSheet = true
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.AddCircle, contentDescription = null, tint = Color.White)
                                    Text("Simulate AI Rep (+1)", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Rest Timer Bottom Sheet Overlay
        if (showRestTimerSheet) {
            ModalBottomSheet(
                onDismissRequest = { showRestTimerSheet = false },
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                containerColor = Color(0xFF181824)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        text = "REST TIME ⏱️",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = OrangeSecondary,
                        letterSpacing = 1.5.sp
                    )

                    Text(
                        text = "${restTimerSeconds}s",
                        fontSize = 56.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    Surface(
                        color = Color.White.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "UP NEXT",
                                style = MaterialTheme.typography.labelSmall,
                                color = OrangeSecondary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Set ${completedSets + 1} • ${currentExercise?.exercise?.name}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { restTimerSeconds += 15 },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(18.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text("+15s Rest", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { showRestTimerSheet = false },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                        ) {
                            Text("Skip Rest", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ManualSetRowCard(
    setLog: SetLog,
    onSetCompletedChange: (Boolean) -> Unit
) {
    var isDone by remember { mutableStateOf(setLog.isCompleted) }
    var weightInput by remember { mutableStateOf(setLog.actualWeightKg.toString()) }
    var repsInput by remember { mutableStateOf(setLog.actualReps.toString()) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (isDone) Color(0xFF4CAF50).copy(alpha = 0.12f) else Color.White.copy(alpha = 0.06f)
            )
            .border(
                width = 1.dp,
                color = if (isDone) Color(0xFF4CAF50).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Set Number Badge
            Surface(
                color = if (isDone) Color(0xFF4CAF50).copy(alpha = 0.25f) else OrangePrimary.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "SET ${setLog.setNumber}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDone) Color(0xFF4CAF50) else OrangeSecondary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            // Weight Input
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("WEIGHT (KG)", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                OutlinedTextField(
                    value = weightInput,
                    onValueChange = {
                        weightInput = it
                        it.toDoubleOrNull()?.let { w -> setLog.actualWeightKg = w }
                    },
                    modifier = Modifier.width(80.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = OrangePrimary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                    )
                )
            }

            // Reps Input
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("REPS", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                OutlinedTextField(
                    value = repsInput,
                    onValueChange = {
                        repsInput = it
                        it.toIntOrNull()?.let { r -> setLog.actualReps = r }
                    },
                    modifier = Modifier.width(80.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = OrangePrimary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                    )
                )
            }

            // Complete Toggle Button
            IconButton(
                onClick = {
                    isDone = !isDone
                    onSetCompletedChange(isDone)
                },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isDone) Color(0xFF4CAF50) else Color.White.copy(alpha = 0.1f))
            ) {
                Icon(
                    imageVector = if (isDone) Icons.Default.Check else Icons.Default.Check,
                    contentDescription = "Complete Set",
                    tint = Color.White
                )
            }
        }
    }
}
