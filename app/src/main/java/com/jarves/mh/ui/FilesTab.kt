package com.jarves.mh.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jarves.mh.R
import com.jarves.mh.model.WorkspaceEntry
import com.jarves.mh.ui.theme.PocketOrange

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
                        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
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

@Composable
