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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReadOnlyProjectScreen(
    state: AppUiState,
    onBack: () -> Unit,
    onSwitchChat: (String) -> Unit,
    onContinueHere: () -> Unit,
) {
    BackHandler(onBack = onBack)
    val project = state.readOnlyProject ?: return
    val activeChat = state.readOnlyProjectChats.firstOrNull { it.id == state.readOnlyChatId }
    val listState = rememberLazyListState()
    var showChats by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.readOnlyChatId) {
        if (state.readOnlyMessages.isNotEmpty()) listState.scrollToItem(state.readOnlyMessages.lastIndex)
    }

    if (showChats) {
        ChatSwitcherDialog(
            chats = state.readOnlyProjectChats,
            activeChatId = state.readOnlyChatId,
            switchingEnabled = true,
            allowCreate = false,
            onDismiss = { showChats = false },
            onCreate = {},
            onSwitch = { chatId ->
                onSwitchChat(chatId)
                showChats = false
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(project.name, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            "${activeChat?.localizedTitle() ?: stringResource(R.string.tab_chat)} · ${stringResource(R.string.ws_history)}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.tab_projects)) }
                },
                actions = {
                    IconButton(onClick = { showChats = true }) { Icon(Icons.Default.History, stringResource(R.string.ws_project_chats)) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            ChatTab(
                messages = state.readOnlyMessages,
                approval = null,
                liveProcess = emptyList(),
                isRunning = false,
                onSend = {},
                onStop = {},
                onApproval = {},
                listState = listState,
                taskStartedAtMillis = null,
                taskFinishedAtMillis = null,
                thinkingActive = false,
                agentKind = state.agentKind,
                pendingAttachments = emptyList(),
                onAttach = {},
                onRemoveAttachment = {},
                onOpenAttachment = {},
                onRunInTerminal = {},
                readOnly = true,
                readOnlyBlocked = state.isRunning || state.projectTerminalRunning,
                onContinueHere = onContinueHere,
            )
        }
    }
}

