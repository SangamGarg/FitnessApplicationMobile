package com.muscleplay.app.feature.profile.gallery

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.muscleplay.app.core.network.dto.ProgressGalleryResponseDto
import com.muscleplay.app.feature.profile.GalleryUiState
import com.muscleplay.app.feature.profile.ProfileViewModel
import com.muscleplay.app.feature.profile.data.ProfileRepository
import com.muscleplay.app.feature.profile.support.rememberAttachmentPicker

private val OrangePrimary = Color(0xFFFF5722)
private val OrangeSecondary = Color(0xFFFF9800)
private val DarkBackground = Color(0xFF0F0F17)
private val TextMuted = Color(0xFF9E9B9E)
private val CardBackground = Color(0xFF181824)
private val CardBorderColor = Color(0xFF2A283A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullPhotoGalleryScreen(
    profileRepository: ProfileRepository? = null,
    onBack: () -> Unit,
) {
    val viewModel = remember(profileRepository) {
        profileRepository?.let { ProfileViewModel(it) }
    }
    val galleryState by (viewModel?.galleryState?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf<GalleryUiState>(GalleryUiState.Loading) })

    var selectedPhoto by remember { mutableStateOf<ProgressGalleryResponseDto?>(null) }
    var photoToDelete by remember { mutableStateOf<ProgressGalleryResponseDto?>(null) }

    val openGalleryPicker = rememberAttachmentPicker(
        allowedMimeTypes = listOf("image/jpeg", "image/png", "image/webp"),
        onAttachmentsPicked = { attachments ->
            attachments.firstOrNull()?.let { att ->
                viewModel?.uploadGalleryImage(att)
            }
        }
    )

    LaunchedEffect(Unit) {
        viewModel?.loadProgressGallery()
    }

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
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { openGalleryPicker() }) {
                            Icon(
                                Icons.Default.AddAPhoto,
                                contentDescription = "Add Progress Photo",
                                tint = OrangeSecondary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                when (val state = galleryState) {
                    is GalleryUiState.Loading -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(6) {
                                ShimmerGalleryTile()
                            }
                        }
                    }

                    is GalleryUiState.Success -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(state.items, key = { it.id }) { photo ->
                                GlassPhotoCard(
                                    photo = photo,
                                    onClick = { selectedPhoto = photo }
                                )
                            }
                        }
                    }

                    is GalleryUiState.Empty -> {
                        Text(
                            text = "No progress photos uploaded currently. Kindly upload your first progress photo! 📸",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().align(Alignment.Center)
                        )
                    }

                    is GalleryUiState.Error -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )
                            OutlinedButton(
                                onClick = { viewModel?.loadProgressGallery(forceRefresh = true) },
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, OrangePrimary),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangePrimary)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Retry", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Photo Details Modal
            selectedPhoto?.let { photo ->
                AlertDialog(
                    onDismissRequest = { selectedPhoto = null },
                    shape = RoundedCornerShape(28.dp),
                    containerColor = CardBackground,
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(OrangePrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Image,
                                contentDescription = null,
                                tint = OrangeSecondary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    },
                    title = { Text(photo.caption?.ifBlank { "Progress Photo" } ?: "Progress Photo", fontWeight = FontWeight.Bold, color = Color.White) },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Uploaded: ${photo.uploadedAt?.take(10) ?: "Recent"}", fontWeight = FontWeight.SemiBold, color = OrangeSecondary)
                            Text("Category: ${photo.category ?: "Progress"}", color = Color.White.copy(alpha = 0.85f))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(OrangePrimary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!photo.imageUrl.isBlank()) {
                                    AsyncImage(
                                        model = photo.imageUrl,
                                        contentDescription = photo.caption,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp))
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = null,
                                        modifier = Modifier.size(56.dp),
                                        tint = OrangePrimary
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                photoToDelete = photo
                                selectedPhoto = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete Photo", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { selectedPhoto = null }) {
                            Text("Close", color = TextMuted)
                        }
                    }
                )
            }

            // Confirm Delete Modal
            photoToDelete?.let { photo ->
                AlertDialog(
                    onDismissRequest = { photoToDelete = null },
                    shape = RoundedCornerShape(24.dp),
                    containerColor = CardBackground,
                    title = { Text("Delete Progress Photo?", fontWeight = FontWeight.Bold, color = Color.White) },
                    text = { Text("Are you sure you want to delete this progress photo? This action cannot be undone.", color = Color.White.copy(alpha = 0.85f)) },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel?.deleteGalleryImage(photo.id)
                                photoToDelete = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                        ) {
                            Text("Delete", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { photoToDelete = null }) {
                            Text("Cancel", color = TextMuted)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun GlassPhotoCard(
    photo: ProgressGalleryResponseDto,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = CardBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor),
        modifier = Modifier.fillMaxWidth().height(140.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(OrangePrimary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                if (!photo.imageUrl.isBlank()) {
                    AsyncImage(
                        model = photo.imageUrl,
                        contentDescription = photo.caption,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                    )
                } else {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        tint = OrangePrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
            Text(
                photo.caption?.ifBlank { "Progress Photo" } ?: "Progress Photo",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1
            )
            Text(
                photo.uploadedAt?.take(10) ?: "Recent",
                style = MaterialTheme.typography.labelSmall,
                color = OrangeSecondary,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ShimmerGalleryTile() {
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

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = CardBackground,
        modifier = Modifier.fillMaxWidth().height(140.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = alpha)))
    }
}
