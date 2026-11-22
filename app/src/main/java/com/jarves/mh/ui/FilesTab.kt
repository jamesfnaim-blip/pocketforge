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
import androidx.compose.ui.text.TextRange

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun FilesTab(
    files: List<WorkspaceEntry>,
    loading: Boolean,
    suggestedProjectRoot: String?,
    onRefresh: () -> Unit,
    onLoadDirectory: (String) -> Unit,
    onOpenFile: (WorkspaceEntry) -> Unit,
    onUseSuggestedProjectRoot: () -> Unit,
    selectionMode: Boolean,
    exportSelection: ExportSelection,
    exportRunning: Boolean,
    exportEnabled: Boolean,
    onEnterSelection: () -> Unit,
    onCancelSelection: () -> Unit,
    onToggleSelection: (WorkspaceEntry) -> Unit,
    onSelectAll: () -> Unit,
    onDownloadSelection: () -> Unit,
    fileOpsEnabled: Boolean = false,
    onCreateEntry: (parentPath: String, name: String, isDirectory: Boolean) -> Unit = { _, _, _ -> },
    onRenameEntry: (path: String, newName: String) -> Unit = { _, _ -> },
    onDeleteEntry: (path: String) -> Unit = {},
    onUploadInto: (parentPath: String) -> Unit = {},
    onShareFile: (path: String) -> Unit = {},
    onSaveFileCopy: (path: String) -> Unit = {},
) {
    var expandedDirectories by rememberSaveable { mutableStateOf(emptyList<String>()) }
    var nameDialog by remember { mutableStateOf<FileNameDialog?>(null) }
    var deleteTarget by remember { mutableStateOf<WorkspaceEntry?>(null) }
    var rootMenuOpen by remember { mutableStateOf(false) }
    LaunchedEffect(files.map { it.path }) {
        val directories = files.asSequence().filter { it.isDirectory }.map { it.path }.toSet()
        expandedDirectories = expandedDirectories.filter { it in directories }
    }
    val expandedSet = expandedDirectories.toSet()
    val visibleFiles = files.filter { entry ->
        val segments = entry.path.split('/')
        segments.size == 1 || (1 until segments.size).all { depth ->
            segments.take(depth).joinToString("/") in expandedSet
        }
    }
    fun selectionState(entry: WorkspaceEntry): ToggleableState {
        if (!entry.isDirectory) return if (exportSelection.includes(entry.path)) ToggleableState.On else ToggleableState.Off
        val descendants = files.filter { it.path == entry.path || it.path.startsWith("${entry.path}/") }
        val selected = descendants.count { exportSelection.includes(it.path) }
        return when {
            selected == 0 -> ToggleableState.Off
            selected == descendants.size -> ToggleableState.On
            else -> ToggleableState.Indeterminate
        }
    }
    val hasSelection = files.any { !it.isDirectory && exportSelection.includes(it.path) } ||
        files.any { it.isDirectory && selectionState(it) != ToggleableState.Off }

    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                ),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        stringResource(if (selectionMode) R.string.select_files else R.string.workspace_files),
                        Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (selectionMode) {
                        TextButton(onClick = onSelectAll) { Text(stringResource(R.string.select_all), fontSize = 11.sp) }
                        TextButton(onClick = onCancelSelection, enabled = !exportRunning) { Text(stringResource(R.string.cancel), fontSize = 11.sp) }
                    } else if (expandedDirectories.isNotEmpty()) {
                        TextButton(onClick = { expandedDirectories = emptyList() }) {
                            Icon(Icons.Default.KeyboardArrowUp, null, Modifier.size(17.dp))
                            Spacer(Modifier.width(3.dp))
                            Text(stringResource(R.string.collapse_all), fontSize = 11.sp)
                        }
                    }
                    if (!selectionMode) {
                        Box {
                            IconButton(onClick = { rootMenuOpen = true }, enabled = fileOpsEnabled) {
                                Icon(Icons.Default.Add, stringResource(R.string.fm_new))
                            }
                            DropdownMenu(expanded = rootMenuOpen, onDismissRequest = { rootMenuOpen = false }) {
                                FolderActionItems(
                                    onNewFile = { rootMenuOpen = false; nameDialog = FileNameDialog.Create("", isDirectory = false) },
                                    onNewFolder = { rootMenuOpen = false; nameDialog = FileNameDialog.Create("", isDirectory = true) },
                                    onUpload = { rootMenuOpen = false; onUploadInto("") },
                                )
                            }
                        }
                    }
                    if (!selectionMode && !loading && files.any { !it.isDirectory }) {
                        IconButton(onClick = onEnterSelection, enabled = exportEnabled) {
                            Icon(Icons.Default.Download, stringResource(R.string.export_project_zip))
                        }
                    }
                    if (loading) {
                        CircularProgressIndicator(Modifier.padding(12.dp).size(20.dp), strokeWidth = 2.dp)
                    } else {
                        IconButton(onClick = onRefresh) { Icon(Icons.Default.Refresh, stringResource(R.string.ws_refresh_files)) }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
        if (suggestedProjectRoot != null) {
            item(key = "suggested-project-root") {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.ws_project_folder_detected), fontWeight = FontWeight.Bold)
                        Text(
                            stringResource(R.string.ws_use_project_root_desc, suggestedProjectRoot),
                            fontSize = 13.sp,
                        )
                        Button(onClick = onUseSuggestedProjectRoot, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.ws_use_project_root, suggestedProjectRoot))
                        }
                    }
                }
            }
        }
        if (!loading && files.isEmpty()) {
            item { EmptyState(Icons.Default.Folder, stringResource(R.string.workspace_no_files), stringResource(R.string.workspace_no_files_description)) }
        }
        items(visibleFiles, key = { it.path }) { entry ->
            var entryMenuOpen by remember { mutableStateOf(false) }
            Row(
                Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onLongClick = { if (!selectionMode && fileOpsEnabled) entryMenuOpen = true },
                    ) {
                        if (selectionMode) {
                            onToggleSelection(entry)
                        } else if (entry.isDirectory) {
                            if (entry.path !in expandedSet) onLoadDirectory(entry.path)
                            expandedDirectories = if (entry.path in expandedSet) {
                                expandedDirectories.filterNot { it == entry.path || it.startsWith("${entry.path}/") }
                            } else {
                                expandedDirectories + entry.path
                            }
                        } else {
                            onOpenFile(entry)
                        }
                    }
                    .padding(start = (entry.depth * 20).dp)
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (selectionMode) {
                    if (entry.isDirectory) {
                        TriStateCheckbox(
                            state = selectionState(entry),
                            onClick = { onToggleSelection(entry) },
                        )
                    } else {
                        Checkbox(
                            checked = exportSelection.includes(entry.path),
                            onCheckedChange = { onToggleSelection(entry) },
                        )
                    }
                    Spacer(Modifier.width(2.dp))
                }
                if (entry.isDirectory) {
                    Icon(
                        if (entry.path in expandedSet) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        stringResource(if (entry.path in expandedSet) R.string.collapse_folder else R.string.expand_folder),
                        Modifier.size(28.dp).clickable {
                            if (entry.path !in expandedSet) onLoadDirectory(entry.path)
                            expandedDirectories = if (entry.path in expandedSet) {
                                expandedDirectories.filterNot { it == entry.path || it.startsWith("${entry.path}/") }
                            } else expandedDirectories + entry.path
                        }.padding(5.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.width(5.dp))
                }
                Icon(
                    if (entry.isDirectory) Icons.Default.Folder else Icons.Default.Description,
                    null,
                    tint = if (entry.isDirectory) PocketOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(11.dp))
                Text(
                    if (entry.isDirectory) "${entry.name} (${entry.childCount?.toString() ?: "…"})" else entry.name,
                    Modifier.weight(1f),
                    color = if (!entry.isDirectory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                )
                if (!entry.isDirectory) {
                    Spacer(Modifier.width(8.dp))
                    Text(formatFileSize(entry.sizeBytes), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (!selectionMode) {
                    Box {
                        IconButton(
                            onClick = { entryMenuOpen = true },
                            enabled = fileOpsEnabled,
                            modifier = Modifier.size(32.dp),
                        ) {
                            Icon(
                                Icons.Default.MoreVert,
                                stringResource(R.string.fm_more_actions, entry.name),
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        DropdownMenu(expanded = entryMenuOpen, onDismissRequest = { entryMenuOpen = false }) {
                            if (entry.isDirectory) {
                                FolderActionItems(
                                    onNewFile = { entryMenuOpen = false; nameDialog = FileNameDialog.Create(entry.path, isDirectory = false) },
                                    onNewFolder = { entryMenuOpen = false; nameDialog = FileNameDialog.Create(entry.path, isDirectory = true) },
                                    onUpload = { entryMenuOpen = false; onUploadInto(entry.path) },
                                )
                            } else {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.fm_share)) },
                                    leadingIcon = { Icon(Icons.Default.Share, null) },
                                    onClick = { entryMenuOpen = false; onShareFile(entry.path) },
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.fm_save_copy)) },
                                    leadingIcon = { Icon(Icons.Default.Download, null) },
                                    onClick = { entryMenuOpen = false; onSaveFileCopy(entry.path) },
                                )
                            }
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.fm_rename)) },
                                leadingIcon = { Icon(Icons.Default.DriveFileRenameOutline, null) },
                                onClick = { entryMenuOpen = false; nameDialog = FileNameDialog.Rename(entry.path, entry.name) },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.fm_delete), color = MaterialTheme.colorScheme.error) },
                                leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                                onClick = { entryMenuOpen = false; deleteTarget = entry },
                            )
                        }
                    }
                }
            }
            if (!entry.isDirectory) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(start = (entry.depth * 20 + 42).dp))
            }
        }
        if (selectionMode) {
            item(key = "download-selection") {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onDownloadSelection,
                    enabled = hasSelection && !exportRunning && exportEnabled,
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
                ) {
                    if (exportRunning) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Download, null)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(if (exportRunning) R.string.creating_zip else R.string.download_zip))
                }
            }
        }
    }

    nameDialog?.let { dialog ->
        var name by remember(dialog) {
            val initial = (dialog as? FileNameDialog.Rename)?.currentName.orEmpty()
            // Preselect the stem so typing replaces the name but keeps the extension.
            mutableStateOf(TextFieldValue(initial, TextRange(0, initial.substringBeforeLast('.', initial).length)))
        }
        val focusRequester = remember { FocusRequester() }
        LaunchedEffect(dialog) { focusRequester.requestFocus() }
        val title = when (dialog) {
            is FileNameDialog.Create -> stringResource(if (dialog.isDirectory) R.string.fm_new_folder else R.string.fm_new_file)
            is FileNameDialog.Rename -> stringResource(R.string.fm_rename)
        }
        val confirm = {
            when (dialog) {
                is FileNameDialog.Create -> onCreateEntry(dialog.parentPath, name.text, dialog.isDirectory)
                is FileNameDialog.Rename -> onRenameEntry(dialog.path, name.text)
            }
            nameDialog = null
        }
        AlertDialog(
            onDismissRequest = { nameDialog = null },
            title = { Text(title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    val parent = when (dialog) {
                        is FileNameDialog.Create -> dialog.parentPath
                        is FileNameDialog.Rename -> dialog.path.substringBeforeLast('/', "")
                    }
                    Text(
                        "/" + parent,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(R.string.fm_name)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { if (name.text.isNotBlank()) confirm() }),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
                            cursorColor = MaterialTheme.colorScheme.primary,
                        ),
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = confirm, enabled = name.text.isNotBlank()) {
                    Text(stringResource(if (dialog is FileNameDialog.Rename) R.string.fm_rename else R.string.fm_create))
                }
            },
            dismissButton = { TextButton(onClick = { nameDialog = null }) { Text(stringResource(R.string.action_cancel)) } },
        )
    }
    deleteTarget?.let { entry ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text(stringResource(R.string.fm_delete_title, entry.name)) },
            text = { Text(stringResource(if (entry.isDirectory) R.string.fm_delete_folder_desc else R.string.fm_delete_file_desc)) },
            confirmButton = {
                TextButton(onClick = { onDeleteEntry(entry.path); deleteTarget = null }) {
                    Text(stringResource(R.string.fm_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text(stringResource(R.string.action_cancel)) } },
        )
    }
}

private sealed interface FileNameDialog {
    data class Create(val parentPath: String, val isDirectory: Boolean) : FileNameDialog
    data class Rename(val path: String, val currentName: String) : FileNameDialog
}

