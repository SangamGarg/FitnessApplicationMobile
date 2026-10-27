package com.muscleplay.app.feature.profile.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.muscleplay.app.feature.profile.ProgressPhoto
import com.muscleplay.app.feature.profile.sampleProgressPhotos

private val OrangePrimary = Color(0xFFFF5722)
private val OrangeSecondary = Color(0xFFFF9800)
private val DarkBackground = Color(0xFF0F0F17)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullPhotoGalleryScreen(onBack: () -> Unit) {
    val photoList = remember { mutableStateListOf(*sampleProgressPhotos.toTypedArray()) }
    var selectedPhoto by remember { mutableStateOf<ProgressPhoto?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Full Progress Gallery", fontWeight = FontWeight.Bold, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { showAddDialog = true }) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = "Add Progress Photo", tint = OrangeSecondary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { innerPadding ->
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(photoList) { photo ->
                    GlassPhotoCard(
                        photo = photo,
                        onClick = { selectedPhoto = photo }
                    )
                }
            }

            // Glassmorphic Details Modal
            selectedPhoto?.let { photo ->
                AlertDialog(
                    onDismissRequest = { selectedPhoto = null },
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
                            Icon(Icons.Default.Image, contentDescription = null, tint = OrangeSecondary, modifier = Modifier.size(28.dp))
                        }
                    },
                    title = { Text(photo.title, fontWeight = FontWeight.Bold, color = Color.White) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Date: ${photo.date}", fontWeight = FontWeight.SemiBold, color = OrangeSecondary)
                            Text("Details: ${photo.subtitle}", color = Color.White.copy(alpha = 0.85f))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(170.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = 0.10f),
                                                OrangePrimary.copy(alpha = 0.20f)
                                            )
                                        )
                                    )
                                    .border(1.dp, OrangePrimary.copy(alpha = 0.4f), RoundedCornerShape(20.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = null,
                                        modifier = Modifier.size(56.dp),
                                        tint = OrangePrimary
                                    )
                                    Text("Encrypted Local Storage Preview", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { selectedPhoto = null },
                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                        ) {
                            Text("Close", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            // Glassmorphic Add Photo Modal with Title, Subtitle Inputs & Upload Photo Option
            if (showAddDialog) {
                var newTitle by remember { mutableStateOf("") }
                var newSubtitle by remember { mutableStateOf("") }
                var attachedPhotoName by remember { mutableStateOf<String?>(null) }

                val isAllThreeProvided = newTitle.isNotBlank() && newSubtitle.isNotBlank() && attachedPhotoName != null

                AlertDialog(
                    onDismissRequest = { showAddDialog = false },
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
                            Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = OrangeSecondary, modifier = Modifier.size(28.dp))
                        }
                    },
                    title = { Text("Add Progress Photo", fontWeight = FontWeight.Bold, color = Color.White) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Enter title, subtitle, and upload an image to save your new progress milestone.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )

                            // 1. Photo Title Field
                            OutlinedTextField(
                                value = newTitle,
                                onValueChange = { newTitle = it },
                                label = { Text("Photo Title *") },
                                placeholder = { Text("e.g. Day 120 - Peak Shape", color = Color.White.copy(alpha = 0.4f)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true,
                                colors = inputColors()
                            )

                            // 2. Subtitle Field
                            OutlinedTextField(
                                value = newSubtitle,
                                onValueChange = { newSubtitle = it },
                                label = { Text("Subtitle / Notes *") },
                                placeholder = { Text("e.g. Hit 80kg bench press goal!", color = Color.White.copy(alpha = 0.4f)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true,
                                colors = inputColors()
                            )

                            // 3. Upload Photo Area
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Upload Image *", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White)

                                if (attachedPhotoName != null) {
                                    Surface(
                                        color = OrangePrimary.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(14.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, OrangePrimary.copy(alpha = 0.5f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(18.dp))
                                                Text(attachedPhotoName!!, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                            IconButton(onClick = { attachedPhotoName = null }, modifier = Modifier.size(20.dp)) {
                                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = {
                                            attachedPhotoName = "progress_photo_${(1000..9999).random()}.jpg"
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(46.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, OrangeSecondary.copy(alpha = 0.6f)),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangeSecondary)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Icon(Icons.Default.UploadFile, contentDescription = null, tint = OrangeSecondary, modifier = Modifier.size(18.dp))
                                            Text("Upload / Capture Image 📸", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            if (!isAllThreeProvided) {
                                Text(
                                    text = "⚠️ Title, subtitle, and image are required to save.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OrangeSecondary
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (isAllThreeProvided) {
                                    val newPhoto = ProgressPhoto(
                                        id = "photo_${(1000..9999).random()}",
                                        title = newTitle,
                                        date = "Today",
                                        subtitle = newSubtitle
                                    )
                                    photoList.add(0, newPhoto)
                                    showAddDialog = false
                                }
                            },
                            enabled = isAllThreeProvided,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OrangePrimary,
                                disabledContainerColor = Color.White.copy(alpha = 0.12f)
                            )
                        ) {
                            Text("Save Photo", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddDialog = false }) {
                            Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun GlassPhotoCard(
    photo: ProgressPhoto,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .clip(RoundedCornerShape(20.dp))
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
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = OrangePrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = photo.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = photo.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = OrangeSecondary
                )
            }
        }
    }
}

@Composable
private fun inputColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = OrangePrimary,
    unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
)
