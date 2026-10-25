package com.muscleplay.app.feature.profile.support

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import kotlin.random.Random

private val OrangePrimary = Color(0xFFFF5722)
private val OrangeSecondary = Color(0xFFFF9800)
private val DarkBackground = Color(0xFF0F0F17)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RaiseTicketScreen(
    onBack: () -> Unit,
    onViewTickets: () -> Unit = {}
) {
    val categories = listOf(
        "Pose Tracking & Rep Counting",
        "Account & Subscription",
        "Workout Analytics & Progress",
        "App Crash / Performance Bug",
        "Category Not Present / Other"
    )

    val subCategoriesMap = mapOf(
        "Pose Tracking & Rep Counting" to listOf("Inaccurate Rep Count", "Camera Frame Freezing", "Liveness Challenge Issue"),
        "Account & Subscription" to listOf("Pro Subscription Upgrade", "Payment Failure", "Cancel Subscription"),
        "Workout Analytics & Progress" to listOf("Missing Workout Log", "Streak Count Error", "Progress Photo Sync"),
        "App Crash / Performance Bug" to listOf("Screen Freeze", "App Crashes on Launch", "Audio Cue Delay"),
        "Category Not Present / Other" to listOf("General Feedback", "Custom Feature Request", "Other Issue")
    )

    var selectedCategory by remember { mutableStateOf(categories[0]) }
    var selectedSubCategory by remember { mutableStateOf(subCategoriesMap[categories[0]]!![0]) }
    var customCategoryText by remember { mutableStateOf("") }
    var subjectText by remember { mutableStateOf("") }
    var descriptionText by remember { mutableStateOf("") }
    var attachmentName by remember { mutableStateOf<String?>(null) }

    var isCategoryExpanded by remember { mutableStateOf(false) }
    var isSubCategoryExpanded by remember { mutableStateOf(false) }
    var showAttachmentDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var submittedTicketId by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }

    val isCategoryNotPresent = selectedCategory == "Category Not Present / Other"

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
                    title = { Text("Raise Support Ticket", fontWeight = FontWeight.Bold, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
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
                        text = "Describe Your Issue",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OrangeSecondary
                    )
                    Text(
                        text = "Select a category and attach media so our team can resolve your issue quickly.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                // Category Selector
                item {
                    ExposedDropdownMenuBox(
                        expanded = isCategoryExpanded,
                        onExpandedChange = { isCategoryExpanded = !isCategoryExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category", color = OrangePrimary) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = OrangePrimary,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = isCategoryExpanded,
                            onDismissRequest = { isCategoryExpanded = false },
                            modifier = Modifier.background(Color(0xFF181824))
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat, color = Color.White) },
                                    onClick = {
                                        selectedCategory = cat
                                        val subList = subCategoriesMap[cat] ?: listOf("Other Issue")
                                        selectedSubCategory = subList[0]
                                        isCategoryExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Custom Category Text Field (if Category Not Present)
                if (isCategoryNotPresent) {
                    item {
                        OutlinedTextField(
                            value = customCategoryText,
                            onValueChange = { customCategoryText = it },
                            label = { Text("Specify Custom Category / Topic", color = OrangePrimary) },
                            placeholder = { Text("e.g. Heart Rate Monitor Sync", color = Color.White.copy(alpha = 0.4f)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = OrangePrimary,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                            ),
                            singleLine = true
                        )
                    }
                }

                // Sub-Category Selector
                item {
                    val currentSubList = subCategoriesMap[selectedCategory] ?: listOf("Other Issue")
                    ExposedDropdownMenuBox(
                        expanded = isSubCategoryExpanded,
                        onExpandedChange = { isSubCategoryExpanded = !isSubCategoryExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedSubCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Sub-Category", color = OrangePrimary) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isSubCategoryExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = OrangePrimary,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = isSubCategoryExpanded,
                            onDismissRequest = { isSubCategoryExpanded = false },
                            modifier = Modifier.background(Color(0xFF181824))
                        ) {
                            currentSubList.forEach { subCat ->
                                DropdownMenuItem(
                                    text = { Text(subCat, color = Color.White) },
                                    onClick = {
                                        selectedSubCategory = subCat
                                        isSubCategoryExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Subject Text Field
                item {
                    OutlinedTextField(
                        value = subjectText,
                        onValueChange = { subjectText = it },
                        label = { Text("Subject", color = OrangePrimary) },
                        placeholder = { Text("Brief summary of the problem", color = Color.White.copy(alpha = 0.4f)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                        ),
                        singleLine = true
                    )
                }

                // Description Text Field
                item {
                    OutlinedTextField(
                        value = descriptionText,
                        onValueChange = { descriptionText = it },
                        label = { Text("Detailed Description", color = OrangePrimary) },
                        placeholder = { Text("Provide details, steps to reproduce...", color = Color.White.copy(alpha = 0.4f)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                        ),
                        minLines = 4,
                        maxLines = 6
                    )
                }

                // Attachment Section (Photo / Video)
                item {
                    Text(
                        text = "Upload Video or Image (Optional)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (attachmentName != null) {
                        GlassCardContainer {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AttachFile, contentDescription = null, tint = OrangePrimary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = attachmentName!!,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    )
                                }
                                IconButton(onClick = { attachmentName = null }) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White)
                                }
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = { showAttachmentDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangeSecondary)
                        ) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = OrangeSecondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Attach Photo or Recording", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Submit Button
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val randomNum = Random.nextInt(10000, 99999)
                            submittedTicketId = "#MP-$randomNum"
                            showSuccessDialog = true
                        },
                        enabled = subjectText.isNotBlank() && descriptionText.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrangePrimary,
                            disabledContainerColor = Color.White.copy(alpha = 0.12f)
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.White)
                            Text("Submit Support Ticket", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        }
                    }
                }
            }

            // Glassmorphic Attachment Source Dialog
            if (showAttachmentDialog) {
                AlertDialog(
                    onDismissRequest = { showAttachmentDialog = false },
                    shape = RoundedCornerShape(28.dp),
                    containerColor = Color(0xFF181824),
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(OrangePrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AttachFile, contentDescription = null, tint = OrangeSecondary, modifier = Modifier.size(28.dp))
                        }
                    },
                    title = { Text("Choose Attachment Source", fontWeight = FontWeight.Bold, color = Color.White) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Surface(
                                onClick = {
                                    attachmentName = "camera_capture_${Random.nextInt(1000, 9999)}.mp4"
                                    showAttachmentDialog = false
                                },
                                color = Color.White.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, OrangePrimary.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = OrangeSecondary)
                                    Text("Take Photo or Video (Live Camera)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                }
                            }

                            Surface(
                                onClick = {
                                    attachmentName = "workout_log_screenshot.png"
                                    showAttachmentDialog = false
                                },
                                color = Color.White.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(Icons.Default.Collections, contentDescription = null, tint = OrangeSecondary)
                                    Text("Choose from Gallery", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showAttachmentDialog = false }) {
                            Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                        }
                    }
                )
            }

            // Glassmorphic Success Dialog
            if (showSuccessDialog) {
                AlertDialog(
                    onDismissRequest = { showSuccessDialog = false },
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
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(36.dp))
                        }
                    },
                    title = { Text("Ticket Submitted! 📩", fontWeight = FontWeight.Bold, color = Color.White) },
                    text = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Your ticket reference number is:", color = Color.White.copy(alpha = 0.8f))
                            Text(
                                text = submittedTicketId,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = OrangeSecondary
                            )
                            Text(
                                text = "Our support team will review your ticket and reply within 24 hours.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showSuccessDialog = false
                                onViewTickets()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                        ) {
                            Text("View My Tickets", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showSuccessDialog = false
                                onBack()
                            }
                        ) {
                            Text("Done", color = Color.White.copy(alpha = 0.6f))
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
