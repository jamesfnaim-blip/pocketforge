package com.jarves.mh.ui

import android.provider.Settings
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.ui.graphics.vector.ImageVector
import com.jarves.mh.R

internal enum class RootScreen(@StringRes val labelRes: Int, val icon: ImageVector) {
    PROJECTS(R.string.tab_projects, Icons.Default.Folder),
    AGENT(R.string.tab_agent, Icons.Default.SmartToy),
    SETTINGS(R.string.tab_settings, Icons.Default.Settings),
}
internal enum class WorkspaceTab(@StringRes val labelRes: Int, val icon: ImageVector) {
    CHAT(R.string.tab_chat, Icons.Default.AutoAwesome),
    FILES(R.string.tab_files, Icons.Default.Folder),
    TERMINAL(R.string.tab_terminal, Icons.Default.Terminal),
    CHANGES(R.string.tab_changes, Icons.Default.Code),
    PREVIEW(R.string.tab_preview, Icons.Default.Preview),
}

