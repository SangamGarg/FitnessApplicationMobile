package com.muscleplay.app.feature.profile.support

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

private val OrangePrimary = Color(0xFFFF5722)
private val OrangeSecondary = Color(0xFFFF9800)
private val DarkBackground = Color(0xFF0F0F17)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailSupportScreen(
    onBack: () -> Unit,
    viewModel: EmailSupportViewModel = viewModel { EmailSupportViewModel() }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("Email Support", fontWeight = FontWeight.Bold, color = Color.White)
                            Text(
                                "Sent to ${uiState.userEmail}",
                                style = MaterialTheme.typography.labelSmall,
                                color = OrangeSecondary
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .imePadding()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                // Header Note
                item {
                    GlassCardContainer {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(OrangePrimary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Mail, contentDescription = null, tint = OrangeSecondary)
                            }
                            Column {
                                Text("Dedicated Ticket Desk 📩", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Replies within ~2 business hours. Attach logs or screenshots for faster resolution.", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                            }
                        }
                    }
                }

                // Field 1: Category Dropdown
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Issue Category", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White)

                        ExposedDropdownMenuBox(
                            expanded = isCategoryDropdownExpanded,
                            onExpandedChange = { isCategoryDropdownExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = uiState.selectedCategory,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
                                shape = RoundedCornerShape(16.dp),
                                colors = inputColors()
                            )

                            ExposedDropdownMenu(
                                expanded = isCategoryDropdownExpanded,
                                onDismissRequest = { isCategoryDropdownExpanded = false },
                                modifier = Modifier.background(Color(0xFF181824))
                            ) {
                                supportIssueCategories.forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text(category, color = Color.White) },
                                        onClick = {
                                            viewModel.selectCategory(category)
                                            isCategoryDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Field 2: Subject Input
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Subject", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White)

                        OutlinedTextField(
                            value = uiState.subject,
                            onValueChange = { viewModel.updateSubject(it) },
                            placeholder = { Text("Short summary of your issue...", color = Color.White.copy(alpha = 0.4f)) },
                            trailingIcon = {
                                if (uiState.subject.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.updateSubject("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.White.copy(alpha = 0.5f))
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            colors = inputColors()
                        )
                    }
                }

                // Field 3: Message Body Area
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Detailed Message", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("${uiState.messageBody.length} / 1000", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                        }

                        OutlinedTextField(
                            value = uiState.messageBody,
                            onValueChange = { if (it.length <= 1000) viewModel.updateMessageBody(it) },
                            placeholder = { Text("Describe what happened, steps to reproduce, or questions...", color = Color.White.copy(alpha = 0.4f)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = inputColors()
                        )
                    }
                }

                // Field 4: Attachments Section
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Attachments", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White)

                            Button(
                                onClick = {
                                    viewModel.addAttachment("screenshot_${uiState.attachedFiles.size + 1}.png")
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary.copy(alpha = 0.25f))
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.AttachFile, contentDescription = "Attach", tint = OrangeSecondary, modifier = Modifier.size(18.dp))
                                    Text("Add Image", fontWeight = FontWeight.Bold, color = OrangeSecondary, fontSize = 12.sp)
                                }
                            }
                        }

                        if (uiState.attachedFiles.isNotEmpty()) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                itemsIndexed(uiState.attachedFiles) { index, fileUri ->
                                    Surface(
                                        color = Color.White.copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(12.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, OrangePrimary.copy(alpha = 0.4f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(Icons.Default.Image, contentDescription = null, tint = OrangeSecondary, modifier = Modifier.size(16.dp))
                                            Text(fileUri, style = MaterialTheme.typography.labelSmall, color = Color.White)
                                            IconButton(
                                                onClick = { viewModel.removeAttachment(index) },
                                                modifier = Modifier.size(18.dp)
                                            ) {
                                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.Red, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Field 5: Auto-attached Diagnostics Card (Collapsible)
                item {
                    GlassCardContainer {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.toggleDiagnosticExpanded() },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.PermDeviceInformation, contentDescription = null, tint = OrangeSecondary, modifier = Modifier.size(20.dp))
                                    Text("Auto-Attached Diagnostics Info", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Icon(
                                    imageVector = if (uiState.isDiagnosticExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Expand",
                                    tint = Color.White.copy(alpha = 0.7f)
                                )
                            }

                            AnimatedVisibility(visible = uiState.isDiagnosticExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    DiagnosticRow("Device Model:", uiState.deviceModel)
                                    DiagnosticRow("OS Version:", uiState.osVersion)
                                    DiagnosticRow("App Version:", uiState.appVersion)
                                    DiagnosticRow("User ID:", uiState.userId)
                                }
                            }
                        }
                    }
                }

                // Submit Button
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                viewModel.submitTicket {
                                    showSuccessDialog = true
                                }
                            },
                            enabled = uiState.isFormValid && !uiState.isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OrangePrimary,
                                disabledContainerColor = Color.White.copy(alpha = 0.12f)
                            )
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                                    Text("Send Email", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                        }

                        if (!uiState.isFormValid) {
                            Text(
                                text = "⚠️ Please enter a subject and message body.",
                                style = MaterialTheme.typography.labelSmall,
                                color = OrangeSecondary,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Ticket Success Confirmation Dialog
        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = {
                    showSuccessDialog = false
                    onBack()
                },
                shape = RoundedCornerShape(28.dp),
                containerColor = Color(0xFF181824),
                icon = {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = Color(0xFF4CAF50), modifier = Modifier.size(36.dp))
                    }
                },
                title = { Text("Ticket Submitted! 📩", fontWeight = FontWeight.Bold, color = Color.White) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Your ticket reference #MP-${(10000..99999).random()} has been created.", fontWeight = FontWeight.Bold, color = OrangeSecondary)
                        Text("Our support engineers will review your request and reply to support@muscleplay.com within 2 hours.", color = Color.White.copy(alpha = 0.8f))
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showSuccessDialog = false
                            onBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                    ) {
                        Text("Done", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

@Composable
private fun DiagnosticRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
        Text(value, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
private fun inputColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = OrangePrimary,
    unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
)

@Composable
private fun GlassCardContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
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
                        Color.White.copy(alpha = 0.30f),
                        OrangePrimary.copy(alpha = 0.20f)
                    )
                ),
                shape = RoundedCornerShape(22.dp)
            )
    ) {
        content()
    }
}
