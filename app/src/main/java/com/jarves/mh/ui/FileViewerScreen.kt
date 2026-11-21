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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextRange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FileViewerScreen(
    file: OpenedFileState,
    editingAllowed: Boolean,
    onClose: () -> Unit,
    onBeginEdit: () -> Unit,
    onDraftChange: (String) -> Unit,
    onCancelEdit: () -> Unit,
    onSave: () -> Unit,
    onReload: () -> Unit,
    onDismissConflict: () -> Unit,
) {
    val fileName = file.path.substringAfterLast('/')
    val ext = fileName.substringAfterLast('.', "")
    val isMarkdown = ext == "md"
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    var copied by remember { mutableStateOf(false) }
    var showUnsavedDialog by remember { mutableStateOf(false) }
    var showFind by remember { mutableStateOf(false) }
    var findQuery by remember { mutableStateOf("") }
    var editorValue by remember(file.path, file.editing) { mutableStateOf(TextFieldValue(file.draft)) }
    var undoHistory by remember(file.path, file.editing) { mutableStateOf(emptyList<TextFieldValue>()) }
    var redoHistory by remember(file.path, file.editing) { mutableStateOf(emptyList<TextFieldValue>()) }
    val matches = remember(editorValue.text, findQuery) {
        if (findQuery.isBlank()) emptyList() else buildList {
            var start = 0
            while (start <= editorValue.text.length - findQuery.length) {
                val index = editorValue.text.indexOf(findQuery, start, ignoreCase = true)
                if (index < 0) break
                add(index)
                start = index + findQuery.length.coerceAtLeast(1)
            }
        }
    }
    val currentMatch = matches.indexOfFirst { it >= editorValue.selection.start }.let { if (it < 0 && matches.isNotEmpty()) 0 else it }

    fun leaveEditor() {
        if (file.dirty) showUnsavedDialog = true else onCancelEdit()
    }

    BackHandler {
        if (file.editing) leaveEditor() else onClose()
    }

    if (showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedDialog = false },
            title = { Text(stringResource(R.string.unsaved_changes)) },
            text = { Text(stringResource(R.string.unsaved_changes_description)) },
            confirmButton = {
                TextButton(onClick = { showUnsavedDialog = false; onSave() }) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { showUnsavedDialog = false; onCancelEdit() }) { Text(stringResource(R.string.discard)) }
                    TextButton(onClick = { showUnsavedDialog = false }) { Text(stringResource(R.string.keep_editing)) }
                }
            },
        )
    }

    if (file.conflict) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(stringResource(R.string.file_changed_title)) },
            text = { Text(stringResource(R.string.file_changed_description)) },
            confirmButton = { TextButton(onClick = onReload) { Text(stringResource(R.string.reload)) } },
            dismissButton = { TextButton(onClick = onDismissConflict) { Text(stringResource(R.string.cancel)) } },
        )
    }

    // Code palette follows the app theme (light or dark) instead of a fixed dark scheme.
    val darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val codeBackground = if (darkTheme) Color(0xFF0D1117) else MaterialTheme.colorScheme.surface
    val codeText = if (darkTheme) Color(0xFFE2E8F0) else MaterialTheme.colorScheme.onSurface
    val gutterText = if (darkTheme) Color(0xFF6E7681) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
    val toolbarBackground = if (darkTheme) Color(0xFF161B22) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(fileName, fontWeight = FontWeight.SemiBold)
                        Text(file.path, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { if (file.editing) leaveEditor() else onClose() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.close_file))
                    }
                },
                actions = {
                    if (file.editing) {
                        TextButton(onClick = ::leaveEditor, enabled = !file.saving) { Text(stringResource(R.string.cancel)) }
                        IconButton(onClick = onSave, enabled = file.dirty && !file.saving && editingAllowed) {
                            if (file.saving) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                            else Icon(Icons.Default.Save, stringResource(R.string.save_file))
                        }
                    } else if (file.content != null) {
                        // Empty files (e.g. just created) can still be edited; there is just nothing to copy.
                        if (file.content.isNotEmpty()) {
                            IconButton(onClick = {
                                clipboard.setText(AnnotatedString(file.content))
                                copied = true
                                scope.launch { delay(2000); copied = false }
                            }) {
                                Icon(
                                    if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                                    stringResource(R.string.copy_file_contents),
                                    tint = if (copied) PocketOrange else MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                        if (file.readOnlyReason == null) {
                            IconButton(onClick = onBeginEdit, enabled = editingAllowed) {
                                Icon(Icons.Default.Edit, stringResource(R.string.edit_file))
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                file.loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PocketOrange)
                    }
                }
                file.content == null -> {
                    val reason = when (file.readOnlyReason) {
                        FileReadOnlyReason.BINARY -> R.string.file_binary_description
                        FileReadOnlyReason.INVALID_UTF8 -> R.string.file_invalid_utf8_description
                        FileReadOnlyReason.UNSAFE -> R.string.file_unsafe_description
                        else -> R.string.file_could_not_read
                    }
                    EmptyState(Icons.Default.Description, stringResource(R.string.no_content), stringResource(reason))
                }
                file.editing -> {
                    val vertical = rememberScrollState()
                    val horizontal = rememberScrollState()
                    Column(Modifier.fillMaxSize().background(codeBackground)) {
                        CompositionLocalProvider(LocalContentColor provides codeText) {
                        Row(
                            Modifier.fillMaxWidth().background(toolbarBackground).padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconButton(
                                onClick = {
                                    val previous = undoHistory.lastOrNull() ?: return@IconButton
                                    undoHistory = undoHistory.dropLast(1)
                                    redoHistory = (redoHistory + editorValue).takeLast(50)
                                    editorValue = previous
                                    onDraftChange(previous.text)
                                },
                                enabled = undoHistory.isNotEmpty(),
                            ) { Icon(Icons.Default.Undo, stringResource(R.string.undo)) }
                            IconButton(
                                onClick = {
                                    val next = redoHistory.lastOrNull() ?: return@IconButton
                                    redoHistory = redoHistory.dropLast(1)
                                    undoHistory = (undoHistory + editorValue).takeLast(50)
                                    editorValue = next
                                    onDraftChange(next.text)
                                },
                                enabled = redoHistory.isNotEmpty(),
                            ) { Icon(Icons.Default.Redo, stringResource(R.string.redo)) }
                            IconButton(onClick = { showFind = !showFind }) {
                                Icon(Icons.Default.Search, stringResource(R.string.find_in_file))
                            }
                            Text(
                                if (file.dirty) stringResource(R.string.unsaved) else stringResource(R.string.saved),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                            )
                        }
                        if (showFind) {
                            Row(
                                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                OutlinedTextField(
                                    value = findQuery,
                                    onValueChange = { findQuery = it },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    label = { Text(stringResource(R.string.find)) },
                                )
                                Text(
                                    if (matches.isEmpty()) "0/0" else "${currentMatch + 1}/${matches.size}",
                                    Modifier.padding(horizontal = 8.dp),
                                    fontSize = 12.sp,
                                    color = codeText,
                                )
                                IconButton(onClick = {
                                    if (matches.isNotEmpty()) {
                                        val index = if (currentMatch <= 0) matches.lastIndex else currentMatch - 1
                                        editorValue = editorValue.copy(selection = TextRange(matches[index], matches[index] + findQuery.length))
                                    }
                                }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.previous_match)) }
                                IconButton(onClick = {
                                    if (matches.isNotEmpty()) {
                                        val index = if (currentMatch >= matches.lastIndex) 0 else currentMatch + 1
                                        editorValue = editorValue.copy(selection = TextRange(matches[index], matches[index] + findQuery.length))
                                    }
                                }) { Icon(Icons.AutoMirrored.Filled.ArrowForward, stringResource(R.string.next_match)) }
                            }
                        }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        Row(
                            Modifier.fillMaxSize().verticalScroll(vertical),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Text(
                                (1..editorValue.text.count { it == '\n' } + 1).joinToString("\n"),
                                modifier = Modifier.width(48.dp).padding(horizontal = 8.dp, vertical = 10.dp),
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                                color = gutterText,
                                textAlign = TextAlign.End,
                            )
                            BasicTextField(
                                value = editorValue,
                                onValueChange = { next ->
                                    if (next.text != editorValue.text) {
                                        undoHistory = (undoHistory + editorValue).takeLast(50)
                                        redoHistory = emptyList()
                                    }
                                    editorValue = next
                                    onDraftChange(next.text)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .horizontalScroll(horizontal)
                                    .widthIn(min = 720.dp)
                                    .padding(vertical = 10.dp, horizontal = 8.dp),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontSize = 13.sp,
                                    lineHeight = 20.sp,
                                    color = codeText,
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            )
                        }
                    }
                }
                isMarkdown -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        item { MarkdownText(markdown = file.content, color = MaterialTheme.colorScheme.onSurface) }
                    }
                }
                else -> {
                    // Code / plain-text viewer
                    val codeListState = rememberLazyListState()
                    val targetIndex = file.targetLine?.minus(1)?.coerceAtLeast(0)
                    LaunchedEffect(file.path, targetIndex) {
                        if (targetIndex != null) codeListState.scrollToItem(targetIndex)
                    }
                    LazyColumn(
                        state = codeListState,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .background(codeBackground),
                    ) {
                        val lines = file.content.lines()
                        items(lines.size) { idx ->
                            val isTargetLine = idx == targetIndex
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (isTargetLine) PocketOrange.copy(alpha = 0.18f) else Color.Transparent)
                                    .padding(vertical = 1.dp),
                                verticalAlignment = Alignment.Top,
                            ) {
                                Text(
                                    text = "${idx + 1}",
                                    modifier = Modifier
                                        .width(42.dp)
                                        .padding(start = 8.dp, end = 6.dp),
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    color = gutterText,
                                    textAlign = TextAlign.End,
                                )
                                Text(
                                    text = lines[idx],
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 12.dp),
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontSize = 13.sp,
                                    lineHeight = 19.sp,
                                    color = codeText,
                                )
                            }
                        }
                    }
                }
            }
            if (!file.editing && file.readOnlyReason == FileReadOnlyReason.TOO_LARGE) {
                Surface(
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(stringResource(R.string.file_too_large_description), Modifier.padding(12.dp), fontSize = 12.sp)
                }
            }
        }
    }
}

