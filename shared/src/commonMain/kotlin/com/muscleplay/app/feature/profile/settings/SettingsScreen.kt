package com.muscleplay.app.feature.profile.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
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

private val OrangePrimary = Color(0xFFFF5722)
private val OrangeSecondary = Color(0xFFFF9800)
private val DarkBackground = Color(0xFF0F0F17)

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
) {
    var darkModeEnabled by remember { mutableStateOf(true) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var soundCuesEnabled by remember { mutableStateOf(true) }
    var reminderTime by remember { mutableStateOf("08:00 AM") }
    var selectedUnit by remember { mutableStateOf("Metric (kg/cm)") }
    var showUnitDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Settings & Preferences",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item {
                    Text(
                        text = "Preferences",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OrangeSecondary
                    )
                }

                item {
                    GlassCardContainer {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            SettingSwitchRow(
                                icon = Icons.Default.DarkMode,
                                title = "Dark Theme",
                                subtitle = "Use dark appearance across the app",
                                checked = darkModeEnabled,
                                onCheckedChange = { darkModeEnabled = it }
                            )
                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                            SettingSwitchRow(
                                icon = Icons.Default.Notifications,
                                title = "Push Notifications",
                                subtitle = "Workout reminders and challenge alerts",
                                checked = notificationsEnabled,
                                onCheckedChange = { notificationsEnabled = it }
                            )
                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                            SettingSwitchRow(
                                icon = Icons.AutoMirrored.Filled.VolumeUp,
                                title = "Voice Cues & Audio",
                                subtitle = "Spoken rep counts and form corrections",
                                checked = soundCuesEnabled,
                                onCheckedChange = { soundCuesEnabled = it }
                            )
                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                            SettingClickRow(
                                icon = Icons.Default.RecordVoiceOver,
                                title = "AI Voice Persona",
                                subtitle = "Motivational Coach 💪",
                                onClick = {}
                            )
                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                            SettingClickRow(
                                icon = Icons.Default.GraphicEq,
                                title = "Haptic Pace Metronome",
                                subtitle = "2s Down • 1s Hold • 1s Up",
                                onClick = {}
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Workout & Measurements",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OrangeSecondary
                    )
                }

                item {
                    GlassCardContainer {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            SettingClickRow(
                                icon = Icons.Default.Straighten,
                                title = "Measurement Units",
                                subtitle = selectedUnit,
                                onClick = { showUnitDialog = true }
                            )
                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                            SettingClickRow(
                                icon = Icons.Default.Timer,
                                title = "Daily Workout Reminder",
                                subtitle = reminderTime,
                                onClick = {
                                    reminderTime =
                                        if (reminderTime == "08:00 AM") "06:30 PM" else "08:00 AM"
                                }
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Account Danger Zone",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF5252)
                    )
                }

                item {
                    GlassCardContainer {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            SettingClickRow(
                                icon = Icons.Default.DeleteForever,
                                title = "Delete My Account",
                                subtitle = "Permanently remove your account and all fitness data",
                                tint = Color(0xFFFF5252),
                                onClick = { showDeleteAccountDialog = true }
                            )
                        }
                    }
                }
            }

            if (showUnitDialog) {
                AlertDialog(
                    onDismissRequest = { showUnitDialog = false },
                    title = { Text("Select Measurement Units") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = {
                                selectedUnit = "Metric (kg/cm)"; showUnitDialog = false
                            }) {
                                Text("Metric (kg/cm)", color = OrangePrimary)
                            }
                            TextButton(onClick = {
                                selectedUnit = "Imperial (lbs/in)"; showUnitDialog = false
                            }) {
                                Text("Imperial (lbs/in)", color = OrangePrimary)
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { showUnitDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                        ) {
                            Text("Close")
                        }
                    }
                )
            }

            if (showDeleteAccountDialog) {
                var deleteConfirmText by remember { mutableStateOf("") }
                val isDeleteConfirmed = deleteConfirmText.trim().equals("delete", ignoreCase = true)

                AlertDialog(
                    onDismissRequest = { showDeleteAccountDialog = false },
                    shape = RoundedCornerShape(28.dp),
                    containerColor = Color(0xFF181824),
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF5252).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteForever,
                                contentDescription = "Delete Account",
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    },
                    title = {
                        Text(
                            text = "Delete My Account ⚠️",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Are you sure you want to permanently delete your MusclePlay account? All your workout history, MP coins, and progress photos will be deleted forever.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.85f)
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Type \"delete\" below to confirm:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF5252)
                                )

                                OutlinedTextField(
                                    value = deleteConfirmText,
                                    onValueChange = { deleteConfirmText = it },
                                    placeholder = { Text("delete", color = Color.White.copy(alpha = 0.4f)) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color(0xFFFF5252),
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                                    )
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (isDeleteConfirmed) {
                                    showDeleteAccountDialog = false
                                    onBack()
                                }
                            },
                            enabled = isDeleteConfirmed,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF5252),
                                disabledContainerColor = Color.White.copy(alpha = 0.12f)
                            )
                        ) {
                            Text("Delete Account", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteAccountDialog = false }) {
                            Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun GlassCardContainer(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
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
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        content()
    }
}

@Composable
fun SettingSwitchRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = OrangePrimary)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = OrangePrimary,
                uncheckedThumbColor = Color.White.copy(alpha = 0.6f),
                uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
            )
        )
    }
}

@Composable
fun SettingClickRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    tint: Color = OrangePrimary,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = OrangeSecondary
                )
            }
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.5f)
        )
    }
}
