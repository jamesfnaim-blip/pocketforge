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
internal fun ProviderSetupScreen(
    initial: ProviderProfile,
    onboarding: Boolean,
    agentKind: AgentKind = AgentKind.CLAUDE_CODE,
    initialStep: Int = if (onboarding) 0 else 1,
    onBack: (() -> Unit)? = null,
    currentLanguageCode: String = "system",
    onSetLanguage: ((String) -> Unit)? = null,
    onSave: (ProviderProfile, String) -> Unit,
    // Onboarding only: finish first run without a provider; it can be configured later in Settings.
    onSkip: (() -> Unit)? = null,
    onDiscover: suspend (ProviderProfile, String) -> ModelDiscoveryResult,
    onValidate: suspend (ProviderProfile, String, List<DiscoveredModel>) -> ConnectionValidation,
    onSelectAgent: (AgentKind) -> Unit,
    onToggleTheme: (() -> Unit)? = null,
    themeMode: AppThemeMode = AppThemeMode.DARK,
) {
    val context = LocalContext.current
    var step by rememberSaveable { mutableIntStateOf(initialStep) }
    var selected by rememberSaveable { mutableStateOf(initial.kind) }
    var baseUrl by rememberSaveable { mutableStateOf(initial.baseUrl.ifBlank { initial.kind.defaultBaseUrl }) }
    var model by rememberSaveable { mutableStateOf(initial.model.ifBlank { initial.kind.defaultModel }) }
    var dshApi by rememberSaveable { mutableStateOf(initial.dshApi.ifBlank { "anthropic-messages" }) }
    var apiKey by rememberSaveable { mutableStateOf("") }
    var showAgentPicker by rememberSaveable { mutableStateOf(false) }

    if (showAgentPicker) {
        AgentSwitchSheet(
            selected = agentKind,
            onSelect = { agent ->
                showAgentPicker = false
                onSelectAgent(agent)
            },
            onDismiss = { showAgentPicker = false },
        )
    }

    val handleBack: (() -> Unit)? = when {
        step > 1 -> { { step = 1 } }
        onBack != null -> onBack
        else -> null
    }

    if (handleBack != null) {
        BackHandler(onBack = handleBack)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (onboarding) R.string.setup_mobile_harness else R.string.provider_setup_settings_title)) },
                navigationIcon = {
                    if (handleBack != null) {
                        IconButton(onClick = handleBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.action_back))
                        }
                    }
                },
                actions = {
                    if (onSetLanguage != null) {
                        LanguagePickerAction(
                            currentLanguageCode = currentLanguageCode,
                            onSelectLanguage = onSetLanguage,
                        )
                    }
                    if (onToggleTheme != null) {
                        IconButton(onClick = onToggleTheme) {
                            Icon(
                                if (themeMode == AppThemeMode.DARK) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = stringResource(R.string.theme_toggle),
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
        ) {
            if (onboarding) StepDots(step)
            Spacer(Modifier.height(16.dp))
            when (step) {
                0 -> DeviceCheckStep(context, onContinue = { step = 1 })
                1 -> ProviderChoiceStep(
                    selected = selected,
                    agentKind = agentKind,
                    onSelected = {
                        if (selected != it) {
                            selected = it
                            baseUrl = it.defaultBaseUrl
                            model = it.defaultModel
                            apiKey = ""
                        }
                    },
                    onContinue = { step = 2 },
                    onChangeAgent = { showAgentPicker = true },
                    onSkip = onSkip,
                )
                else -> ProviderCredentialsStep(
                    provider = selected,
                    agentKind = agentKind,
                    baseUrl = baseUrl,
                    model = model,
                    dshApi = dshApi,
                    apiKey = apiKey,
                    onBaseUrl = {
                        baseUrl = it
                        if (agentKind == AgentKind.DEEPSEEK_HARNESS && selected == ProviderKind.CUSTOM) {
                            dshApi = inferredDshApiForUrl(it)
                        }
                    },
                    onModel = { model = it },
                    onDshApi = { dshApi = it },
                    onApiKey = { apiKey = it },
                    hasStoredSecret = initial.kind == selected && initial.hasSecret,
                    onDiscover = {
                        val url = if (selected.fixedBaseUrl) selected.defaultBaseUrl else baseUrl.trim()
                        onDiscover(ProviderProfile(selected, url, model.trim(), dshApi = dshApi), apiKey)
                    },
                    onValidate = { models ->
                        val url = if (selected.fixedBaseUrl) selected.defaultBaseUrl else baseUrl.trim()
                        onValidate(ProviderProfile(selected, url, model.trim(), dshApi = dshApi), apiKey, models)
                    },
                    onSave = {
                        val url = if (selected.fixedBaseUrl) selected.defaultBaseUrl else baseUrl.trim()
                        onSave(ProviderProfile(selected, url, model.trim(), dshApi = dshApi), apiKey)
                    },
                    onChangeAgent = { showAgentPicker = true },
                    onSkip = onSkip,
                )
            }
        }
    }
}

