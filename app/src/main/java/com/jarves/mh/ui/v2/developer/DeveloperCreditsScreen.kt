package com.jarves.mh.ui.v2.developer

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.jarves.mh.BuildConfig
import com.jarves.mh.ui.components.PhosphorIcon
import com.jarves.mh.ui.components.PhosphorIconType
import com.jarves.mh.ui.v2.components.ForgeButton
import com.jarves.mh.ui.v2.components.ForgeButtonStyle
import com.jarves.mh.ui.v2.components.ForgeCard
import com.jarves.mh.ui.v2.components.ForgeCardStyle
import com.jarves.mh.ui.v2.theme.ForgeTheme

@Composable
fun DeveloperCreditsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val colors = ForgeTheme.colors
    val typography = ForgeTheme.typography
    val shapes = ForgeTheme.shapes

    var showLicenses by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // TOP APP BAR
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
                PhosphorIcon(icon = PhosphorIconType.CARET_DOWN, tint = colors.textPrimary)
            }
            Text(
                text = "Developer & Credits",
                style = typography.titleMedium,
                color = colors.textPrimary,
            )
        }

        // DEVELOPER PROFILE CARD
        ForgeCard(
            style = ForgeCardStyle.ELEVATED,
            contentPadding = 20.dp,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Profile Avatar Box
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(shapes.card)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(colors.primary, colors.accent)
                            )
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "NH",
                        style = typography.displayMedium,
                        color = Color.White,
                    )
                }

                Text(
                    text = "Nayem Hossain",
                    style = typography.titleLarge,
                    color = colors.textPrimary,
                )
                Text(
                    text = "Founder & Lead Architect, PocketForge AI",
                    style = typography.labelMedium,
                    color = colors.accent,
                )
                Text(
                    text = "\"Empowering developers and creators to build real software anywhere, directly from their phone.\"",
                    style = typography.bodySmall,
                    color = colors.textSecondary,
                    fontStyle = FontStyle.Italic,
                )

                Spacer(modifier = Modifier.height(6.dp))

                // External Links
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ForgeButton(
                        text = "nayemhsn.com",
                        icon = PhosphorIconType.GLOBE,
                        onClick = {
                            runCatching {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://nayemhsn.com")))
                            }
                        },
                        style = ForgeButtonStyle.OUTLINE,
                        modifier = Modifier.weight(1f),
                    )
                    ForgeButton(
                        text = "LinkedIn",
                        icon = PhosphorIconType.ARROW_SQUARE_OUT,
                        onClick = {
                            runCatching {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://linkedin.com/in/nayemdevbd")))
                            }
                        },
                        style = ForgeButtonStyle.OUTLINE,
                        modifier = Modifier.weight(1f),
                    )
                }

                Text(
                    text = "PocketForge AI v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
                    style = typography.codeSmall,
                    color = colors.textMuted,
                )
            }
        }

        // OPEN-SOURCE ACKNOWLEDGMENTS
        ForgeCard(style = ForgeCardStyle.DEFAULT) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Open-Source Foundation",
                    style = typography.titleSmall,
                    color = colors.textPrimary,
                )
                Text(
                    text = "PocketForge AI is built upon Mobile Harness by Jarves (MIT License) and the PRoot user-space Linux emulation engine (GPL-2.0).",
                    style = typography.bodySmall,
                    color = colors.textSecondary,
                )

                HorizontalDivider(thickness = 1.dp, color = colors.border)

                LicenseRow("Mobile Harness", "MIT License", "Core Linux PTY & process management engine")
                LicenseRow("PRoot & care", "GPL-2.0", "Unprivileged user-space Linux execution on Android")
                LicenseRow("talloc", "LGPL-3.0", "Hierarchical memory allocation library")
                LicenseRow("Phosphor Icons", "MIT License", "Consistent, premium UI iconography")
                LicenseRow("Noto Sans Bengali", "SIL OFL 1.1", "Native Bengali typographic rendering")
                LicenseRow("JetBrains Mono", "SIL OFL 1.1", "Developer monospaced code font")
                LicenseRow("Inter", "SIL OFL 1.1", "UI display and interface typeface")
            }
        }

        // VIEW FULL LICENSES BUTTON
        ForgeButton(
            text = if (showLicenses) "Hide Full License Details" else "View Full License Notices",
            onClick = { showLicenses = !showLicenses },
            style = ForgeButtonStyle.GHOST,
            modifier = Modifier.fillMaxWidth(),
        )

        if (showLicenses) {
            ForgeCard(style = ForgeCardStyle.DEFAULT) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Full License Notices",
                        style = typography.titleSmall,
                        color = colors.textPrimary,
                    )
                    Text(
                        text = """
                            MIT License: Copyright (c) 2024 Jarves / Mobile Harness contributors.
                            Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files to deal in the Software without restriction...

                            GPL v2: Copyright (C) PRoot developers. PRoot is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation...

                            SIL Open Font License 1.1: Copyright (c) Google LLC (Noto Sans Bengali), JetBrains s.r.o. (JetBrains Mono), Rasmus Andersson (Inter).
                        """.trimIndent(),
                        style = typography.codeSmall,
                        color = colors.textMuted,
                    )
                }
            }
        }
    }
}

@Composable
private fun LicenseRow(
    name: String,
    license: String,
    description: String,
) {
    val colors = ForgeTheme.colors
    val typography = ForgeTheme.typography

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = name, style = typography.labelMedium, color = colors.textPrimary)
            Text(text = license, style = typography.codeSmall, color = colors.accent)
        }
        Text(text = description, style = typography.bodySmall, color = colors.textSecondary)
    }
}
