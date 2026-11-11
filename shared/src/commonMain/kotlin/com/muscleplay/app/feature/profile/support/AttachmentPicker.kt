package com.muscleplay.app.feature.profile.support

import androidx.compose.runtime.Composable
import com.muscleplay.app.core.network.dto.SupportAttachment

@Composable
expect fun rememberAttachmentPicker(
    allowedMimeTypes: List<String>,
    onAttachmentsPicked: (List<SupportAttachment>) -> Unit,
): () -> Unit
