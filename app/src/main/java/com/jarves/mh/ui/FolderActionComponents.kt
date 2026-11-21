package com.jarves.mh.ui

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.webkit.*
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jarves.mh.BuildConfig
import com.jarves.mh.R
import com.jarves.mh.model.*
import com.jarves.mh.network.*
import com.jarves.mh.runtime.*
import com.jarves.mh.ui.components.*
import com.jarves.mh.ui.theme.*
import java.io.ByteArrayInputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
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

