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
internal fun PreviewTab(ready: Boolean, url: String?) {
    var address by rememberSaveable(url) { mutableStateOf(if (ready) url.orEmpty() else "") }
    var activeUrl by rememberSaveable(url) { mutableStateOf(if (ready) url else null) }
    var addressError by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var webView by remember { mutableStateOf<WebView?>(null) }
    val localUrlError = stringResource(R.string.ws_preview_use_local_url)
    val externalBlockedError = stringResource(R.string.ws_preview_external_blocked)

    val navigate = {
        val normalized = normalizePreviewUrl(address)
        if (normalized == null) {
            addressError = localUrlError
        } else {
            addressError = null
            address = normalized
            activeUrl = normalized
        }
    }

    LaunchedEffect(ready, url) {
        if (ready && !url.isNullOrBlank() && activeUrl == null) {
            normalizePreviewUrl(url)?.let {
                address = it
                activeUrl = it
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            tonalElevation = 1.dp,
        ) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = address,
                        onValueChange = {
                            address = it
                            addressError = null
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        label = { Text(stringResource(R.string.workspace_preview_url)) },
                        placeholder = { Text("localhost:3000") },
                        leadingIcon = {
                            Box(
                                Modifier.size(8.dp).background(
                                    if (activeUrl != null) PocketGreen else MaterialTheme.colorScheme.outline,
                                    CircleShape,
                                ),
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = navigate) {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = stringResource(R.string.ws_open_url))
                            }
                        },
                        isError = addressError != null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Go,
                        ),
                        keyboardActions = KeyboardActions(onGo = { navigate() }),
                    )
                    IconButton(
                        onClick = { webView?.reload() ?: navigate() },
                        enabled = address.isNotBlank(),
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.ws_refresh_preview))
                    }
                }
                if (addressError != null) {
                    Text(
                        addressError.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 16.dp, top = 3.dp),
                    )
                } else if (loading) {
                    LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 5.dp))
                }
            }
        }
        val targetUrl = activeUrl
        if (targetUrl == null) {
            EmptyState(Icons.Default.PlayArrow, stringResource(R.string.workspace_preview_not_running), stringResource(R.string.workspace_preview_not_running_description))
        } else {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        webView = this
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                loading = newProgress < 100
                            }
                        }
                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                val target = request?.url ?: return true
                                if (!target.isLoopbackPreviewUrl()) {
                                    addressError = externalBlockedError
                                    return true
                                }
                                address = target.toString()
                                return false
                            }

                            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                                val target = request?.url ?: return blockedPreviewResponse()
                                return if (target.isLoopbackPreviewUrl()) null else blockedPreviewResponse()
                            }
                        }
                        loadUrl(targetUrl)
                    }
                },
                update = { current ->
                    webView = current
                    if (current.url != targetUrl) current.loadUrl(targetUrl)
                },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

internal fun normalizePreviewUrl(input: String): String? {
    val raw = input.trim()
    if (raw.isBlank()) return null
    val withScheme = if ("://" in raw) raw else "http://$raw"
    val parsed = runCatching { Uri.parse(withScheme) }.getOrNull() ?: return null
    if (!parsed.isLoopbackPreviewUrl() || parsed.host.isNullOrBlank()) return null
    return if (parsed.host == "0.0.0.0") {
        parsed.buildUpon().encodedAuthority(
            buildString {
                append("127.0.0.1")
                if (parsed.port >= 0) append(":${parsed.port}")
            },
        ).build().toString()
    } else {
        parsed.toString()
    }
}

internal fun Uri.isLoopbackPreviewUrl(): Boolean =
    scheme in setOf("data", "blob", "about") ||
        (scheme in setOf("http", "https", "ws", "wss") && host in setOf("127.0.0.1", "localhost", "0.0.0.0"))

internal fun blockedPreviewResponse(): WebResourceResponse =
    WebResourceResponse("text/plain", "UTF-8", 403, "Blocked", emptyMap(), ByteArrayInputStream(ByteArray(0)))

