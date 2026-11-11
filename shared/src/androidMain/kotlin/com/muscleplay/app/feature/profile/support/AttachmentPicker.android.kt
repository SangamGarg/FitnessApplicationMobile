package com.muscleplay.app.feature.profile.support

import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.muscleplay.app.core.network.dto.SupportAttachment

@Composable
actual fun rememberAttachmentPicker(
    allowedMimeTypes: List<String>,
    onAttachmentsPicked: (List<SupportAttachment>) -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val mimeArray = if (allowedMimeTypes.isEmpty()) {
        arrayOf("image/*", "application/pdf")
    } else {
        allowedMimeTypes.toTypedArray()
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (!uris.isNullOrEmpty()) {
            val resolver = context.contentResolver
            val list = uris.mapNotNull { uri ->
                try {
                    var name = "attachment"
                    var size = 0L
                    val mimeType = resolver.getType(uri) ?: "application/octet-stream"

                    resolver.query(uri, null, null, null, null)?.use { cursor ->
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                        if (cursor.moveToFirst()) {
                            if (nameIndex != -1) name = cursor.getString(nameIndex) ?: name
                            if (sizeIndex != -1) size = cursor.getLong(sizeIndex)
                        }
                    }

                    val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
                    if (bytes != null) {
                        SupportAttachment(
                            name = name,
                            sizeBytes = if (size > 0) size else bytes.size.toLong(),
                            mimeType = mimeType,
                            bytes = bytes,
                            uriString = uri.toString()
                        )
                    } else null
                } catch (e: Throwable) {
                    null
                }
            }
            onAttachmentsPicked(list)
        }
    }

    return { launcher.launch(mimeArray) }
}
