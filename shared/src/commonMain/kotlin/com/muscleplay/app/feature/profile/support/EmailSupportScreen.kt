package com.muscleplay.app.feature.profile.support

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.PermDeviceInformation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val OrangePrimary = Color(0xFFFF5722)
private val OrangeSecondary = Color(0xFFFF9800)
private val DarkBackground = Color(0xFF0F0F17)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailSupportScreen(
    emailSupportRepository: com.muscleplay.app.feature.profile.support.data.EmailSupportRepository? = null,
    onBack: () -> Unit,
) {
    val viewModel = remember(emailSupportRepository) { EmailSupportViewModel(emailSupportRepository) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }

    val openAttachmentPicker = rememberAttachmentPicker(
        allowedMimeTypes = uiState.validation.allowedMimeTypes,
        onAttachmentsPicked = { attachments ->
            viewModel.addAttachments(attachments)
        }
    )

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
                            Text(uiState.homeTitle, fontWeight = FontWeight.Bold, color = Color.White)
                        }
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
                // Header Note or Shimmer
                item {
                    if (uiState.isLoadingHome) {
                        ShimmerHeaderCard()
                    } else {
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
                                    Icon(
                                        Icons.Default.Mail,
                                        contentDescription = null,
                                        tint = OrangeSecondary
                                    )
                                }
                                Column {
                                    Text(
                                        uiState.homeSubtitle,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        uiState.homeDescription,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Error Banner if present
                uiState.errorMessage?.let { errorMsg ->
                    item {
                        Surface(
                            color = OrangePrimary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                OrangePrimary.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = errorMsg,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedButton(
                                    onClick = { viewModel.loadHomeData() },
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        OrangePrimary
                                    ),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangePrimary),
                                    contentPadding = PaddingValues(
                                        horizontal = 10.dp,
                                        vertical = 4.dp
                                    )
                                ) {
                                    Icon(
                                        Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Retry", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Field 1: Category Dropdown
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "Issue Category",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        ExposedDropdownMenuBox(
                            expanded = isCategoryDropdownExpanded,
                            onExpandedChange = { isCategoryDropdownExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = uiState.selectedCategoryLabel.ifBlank { "Select Issue Category" },
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
                                uiState.categories.forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text(category.label, color = Color.White) },
                                        onClick = {
                                            viewModel.selectCategory(category.key)
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Subject",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                "${uiState.subject.length} / ${uiState.validation.subjectMaxLength}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (uiState.isSubjectValid) Color.White.copy(alpha = 0.5f) else OrangePrimary
                            )
                        }

                        OutlinedTextField(
                            value = uiState.subject,
                            onValueChange = { input ->
                                if (input.length <= uiState.validation.subjectMaxLength) {
                                    viewModel.updateSubject(input)
                                }
                            },
                            placeholder = {
                                Text(
                                    "Short summary of your issue...",
                                    color = Color.White.copy(alpha = 0.4f)
                                )
                            },
                            trailingIcon = {
                                if (uiState.subject.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.updateSubject("") }) {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = "Clear",
                                            tint = Color.White.copy(alpha = 0.5f)
                                        )
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
                            Text(
                                "Detailed Message",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                "${uiState.messageBody.length} / ${uiState.validation.messageMaxLength}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (uiState.isMessageValid) Color.White.copy(alpha = 0.5f) else OrangePrimary
                            )
                        }

                        OutlinedTextField(
                            value = uiState.messageBody,
                            onValueChange = { input ->
                                if (input.length <= uiState.validation.messageMaxLength) {
                                    viewModel.updateMessageBody(input)
                                }
                            },
                            placeholder = {
                                Text(
                                    "Describe what happened, steps to reproduce, or questions...",
                                    color = Color.White.copy(alpha = 0.4f)
                                )
                            },
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
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Allowed types: JPG, JPEG, PNG, WEBP • Max file size: 5 MB • Max total: 10 MB",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Attachments (${uiState.attachments.size}/${uiState.validation.maxAttachments})",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Button(
                                onClick = { openAttachmentPicker() },
                                enabled = uiState.attachments.size < uiState.validation.maxAttachments,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary.copy(alpha = 0.25f))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.AttachFile,
                                        contentDescription = "Attach",
                                        tint = OrangeSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        "Add File",
                                        fontWeight = FontWeight.Bold,
                                        color = OrangeSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        if (uiState.attachments.isNotEmpty()) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                itemsIndexed(uiState.attachments) { index, att ->
                                    Surface(
                                        color = Color.White.copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(12.dp),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            OrangePrimary.copy(alpha = 0.4f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Image,
                                                contentDescription = null,
                                                tint = OrangeSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                "${att.name} (${formatFileSize(att.sizeBytes)})",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White
                                            )
                                            IconButton(
                                                onClick = { viewModel.removeAttachment(index) },
                                                modifier = Modifier.size(18.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Close,
                                                    contentDescription = "Remove",
                                                    tint = Color.Red,
                                                    modifier = Modifier.size(14.dp)
                                                )
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.PermDeviceInformation,
                                        contentDescription = null,
                                        tint = OrangeSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        "Auto-Attached Diagnostics Info",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
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
                                }
                            }
                        }
                    }
                }

                // Submit Button
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { viewModel.submitTicket() },
                            enabled = uiState.isFormValid && !uiState.isSubmitting,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OrangePrimary,
                                disabledContainerColor = Color.White.copy(alpha = 0.12f)
                            )
                        ) {
                            if (uiState.isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                                    Text("Send Email", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                        }

                        if (!uiState.isFormValid && !uiState.isSubmitting) {
                            Text(
                                text = "⚠️ Please complete all required fields within valid limits.",
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
        if (uiState.ticketSentSuccess) {
            AlertDialog(
                onDismissRequest = {
                    viewModel.dismissSuccessDialog()
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
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                },
                title = { Text("Ticket Submitted! 📩", fontWeight = FontWeight.Bold, color = Color.White) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            uiState.successMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.dismissSuccessDialog()
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
private fun ShimmerHeaderCard() {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

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
                    .background(Color.White.copy(alpha = alpha))
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White.copy(alpha = alpha))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White.copy(alpha = alpha))
                )
            }
        }
    }
}

private fun formatFileSize(sizeBytes: Long): String {
    if (sizeBytes <= 0) return "0 B"
    val kb = sizeBytes / 1024.0
    val mb = kb / 1024.0
    return if (mb >= 1.0) {
        "${(mb * 10).toInt() / 10.0} MB"
    } else {
        "${kb.toInt()} KB"
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
    content: @Composable () -> Unit,
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
