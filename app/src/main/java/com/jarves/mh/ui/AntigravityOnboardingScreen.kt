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
internal fun AntigravityOnboardingScreen(
    state: AppUiState,
    currentLanguageCode: String,
    onSetLanguage: (String) -> Unit,
    onStartLogin: () -> Unit,
    onSubmitCode: (String) -> Unit,
    onContinue: () -> Unit,
    onSkip: () -> Unit,
    onSelectAgent: (AgentKind) -> Unit,
    onToggleTheme: () -> Unit,
) {
    val clipboard = LocalClipboardManager.current
    var code by rememberSaveable { mutableStateOf("") }
    var showAgentPicker by rememberSaveable { mutableStateOf(false) }
    if (showAgentPicker) {
        AgentSwitchSheet(
            selected = AgentKind.ANTIGRAVITY,
            onSelect = { agent ->
                showAgentPicker = false
                onSelectAgent(agent)
            },
            onDismiss = { showAgentPicker = false },
        )
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.ws_agy_setup_title)) },
                actions = {
                    LanguagePickerAction(currentLanguageCode, onSetLanguage)
                    IconButton(onClick = onToggleTheme) { Icon(Icons.Default.DarkMode, stringResource(R.string.theme_toggle)) }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(24.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(stringResource(R.string.ws_agy_connect_google), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(
                stringResource(R.string.ws_agy_connect_desc),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            when (state.antigravityAuth.status) {
                AntigravityAuthStatus.SIGNED_OUT, AntigravityAuthStatus.ERROR -> {
                    state.antigravityAuth.message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    Button(onClick = onStartLogin, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                        Text(stringResource(R.string.agent_google_signin))
                    }
                }
                AntigravityAuthStatus.STARTING -> {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                    Text(stringResource(R.string.ws_agy_starting_login))
                }
                AntigravityAuthStatus.COMPLETING -> {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                    Text(stringResource(R.string.ws_agy_completing_signin))
                }
                AntigravityAuthStatus.AWAITING_CODE -> {
                    Text(stringResource(R.string.ws_agy_signin_opened))
                    state.antigravityAuth.authorizationUrl?.let { url ->
                        OutlinedButton(
                            onClick = { clipboard.setText(AnnotatedString(url)) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(Icons.Default.ContentCopy, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.ws_agy_copy_signin_url))
                        }
                    }
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text(stringResource(R.string.ws_agy_authorization_code)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Button(
                        onClick = { onSubmitCode(code); code = "" },
                        enabled = code.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(stringResource(R.string.ws_agy_complete_signin)) }
                }
                AntigravityAuthStatus.SIGNED_IN -> {
                    Surface(color = PocketGreen.copy(alpha = 0.12f), shape = RoundedCornerShape(14.dp)) {
                        Text(
                            state.antigravityAuth.accountEmail?.let { stringResource(R.string.ws_agy_connected_as, it) } ?: stringResource(R.string.ws_agy_google_connected),
                            Modifier.fillMaxWidth().padding(16.dp),
                            color = PocketGreen,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Button(onClick = onContinue, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                        Text(stringResource(R.string.action_continue))
                    }
                }
            }
            OnboardingSecondaryActions(onChangeAgent = { showAgentPicker = true }, onSkip = onSkip)
            Surface(color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f), shape = RoundedCornerShape(14.dp)) {
                Text(
                    stringResource(R.string.ws_agy_auto_approval_warning),
                    Modifier.fillMaxWidth().padding(14.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontSize = 12.sp,
                )
            }
        }
    }
}

