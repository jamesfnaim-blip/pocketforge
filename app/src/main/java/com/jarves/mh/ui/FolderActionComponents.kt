package com.jarves.mh.ui

import android.content.Context
import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.core.content.FileProvider
import com.jarves.mh.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal fun FolderActionItems(onNewFile: () -> Unit, onNewFolder: () -> Unit, onUpload: () -> Unit) {
    DropdownMenuItem(
        text = { Text(stringResource(R.string.fm_new_file)) },
        leadingIcon = { Icon(Icons.Default.NoteAdd, null) },
        onClick = onNewFile,
    )
    DropdownMenuItem(
        text = { Text(stringResource(R.string.fm_new_folder)) },
        leadingIcon = { Icon(Icons.Default.CreateNewFolder, null) },
        onClick = onNewFolder,
    )
    DropdownMenuItem(
        text = { Text(stringResource(R.string.fm_upload_here)) },
        leadingIcon = { Icon(Icons.Default.Upload, null) },
        onClick = onUpload,
    )
}

/** Creates an empty file in the app cache for the camera app to write into, shared via FileProvider. */
internal fun createCameraPhotoUri(context: Context): Uri {
    val folder = File(context.cacheDir, "camera").apply { mkdirs() }
    // Photos are copied into the project as attachments, so older captures are no longer needed.
    val staleBefore = System.currentTimeMillis() - 60 * 60 * 1000L
    folder.listFiles()?.filter { it.lastModified() < staleBefore }?.forEach { it.delete() }
    val file = File(folder, "photo-${SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.files", file)
}

@Composable
