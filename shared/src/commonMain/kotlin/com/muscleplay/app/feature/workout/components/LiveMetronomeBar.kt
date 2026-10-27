package com.muscleplay.app.feature.workout.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muscleplay.app.feature.workout.model.*
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

private val OrangePrimary = Color(0xFFFF5722)
private val OrangeSecondary = Color(0xFFFF9800)

@Composable
fun LiveMetronomeBar(
    modifier: Modifier = Modifier,
    isMuted: Boolean = false,
    selectedPersona: CoachPersona = CoachPersona.MOTIVATIONAL,
    onToggleMute: () -> Unit = {},
    onSelectPersona: (CoachPersona) -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    var activePhaseIndex by remember { mutableIntStateOf(0) }
    var showPersonaDialog by remember { mutableStateOf(false) }

    val tempoPhases = remember { TempoPhase.entries.toTypedArray() }
    val currentPhase = tempoPhases[activePhaseIndex]

    // Metronome Tempo Cycle Loop (2s Lowering -> 1s Hold -> 1s Power Up)
    LaunchedEffect(Unit) {
        while (true) {
            delay((currentPhase.durationSec * 1000L).milliseconds)
            activePhaseIndex = (activePhaseIndex + 1) % tempoPhases.size
            // Trigger haptic pulse on tempo transition
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    Surface(
        color = Color.White.copy(alpha = 0.08f),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, OrangePrimary.copy(alpha = 0.35f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Voice Coach & Persona Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Voice Coach Persona Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(OrangePrimary.copy(alpha = 0.2f))
                        .border(1.dp, OrangePrimary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .clickable { showPersonaDialog = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(selectedPersona.iconEmoji, fontSize = 14.sp)
                    Text(selectedPersona.displayName, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Persona", tint = OrangeSecondary, modifier = Modifier.size(16.dp))
                }

                // Audio Mute Toggle Button
                IconButton(
                    onClick = onToggleMute,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (isMuted) Color.White.copy(alpha = 0.1f) else OrangeSecondary.copy(alpha = 0.25f))
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Mute Voice",
                        tint = if (isMuted) Color.White.copy(alpha = 0.5f) else OrangeSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Metronome Tempo Phase Indicator
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(OrangePrimary)
                        )
                        Text(
                            text = "PACE METRONOME: ${currentPhase.label.uppercase()}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = OrangeSecondary
                        )
                    }

                    Text(
                        text = currentPhase.instruction,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }

                // 3-Segment Progress Visualizer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tempoPhases.forEachIndexed { idx, phase ->
                        val isCurrent = idx == activePhaseIndex
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    if (isCurrent) OrangePrimary else Color.White.copy(alpha = 0.15f)
                                )
                        )
                    }
                }
            }
        }
    }

    // Persona Selector Dialog Modal
    if (showPersonaDialog) {
        AlertDialog(
            onDismissRequest = { showPersonaDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color(0xFF181824),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = OrangePrimary)
                    Text("Select AI Voice Persona 🗣️", fontWeight = FontWeight.ExtraBold, color = Color.White)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    CoachPersona.entries.forEach { persona ->
                        val isSelected = selectedPersona == persona
                        Surface(
                            onClick = {
                                onSelectPersona(persona)
                                showPersonaDialog = false
                            },
                            color = if (isSelected) OrangePrimary.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) OrangePrimary else Color.White.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(persona.iconEmoji, fontSize = 24.sp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(persona.displayName, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(persona.description, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = "Selected", tint = OrangeSecondary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPersonaDialog = false }) {
                    Text("Done", color = Color.White.copy(alpha = 0.7f))
                }
            }
        )
    }
}
